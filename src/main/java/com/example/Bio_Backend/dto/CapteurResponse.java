package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour un capteur. */
public class CapteurResponse {

    private Long id;
    private String type;
    private LocalDateTime dateInstallation;
    private Long serreId;

    public CapteurResponse(Long id, String type, LocalDateTime dateInstallation, Long serreId) {
        this.id = id;
        this.type = type;
        this.dateInstallation = dateInstallation;
        this.serreId = serreId;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getDateInstallation() {
        return dateInstallation;
    }

    public Long getSerreId() {
        return serreId;
    }
}
