package com.noteapptoy.noteapptoy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    boolean existsByUserUsernameAndTitle(String username, String title);

    boolean existsByTitle(String title);

    Optional<Note> findByUserUsernameAndTitle(String username, String title);

    List<Note> findAllByUserUsername(String username);

}
