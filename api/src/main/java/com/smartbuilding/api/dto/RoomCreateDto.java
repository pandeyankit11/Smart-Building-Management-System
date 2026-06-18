package com.smartbuilding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a room on a specific floor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateDto {
    private int floorNumber;
    private String name;
    private int capacity;
    private String location;
}
