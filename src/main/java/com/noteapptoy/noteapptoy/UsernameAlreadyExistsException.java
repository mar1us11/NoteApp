package com.noteapptoy.noteapptoy;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String name) {
        super("The username " + name + " already exists.");
    }
}
