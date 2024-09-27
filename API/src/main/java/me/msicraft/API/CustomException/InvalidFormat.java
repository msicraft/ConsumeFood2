package me.msicraft.API.CustomException;

public class InvalidFormat extends RuntimeException {
    public InvalidFormat(String message, String format, String correctFormat) {
        super(message + " -> Invalid Format: " + format + " (" + correctFormat + ")");
    }
}
