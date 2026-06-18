package com.smartbuilding.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a floor via the REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloorCreateDto {
    private String name;
    private int number;
}
