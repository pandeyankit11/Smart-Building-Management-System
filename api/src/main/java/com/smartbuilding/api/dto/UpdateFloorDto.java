package com.smartbuilding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a floor's name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFloorDto {
    private String name;
}
