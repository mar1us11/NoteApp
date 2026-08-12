package com.noteapptoy.noteapptoy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/noteApp",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.hibernate.ddl-auto=none"
})
public class LoginTest2 {

    private WebDriver driver;
    private LoginPage loginPage;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,900");

        driver = new ChromeDriver(options);

        loginPage = new LoginPage(driver);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldLogin(){

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
