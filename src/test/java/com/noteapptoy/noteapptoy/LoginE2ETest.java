package com.noteapptoy.noteapptoy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginE2ETest extends AbstractE2ETest {

    private WebDriver driver;
    private LoginPage loginPage;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        driver = createDriver();
        loginPage = new LoginPage(driver);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldLogin() {
        String username = "e2e" + UUID.randomUUID();
        String password = "password";

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        NotesPage notesPage = loginPage.open()
                .enterUsername(username)
                .enterPassword(password)
                .submitExpectingSuccess();

        assertEquals("Hi, " + username, notesPage.getGreetingText());
    }

    @Test
    void shouldSignUp() {
        String username = "e2e" + UUID.randomUUID();
        String password = "password";

        NotesPage notesPage = loginPage.open()
                .chooseCreateAccount()
                .enterUsername(username)
                .enterPassword(password)
                .submitExpectingSuccess();

        assertEquals("Hi, " + username, notesPage.getGreetingText());
    }

    @Test
    void shouldNotLogInWithBadCreds() {
        LoginPage errorPage = loginPage.open()
                .enterUsername("dwfewdwdwd")
                .enterPassword("12345678")
                .submitExpectingFailure();

        assertEquals("Request failed (401)", errorPage.getErrorMessage());
    }
}
