package com.dnd.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session extends PanacheEntity {

    @Column(name = "session_number", nullable = false)
    public Integer sessionNumber;

    @Column(name = "session_date")
    public LocalDate sessionDate;

    public String summary;

    @Column(name = "xp_awarded")
    public Integer xpAwarded = 0;

    public String notes;

    // Relationship
    @ManyToOne
    @JoinColumn(name = "campaign_id")
    public Campaign campaign;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
