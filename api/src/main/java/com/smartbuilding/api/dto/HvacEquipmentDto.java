package com.smartbuilding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A thin view of HVAC‑type equipment.
 */
@Data @AllArgsConstructor
public class HvacEquipmentDto {
    private String equipmentId;
    private String name;
    private double energyConsumption; // kWh
    private String status; // OPERATIONAL / MAINTENANCE / MALFUNCTIONING
}
