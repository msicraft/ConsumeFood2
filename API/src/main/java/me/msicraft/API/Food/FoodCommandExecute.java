package me.msicraft.API.Food;

public class FoodCommandExecute {

    public enum ExecuteType {
        CONSOLE, PLAYER
    }
    private final String command;
    private final ExecuteType executeType;

    public FoodCommandExecute(String command, ExecuteType executeType) {
        this.command = command;
        this.executeType = executeType;
    }

    public String getCommand() {
        return command;
    }

    public ExecuteType getExecuteType() {
        return executeType;
    }

}
