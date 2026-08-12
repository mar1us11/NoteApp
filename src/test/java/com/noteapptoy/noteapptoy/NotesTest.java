package com.noteapptoy.noteapptoy;

import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/noteApp",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.hibernate.ddl-auto=none"
})
public class NotesTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    private WebDriver driver;
    private NotesPage notesPage;
    private LoginPage loginPage;


    private String password;
    private String username;


    @BeforeEach
    void setUp() {
        password = "password";
        username = "e2e" + UUID.randomUUID();

        //ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless=new");
        //options.addArguments("--window-size=1440,900");

        driver = new ChromeDriver();

        loginPage = new LoginPage(driver);

        notesPage = loginPage.open()
                .chooseCreateAccount()
                .enterUsername(username)
                .enterPassword(password)
                .submitExpectingSuccess();
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldCreateNote() {
        String title = "title";
        String content = "content";
        notesPage.clickNewNoteButton()
                .addNoteTitle(title)
                .addNoteContent(content)
                .clickSaveNote();

        Assertions.assertTrue(notesPage.isNoteVisible(title));

    }


    @Test
    void shouldNotCreateNoteWithDupTitle() {
        String title = "title";
        String content = "content";
        Note note = new Note();
        note.setContent(content);
        note.setTitle(title);

        String userName = username;

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> userRepository.existsByUsername(username));

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));

        note.setUser(user);

        Note savedNote = noteRepository.save(note);


        String title2 = title;
        String content2 = "content";

        notesPage.clickNewNoteButton()
                .addNoteTitle(title2)
                .addNoteContent(content2)
                .clickSaveNote();

        Assertions.assertEquals("Note with title " + title + " already exists."
                , notesPage.getErrorForDupNote()
        );

    }

    @Test
    void shouldDeleteNote() {
        String title = "title";
        String content = "content";
        Note note = new Note();
        note.setContent(content);
        note.setTitle(title);

        String userName = username;

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> userRepository.existsByUsername(username));

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));

        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        //user.addNote(savedNote);

        driver.navigate().refresh();

        Assertions.assertTrue(notesPage.isNoteVisible(title));

        notesPage.clickOnNote(savedNote.getId())
                .clickDeleteNote()
                .confirmNoteDeletion();

        Assertions.assertTrue(notesPage.isNoteGone(title));
    }

    @Test
    void shouldBeAbleToEditNote() {
        String title = "title";
        String content = "content";
        Note note = new Note();
        note.setContent(content);
        note.setTitle(title);

        String userName = username;

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> userRepository.existsByUsername(username));

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));

        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        driver.navigate().refresh();

        Assertions.assertTrue(notesPage.isNoteVisible(title));

        notesPage.clickOnNote(note.getId())
                .addNoteTitle("fwdwdw")
                .addNoteContent("dmcinc")
                .clickSaveNote();

        Assertions.assertTrue(notesPage.isNoteVisible("fwdwdw"));
    }

    @Test
    void doesntLetDupTitlesAtEdit(){
        String title = "title";
        String content = "content";
        Note note = new Note();
        note.setContent(content);
        note.setTitle(title);

        String userName = username;

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> userRepository.existsByUsername(username));

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));

        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        String title2 = "title2";
        String content2 = "content2";
        Note note2 = new Note();
        note2.setContent(content2);
        note2.setTitle(title2);

        note2.setUser(user);

        Note savedNote2 = noteRepository.save(note2);

        notesPage.clickOnNote(note.getId())
                .addNoteTitle(title2)
                .addNoteContent("dmcinc")
                .clickSaveNote();

        Assertions.assertEquals("Note with title " + title2 + " already exists."
                , notesPage.getErrorForDupNote()
        );
    }

    @Test
    void shouldAllowToDelAcc(){
        LoginPage loginPage2 = notesPage.pressAccountSettingsDropdown()
                .pressDeleteAccount()
                .confirmAccountDeletion();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !userRepository.existsByUsername(username));

        LoginPage errorPage = loginPage2
                .chooseSignIn()
                .enterUsername(username)
                .enterPassword(password)
                .submitExpectingFailure();

        Assertions.assertEquals("Request failed (401)", errorPage.getErrorMessage());
    }

    @Test
    void shouldBeAbleToSignOut() {
        LoginPage loginPage2 = notesPage.signOut();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> userRepository.existsByUsername(username));

        Assertions.assertTrue(loginPage2.existsUsernameField());
    }

    @Test
    void shouldBeAbleToChangeAccountDetails(){
        String newName = "e2e" + UUID.randomUUID();
        String newPassword = "password2";

        notesPage.pressAccountSettingsDropdown()
                .pressChangeAccountUsername(newName)
                .pressChangeAccountPassword(newPassword)
                .pressSaveAccountChanges();

        Assertions.assertTrue(notesPage.hasGreetingText("Hi, " + newName));
    }


}
