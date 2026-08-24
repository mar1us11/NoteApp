package com.noteapptoy.noteapptoy;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class NoteControllerTestRest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        userRepository.deleteAll();

        User user = new User();
        user.setUsername("John");
        user.setPassword(passwordEncoder.encode("password"));

        userRepository.save(user);
    }


    private RequestSpecification authenticatedRequest() {
        return given()
                .contentType(ContentType.JSON)
                .auth()
                .basic("John", "password");
    }


    @Test
    void shouldCreateNote() {
        PostNoteRequest postNoteRequest = new PostNoteRequest("title1", "content1");

        authenticatedRequest()
                .body(postNoteRequest)
        .when()
                .post("/notes")
        .then()
                .statusCode(201)
                .body("title", equalTo("title1"))
                .body("content", equalTo("content1"));

    }


    @Test
    void shouldThrowIfNoteTitleAlreadyUsedForPOST() {

        User user = userRepository.findByUsername("John")
                .orElseThrow(() -> new UserNotFoundException("John"));

        Note note = new Note();
        note.setTitle("title1");
        note.setContent("content1");
        user.addNote(note);
        noteRepository.save(note);

        PostNoteRequest request = new PostNoteRequest("title1", "content1");

        given().contentType(ContentType.JSON)
                .auth().basic("John", "password")
                .body(request)
        .when()
                .post("/notes")
        .then()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("error", equalTo("Conflict"))
                .body("message", equalTo("Note with title title1 already exists."));
    }

    @Test
    void shouldThrowIfTitleOrContentBlankForPOST() {
        PostNoteRequest request = new PostNoteRequest(null, "content1");

        given().contentType(ContentType.JSON)
                .auth().basic("John", "password")
                .body(request)
        .when()
                .post("/notes")
        .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("must not be blank"));

    }

    @Test
    void shouldReturn401WhenNotAuthenticatedForPost() {
        PostNoteRequest request = new PostNoteRequest("title1", "content1");

        given().contentType(ContentType.JSON)
               .body(request)
        .when()
               .post("/notes")
        .then()
               .statusCode(401);
    }

    @Test
    void shouldGetNote() {
        User user = userRepository.findByUsername("John")
                .orElseThrow(() -> new UserNotFoundException("John"));

        Note note = new Note();
        note.setTitle("title1");
        note.setContent("content1");
        user.addNote(note);
        noteRepository.save(note);

        authenticatedRequest()
        .when()
                .get("/notes/title1")
        .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("title", equalTo("title1"))
                .body("content", equalTo("content1"));
    }

    @Test
    void shouldThrowIfNotNotExistForGET(){

        authenticatedRequest()
        .when()
                .get("/notes/title1")
        .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"))
                .body("message", equalTo("Note with title title1 not found."));
    }

    @Test
    void shouldUpdateNote(){
        User user = userRepository.findByUsername("John")
                .orElseThrow(() -> new UserNotFoundException("John"));

        Note note = new Note();
        note.setContent("content1");
        note.setTitle("title1");

        user.addNote(note);
        noteRepository.save(note);

        Note updated = noteRepository.findByUserUsernameAndTitle("John", "title2")
                .orElseThrow();

        Assertions.assertEquals("content2", updated.getContent());

        CreateNoteRequest request = new CreateNoteRequest("title2", "content2");

        given().contentType(ContentType.JSON)
                .auth().basic(user.getUsername(), "password")
                .body(request)
        .when()
                .put("/notes/title1")
        .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("title", equalTo("title2"))
                .body("content", equalTo("content2"));

    }

    @Test
    void shouldThrowIfNoteNotFoundForPUT() {

        CreateNoteRequest request = new CreateNoteRequest("title2", "content2");

        authenticatedRequest()
                .body(request)
        .when()
                .put("/notes/title1")
        .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"))
                .body("message", equalTo("Note with title title1 not found."));
    }

    @Test
    void shouldDeleteNote() {

        User user = userRepository.findByUsername("John")
                .orElseThrow(() -> new UserNotFoundException("John"));

        Note note = new Note();
        note.setContent("content1");
        note.setTitle("title1");

        user.addNote(note);
        noteRepository.save(note);

        authenticatedRequest().pathParam("title", "title1")
        .when()
                .delete("notes/{title}")
        .then()
                .statusCode(204);
    }

    @Test
        void shouldThrowWhenNothingToDelete() {

            authenticatedRequest().pathParam("title", "title1")
            .when()
                    .delete("notes/{title}")
            .then()
                    .statusCode(404)
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("message", equalTo("Note with title title1 not found."));

    }

}
