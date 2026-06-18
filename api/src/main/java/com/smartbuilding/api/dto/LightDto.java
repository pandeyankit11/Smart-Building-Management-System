package com.smartbuilding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Minimal view of a lighting unit.
 */
@Data @AllArgsConstructor
public class LightDto {
    private String lightId;
    private String location;
    private boolean on;
    private int brightness; // 0-100 %
}
