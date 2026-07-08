package com.noteapptoy.noteapptoy;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Invalid password or username.");
    }
}
