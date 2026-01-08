package com.dnd.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;

@Entity
public class Campaign extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    public String description;

    public String setting;

    @Column(nullable = false)
    public String status = "ACTIVE";

    @ManyToOne
    @JoinColumn(name = "dm_id")
    public User dm;

    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
