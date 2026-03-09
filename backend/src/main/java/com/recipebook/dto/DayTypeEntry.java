package com.recipebook.dto;

import java.time.LocalDate;

public class DayTypeEntry {
    public LocalDate date;
    public String dayType;

    public DayTypeEntry() {}

    public DayTypeEntry(LocalDate date, String dayType) {
        this.date = date;
        this.dayType = dayType;
    }
}
