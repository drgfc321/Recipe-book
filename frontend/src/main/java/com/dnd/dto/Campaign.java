package com.dnd.dto;

import java.time.LocalDateTime;

public class Campaign {
    private Long id;
    private String name;
    private String description;
    private String setting;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Campaign() {}

    public Campaign(String name, String description, String setting, String status) {
        this.name = name;
        this.description = description;
        this.setting = setting;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSetting() { return setting; }
    public void setSetting(String setting) { this.setting = setting; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
