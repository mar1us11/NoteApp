package com.noteapptoy.noteapptoy;

public class DuplicateNoteTitleException extends RuntimeException {
    public DuplicateNoteTitleException(String title) {
        super("Note with title " + title + " already exists.");
    }
}
