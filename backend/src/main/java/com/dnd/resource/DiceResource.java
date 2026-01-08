package com.dnd.resource;

import com.dnd.dto.DiceRollRequest;
import com.dnd.dto.DiceRollResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/api/dice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiceResource {

    private static final Random random = new Random();
    private static final Pattern DICE_PATTERN = Pattern.compile("(\\d+)?d(\\d+)");

    @POST
    @Path("/roll")
    public DiceRollResponse roll(DiceRollRequest request) {
        if (request.dice == null || request.dice.isEmpty()) {
            throw new BadRequestException("Dice notation is required (e.g., '2d6', '1d20')");
        }

        Matcher matcher = DICE_PATTERN.matcher(request.dice.toLowerCase());
        if (!matcher.matches()) {
            throw new BadRequestException("Invalid dice notation. Use format like '2d6' or '1d20'");
        }

        int numDice = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 1;
        int dieSize = Integer.parseInt(matcher.group(2));

        if (numDice < 1 || numDice > 100) {
            throw new BadRequestException("Number of dice must be between 1 and 100");
        }
        if (dieSize < 2 || dieSize > 100) {
            throw new BadRequestException("Die size must be between 2 and 100");
        }

        int modifier = request.modifier != null ? request.modifier : 0;
        List<Integer> rolls = new ArrayList<>();
        String description;

        // Handle advantage/disadvantage for d20
        if (dieSize == 20 && numDice == 1 && (Boolean.TRUE.equals(request.advantage) || Boolean.TRUE.equals(request.disadvantage))) {
            int roll1 = rollDie(dieSize);
            int roll2 = rollDie(dieSize);
            rolls.add(roll1);
            rolls.add(roll2);

            int chosen;
            if (Boolean.TRUE.equals(request.advantage)) {
                chosen = Math.max(roll1, roll2);
                description = String.format("Advantage: rolled %d and %d, took %d", roll1, roll2, chosen);
            } else {
                chosen = Math.min(roll1, roll2);
                description = String.format("Disadvantage: rolled %d and %d, took %d", roll1, roll2, chosen);
            }

            int total = chosen + modifier;
            if (modifier != 0) {
                description += String.format(" + %d = %d", modifier, total);
            }

            return new DiceRollResponse(request.dice, rolls, modifier, total, description);
        }

        // Normal roll
        int sum = 0;
        for (int i = 0; i < numDice; i++) {
            int roll = rollDie(dieSize);
            rolls.add(roll);
            sum += roll;
        }

        int total = sum + modifier;

        if (numDice == 1) {
            description = String.format("Rolled %d", rolls.get(0));
        } else {
            description = String.format("Rolled %s = %d", rolls.toString(), sum);
        }

        if (modifier > 0) {
            description += String.format(" + %d = %d", modifier, total);
        } else if (modifier < 0) {
            description += String.format(" - %d = %d", Math.abs(modifier), total);
        }

        return new DiceRollResponse(request.dice, rolls, modifier, total, description);
    }

    private int rollDie(int sides) {
        return random.nextInt(sides) + 1;
    }
}
