package com.dnd.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
public class Character extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    public String race;

    @Column(name = "class")
    public String characterClass;

    public Integer level = 1;

    // Stats
    public Integer strength = 10;
    public Integer dexterity = 10;
    public Integer constitution = 10;
    public Integer intelligence = 10;
    public Integer wisdom = 10;
    public Integer charisma = 10;

    // Combat
    @Column(name = "hit_points")
    public Integer hitPoints = 10;

    @Column(name = "armor_class")
    public Integer armorClass = 10;

    public Integer speed = 30;

    public String backstory;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    public Campaign campaign;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt = LocalDateTime.now();
}
