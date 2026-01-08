package com.dnd.dto;

public class Character {
    private Long id;
    private String name;
    private String race;
    private String characterClass;
    private Integer level;
    private Integer strength;
    private Integer dexterity;
    private Integer constitution;
    private Integer intelligence;
    private Integer wisdom;
    private Integer charisma;
    private Integer hitPoints;
    private Integer armorClass;
    private Integer speed;
    private String backstory;
    private Long campaignId;

    public Character() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }

    public String getCharacterClass() { return characterClass; }
    public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getStrength() { return strength; }
    public void setStrength(Integer strength) { this.strength = strength; }

    public Integer getDexterity() { return dexterity; }
    public void setDexterity(Integer dexterity) { this.dexterity = dexterity; }

    public Integer getConstitution() { return constitution; }
    public void setConstitution(Integer constitution) { this.constitution = constitution; }

    public Integer getIntelligence() { return intelligence; }
    public void setIntelligence(Integer intelligence) { this.intelligence = intelligence; }

    public Integer getWisdom() { return wisdom; }
    public void setWisdom(Integer wisdom) { this.wisdom = wisdom; }

    public Integer getCharisma() { return charisma; }
    public void setCharisma(Integer charisma) { this.charisma = charisma; }

    public Integer getHitPoints() { return hitPoints; }
    public void setHitPoints(Integer hitPoints) { this.hitPoints = hitPoints; }

    public Integer getArmorClass() { return armorClass; }
    public void setArmorClass(Integer armorClass) { this.armorClass = armorClass; }

    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }

    public String getBackstory() { return backstory; }
    public void setBackstory(String backstory) { this.backstory = backstory; }

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

    public String getDisplayName() {
        return String.format("%s - Level %d %s %s", name, level != null ? level : 1, race, characterClass);
    }
}
