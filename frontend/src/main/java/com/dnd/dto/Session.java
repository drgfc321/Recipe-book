package com.dnd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Session {
    private Long id;
    private Integer sessionNumber;
    private LocalDate sessionDate;
    private String summary;
    private Integer xpAwarded;
    private String notes;
    private Long campaignId;
    private LocalDateTime createdAt;

    public Session() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(Integer sessionNumber) { this.sessionNumber = sessionNumber; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getXpAwarded() { return xpAwarded; }
    public void setXpAwarded(Integer xpAwarded) { this.xpAwarded = xpAwarded; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
