package com.noteapptoy.noteapptoy;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService (NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public NoteDto updateNote(String title, String username, CreateNoteRequest request) {
        Note note = noteRepository.findByUserUsernameAndTitle(username, title)
                .orElseThrow(() -> new NoteNotFoundException(title));

        if (request.getTitle() != null &&
                !request.getTitle().equals(note.getTitle()) &&
                noteRepository.existsByUserUsernameAndTitle(username, request.getTitle())) {

            throw new DuplicateNoteTitleException(request.getTitle());
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        Note updatedNote = noteRepository.save(note);
        return new NoteDto(updatedNote.getId(), updatedNote.getTitle(), updatedNote.getContent());

    }

    public void deleteNote(String username, String title) {
        Note note = noteRepository.findByUserUsernameAndTitle(username, title)
                .orElseThrow(() -> new NoteNotFoundException(title));

        note.getUser().removeNote(note);

        noteRepository.deleteById(note.getId());
    }

    public NoteDto getNote(String title, String username) {
        Note note = noteRepository.findByUserUsernameAndTitle(username, title)
            .orElseThrow(() -> new NoteNotFoundException(title));
        return new NoteDto(note.getId(), note.getTitle(), note.getContent());
    }

    public NoteDto createNote(PostNoteRequest request, String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));


        if (noteRepository.existsByUserUsernameAndTitle(userName, request.getTitle())) {
            throw new DuplicateNoteTitleException(request.getTitle());
        }
        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        user.addNote(note);
        Note savedNote = noteRepository.save(note);
        return new NoteDto(savedNote.getId(), savedNote.getTitle(), savedNote.getContent());
    }

    public NoteDto updatePartNote(String title, String username, PatchNoteDto request) {
        Note note = noteRepository.findByUserUsernameAndTitle(username, title)
                .orElseThrow(() -> new NoteNotFoundException(title));

        if (request.getTitle() != null &&
                !request.getTitle().equals(note.getTitle()) &&
                noteRepository.existsByUserUsernameAndTitle(username, request.getTitle())) {

            throw new DuplicateNoteTitleException(request.getTitle());
        }

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        Note updatedNote = noteRepository.save(note);
        return new NoteDto(updatedNote.getId(), updatedNote.getTitle(), updatedNote.getContent());
    }

    public List<NoteDto> getAllNotes(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        List<Note> notes = noteRepository.findAllByUserUsername(username);
        return notes.stream()
                .map(note -> new NoteDto(note.getId(), note.getTitle(), note.getContent()))
                .toList();
    }
}
