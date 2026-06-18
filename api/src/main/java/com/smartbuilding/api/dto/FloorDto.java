package com.smartbuilding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Summary information for a floor.
 */
@Data @AllArgsConstructor
public class FloorDto {
    private int floorNumber;
    private String floorName;
    private int totalCapacity;
    private int currentOccupancy;
    private double occupancyRate; // 0-100 %
}
