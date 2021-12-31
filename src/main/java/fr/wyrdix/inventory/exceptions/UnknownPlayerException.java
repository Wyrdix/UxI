package fr.wyrdix.inventory.exceptions;

public class UnknownPlayerException extends Exception {
    public UnknownPlayerException() {
        super("Player is not connected or isn't known.");
    }
}
