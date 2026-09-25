package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour une notification interne (module Notification, ajoute le 2026-09-24). */
public class NotificationResponse {

    private Long id;
    private String canal;
    private String message;
    private String statutEnvoi;
    private LocalDateTime dateEnvoi;

    public NotificationResponse(Long id, String canal, String message, String statutEnvoi, LocalDateTime dateEnvoi) {
        this.id = id;
        this.canal = canal;
        this.message = message;
        this.statutEnvoi = statutEnvoi;
        this.dateEnvoi = dateEnvoi;
    }

    public Long getId() {
        return id;
    }

    public String getCanal() {
        return canal;
    }

    public String getMessage() {
        return message;
    }

    public String getStatutEnvoi() {
        return statutEnvoi;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }
}
