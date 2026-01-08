package com.dnd.dto;

public class DiceRollRequest {
    public String dice;        // e.g., "2d6", "1d20"
    public Integer modifier;   // e.g., 3 for +3
    public Boolean advantage;  // Roll twice, take higher (d20 only)
    public Boolean disadvantage; // Roll twice, take lower (d20 only)
}
