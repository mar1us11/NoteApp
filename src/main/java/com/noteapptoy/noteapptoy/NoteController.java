package com.noteapptoy.noteapptoy;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{title}")
    public ResponseEntity<NoteDto> getNote(@PathVariable String title, Authentication authentication) {
        return ResponseEntity.ok(noteService.getNote(title, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody PostNoteRequest request,
           Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(request, authentication.getName()));
    }

    @PutMapping("/{title}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable String title, Authentication authentication,
           @Valid @RequestBody CreateNoteRequest request){
        NoteDto note = noteService.updateNote( title, authentication.getName(), request);
        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<Void> deleteNote(@PathVariable String title, Authentication authentication) {
        noteService.deleteNote(authentication.getName(), title);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{title}")
    public ResponseEntity<NoteDto> updatePartNote(@PathVariable String title,
               Authentication authentication, @RequestBody PatchNoteDto request) {
        NoteDto note = noteService.updatePartNote(title, authentication.getName(), request);
        return ResponseEntity.ok(note);
    }

    @GetMapping
    public ResponseEntity<List<NoteDto>> getAllNotes(Authentication authentication) {
        return ResponseEntity.ok(noteService.getAllNotes(authentication.getName()));
    }

}
