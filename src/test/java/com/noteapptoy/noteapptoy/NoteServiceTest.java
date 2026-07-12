package com.noteapptoy.noteapptoy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void testCreateNote() {
        PostNoteRequest postNoteRequest = new PostNoteRequest("Test Title", "Test Content");

        User user = new User();
        user.setUsername("Test Name");

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Test Title");
        savedNote.setContent("Test Content");
        savedNote.setUser(user);

        when(userRepository.findByUsername("Test Name")).thenReturn(Optional.of(user));
        when(noteRepository.existsByUserUsernameAndTitle("Test Name", "Test Title")).thenReturn(false);
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        NoteDto result = noteService.createNote(postNoteRequest, "Test Name");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());

        Note noteToSave = noteCaptor.getValue();
        assertEquals("Test Title", noteToSave.getTitle());
        assertEquals("Test Content", noteToSave.getContent());
        assertEquals(user, noteToSave.getUser());
    }

    @Test
    void shouldThrowWhenUserDoesNotExistForPOST(){
        PostNoteRequest postNoteRequest = new PostNoteRequest("Test Title", "Test Content");

        when(userRepository.findByUsername("Test Name")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
            noteService.createNote(postNoteRequest, "Test Name"));

        verify(noteRepository, never())
                .save(any());

    }

    @Test
    void shouldThrowWhenNoteAlreadyExistsForPOST(){
        PostNoteRequest postNoteRequest = new PostNoteRequest("Test Title", "Test Content");

        User user = new User();
        user.setUsername("Test Name");

        Note note = new Note();

        when(userRepository.findByUsername("Test Name")).thenReturn(Optional.of(user));
        when(noteRepository.existsByUserUsernameAndTitle("Test Name", "Test Title")).thenReturn(true);

        assertThrows(DuplicateNoteTitleException.class, () ->
                noteService.createNote(postNoteRequest, "Test Name"));

        verify(noteRepository, never())
                .save(any());
    }


    @Test
    void shouldThrowWhenNoteDoesNotExistForPUT() {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest("Updated Title", "Updated Content");
        String title = "Test Title";
        User user = new User();
        user.setUsername("Test Name");

        when(noteRepository.findByUserUsernameAndTitle("Test Name", "Test Title")).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () ->
                noteService.updateNote(title, "Test Name", createNoteRequest));

        verify(noteRepository, never())
                .save(any());

    }


    @Test
    void testUpdateNote() {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest("Updated Title", "Updated Content");

        User user = new User();
        user.setUsername("Test Name");

        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle("Old Title");
        existingNote.setContent("Old Content");
        existingNote.setUser(user);

        when(noteRepository.existsByUserUsernameAndTitle("Test Name", "Updated Title")).thenReturn(false);
        when(noteRepository.findByUserUsernameAndTitle("Test Name", "Old Title")).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteDto result = noteService.updateNote("Old Title", "Test Name", createNoteRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());

        Note noteToSave = noteCaptor.getValue();
        assertEquals("Updated Title", noteToSave.getTitle());
        assertEquals("Updated Content", noteToSave.getContent());
    }

    @Test
    void shouldThrowWhenTitleIsRepeatedForPUT() {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest("Updated Title", "Updated Content");
        String title = "Test Title";

        User user = new User();
        user.setUsername("Test Name");

        Note existingNote = new Note();
        existingNote.setId(1L);
        existingNote.setTitle(title);
        existingNote.setContent("Old Content");
        existingNote.setUser(user);

        when(noteRepository.findByUserUsernameAndTitle("Test Name", title)).thenReturn(Optional.of(existingNote));
        when(noteRepository.existsByUserUsernameAndTitle(
                "Test Name",
                "Updated Title"))
                .thenReturn(true);

        assertThrows(DuplicateNoteTitleException.class, () ->
                noteService.updateNote(title, "Test Name", createNoteRequest));

        verify(noteRepository, never())
                .save(any());
    }

    @Test
    void deleteNoteTest() {
        String title = "Test title";
        User user = new User();
        user.setUsername("Test Name");

        Note note = new Note();
        note.setId(1L);
        note.setTitle(title);
        note.setContent("Test Content");
        note.setUser(user);
        user.addNote(note);

        when(noteRepository.findByUserUsernameAndTitle("Test Name", title)).thenReturn(Optional.of(note));

        noteService.deleteNote("Test Name", title);

        verify(noteRepository).deleteById(1L);
        assertFalse(user.getNotes().contains(note));
        assertNull(note.getUser());

    }

    @Test
    void shouldThrowWhenNoteDoesNotExistForDELETE() {
        String title = "Test Title";

        User user = new User();
        user.setUsername("Test Name");

        Note note = new Note();
        note.setId(1L);
        note.setTitle(title);
        note.setContent("Test Content");
        note.setUser(user);

        when(noteRepository.findByUserUsernameAndTitle("Test Name", title)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () ->
                noteService.deleteNote("Test Name", title));

        verify(noteRepository, never())
                .deleteById(any());
    }


    @Test
    void getNoteTest() {
        String title = "Test Title";

        User user = new User();
        user.setUsername("Test Name");

        Note note = new Note();
        note.setId(1L);
        note.setTitle(title);
        note.setContent("Test Content");

        user.addNote(note);

        when(noteRepository.findByUserUsernameAndTitle(user.getUsername(), title)).thenReturn(Optional.of(note));

        NoteDto result = noteService.getNote(title, user.getUsername());


        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals(1L, (long)result.getId());

        verify(noteRepository).findByUserUsernameAndTitle(user.getUsername(), title);

    }

    @Test
    void shouldThrowIfNoteNotFound() {
        String title = "Test Title";
        String username = "Test Username";


        when(noteRepository.findByUserUsernameAndTitle(username, title)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.getNote(username, title));

        verify(noteRepository).findByUserUsernameAndTitle(username, title);

    }

}