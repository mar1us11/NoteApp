package com.noteapptoy.noteapptoy;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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


    @Test
    void shouldCreateNote() {
        PostNoteRequest postNoteRequest = new PostNoteRequest("title1", "content1");

        given().contentType(ContentType.JSON)
                .auth().basic("John", "password")
                .body(postNoteRequest)
        .when()
                .post("/notes")
        .then()
                .statusCode(201)
                .body("title", equalTo("title1"))
                .body("content", equalTo("content1"));

    }
}
