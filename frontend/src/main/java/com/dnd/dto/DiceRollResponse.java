package com.dnd.dto;

import java.util.List;

public class DiceRollResponse {
    private String dice;
    private List<Integer> rolls;
    private Integer modifier;
    private Integer total;
    private String description;

    public String getDice() { return dice; }
    public void setDice(String dice) { this.dice = dice; }

    public List<Integer> getRolls() { return rolls; }
    public void setRolls(List<Integer> rolls) { this.rolls = rolls; }

    public Integer getModifier() { return modifier; }
    public void setModifier(Integer modifier) { this.modifier = modifier; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
