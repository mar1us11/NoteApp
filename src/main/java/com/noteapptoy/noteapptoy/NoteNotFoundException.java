package com.noteapptoy.noteapptoy;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String title) {
        super("Note with title " + title + " not found.");
    }
}