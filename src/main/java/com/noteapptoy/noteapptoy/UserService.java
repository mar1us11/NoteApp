package com.noteapptoy.noteapptoy;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, NoteRepository noteRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.noteRepository = noteRepository;
    }

    public UserDto getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return new UserDto(user.getId(), user.getUsername());
    }


    public UserDto updateUser(String Username, UserRequest request) {
        User user = userRepository.findByUsername(Username)
                .orElseThrow(() -> new UserNotFoundException(Username));

        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {

            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser.getId(), savedUser.getUsername());
    }

    public void deleteUser(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        List<Note> notes = user.getNotes();
        noteRepository.deleteAll(notes);

        userRepository.deleteById(user.getId());
    }

    public UserDto updatePartUser(Authentication authentication, UserPartRequest request) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {

            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return new UserDto(savedUser.getId(), savedUser.getUsername());
    }
}
