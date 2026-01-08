package com.dnd.dto;

import java.util.List;

public class DiceRollResponse {
    public String dice;
    public List<Integer> rolls;
    public Integer modifier;
    public Integer total;
    public String description;

    public DiceRollResponse() {}

    public DiceRollResponse(String dice, List<Integer> rolls, Integer modifier, Integer total, String description) {
        this.dice = dice;
        this.rolls = rolls;
        this.modifier = modifier;
        this.total = total;
        this.description = description;
    }
}
