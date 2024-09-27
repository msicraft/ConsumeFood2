package me.msicraft.API.CustomException;

import org.bukkit.Material;

public class MigrationFail extends RuntimeException {

    public MigrationFail(String reason, String internalName) {
        super("Migration Fail Reason: " + reason + " -> InternalName: " + internalName);
    }

    public MigrationFail(String reason, Material material) {
        super("Migration Fail Reason: " + reason + " -> Material: " + material.name());
    }

    public MigrationFail(String reason) {
        super("Migration Fail Reason: " + reason);
    }

}
