package me.msicraft.API.CustomException;

public class MigrationFail extends RuntimeException {

    public MigrationFail(String reason, String internalName) {
        super("Migration Fail Reason: " + reason + " -> InternalName: " + internalName);
    }

    public MigrationFail(String reason) {
        super("Migration Fail Reason: " + reason);
    }

}
