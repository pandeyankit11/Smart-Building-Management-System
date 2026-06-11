package com.smartbuilding.exception;

/**
 * Exception 2: EquipmentNotFoundException
 * Thrown when requested equipment cannot be found.
 */
public class EquipmentNotFoundException extends Exception {
    private String equipmentId;

    public EquipmentNotFoundException(String equipmentId) {
        super("Equipment with ID " + equipmentId + " not found");
        this.equipmentId = equipmentId;
    }

    public EquipmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.equipmentId = null;
    }

    // Getter
    public String getEquipmentId() { return equipmentId; }
}
