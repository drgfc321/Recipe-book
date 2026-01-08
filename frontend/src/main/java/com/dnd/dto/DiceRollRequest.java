package com.dnd.dto;

public class DiceRollRequest {
    private String dice;
    private Integer modifier;
    private Boolean advantage;
    private Boolean disadvantage;

    public DiceRollRequest() {}

    public DiceRollRequest(String dice) {
        this.dice = dice;
    }

    public DiceRollRequest(String dice, Integer modifier) {
        this.dice = dice;
        this.modifier = modifier;
    }

    public String getDice() { return dice; }
    public void setDice(String dice) { this.dice = dice; }

    public Integer getModifier() { return modifier; }
    public void setModifier(Integer modifier) { this.modifier = modifier; }

    public Boolean getAdvantage() { return advantage; }
    public void setAdvantage(Boolean advantage) { this.advantage = advantage; }

    public Boolean getDisadvantage() { return disadvantage; }
    public void setDisadvantage(Boolean disadvantage) { this.disadvantage = disadvantage; }
}
