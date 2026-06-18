package com.smartbuilding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Public representation of an alert (active or resolved).
 */
@Data @AllArgsConstructor
public class AlertDto {
    private String alertId;
    private String type;
    private String message;
    private String severity; // INFO / WARNING / HIGH / CRITICAL
    private boolean resolved;
}
