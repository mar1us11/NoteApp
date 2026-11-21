package com.noteapptoy.noteapptoy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/notesShared")
public class NoteSharedController {

    private final NoteService noteService;

    public NoteSharedController(NoteService noteService) {
        this.noteService = noteService;
    }


}
