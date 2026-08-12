package com.noteapptoy.noteapptoy;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NotesPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By greetingText = By.id("greeting");
    private final By newNoteButton = By.id("newNoteButton");
    private final By notesList = By.id("notesList");

    private final By noteTitleInputField = By.id("noteTitleInput");
    private final  By noteContentInputField = By.id("noteContentInput");
    private final By deleteNoteButton = By.id("deleteNoteButton");
    private final By saveNoteButton = By.id("saveNoteButton");

    private final By accountSettingToggle = By.cssSelector("#accountSettings summary");
    private  final By accountUsernameInput = By.id("accountUsernameInput");
    private final By accountPasswordInput = By.id("accountPasswordInput");
    private final By saveAccountButton = By.id("saveAccountButton");
    private  final By deleteAccountButton = By.id("deleteAccountButton");
    private final By signOutButton = By.id("signOutButton");

    private final By errorMessage = By.cssSelector("p.notice.error");

    public NotesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getGreetingText() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(greetingText)
        ).getText();
    }

    private By noteButton(long noteId) {
        return By.id("note-" + noteId);
    }

    public NotesPage clickNewNoteButton() {
        wait.until(
                ExpectedConditions.elementToBeClickable(newNoteButton)
        ).click();

        return this;
    }

    public NotesPage addNoteTitle(String title) {
        WebElement input = driver.findElement(noteTitleInputField);

        input.clear();
        input.sendKeys(title);

        return this;
    }

    public NotesPage addNoteContent(String content) {
        WebElement input =  driver.findElement(noteContentInputField);

        input.clear();
        input.sendKeys(content);

        return this;
    }

    public NotesPage clickSaveNote(){
        driver.findElement(saveNoteButton).click();

        return this;
    }

    public NotesPage clickOnNote(Long noteId) {
        wait.until(
                ExpectedConditions.elementToBeClickable(noteButton(noteId))
        ).click();

        return this;
    }

    public NotesPage clickDeleteNote() {
        driver.findElement(deleteNoteButton).click();
        return this;
    }

    public NotesPage pressDeleteAccount() {
        driver.findElement(deleteAccountButton).click();
        return this;
    }

    public LoginPage confirmAccountDeletion(){
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("username"))
        );

        return new LoginPage(driver);
    }
    public NotesPage confirmNoteDeletion(){
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        return this;
    }

    public LoginPage signOut() {
        wait.until(
                ExpectedConditions.elementToBeClickable(signOutButton)
        ).click();

        return new LoginPage(driver);
    }

    public NotesPage pressAccountSettingsDropdown(){
        wait.until(
                ExpectedConditions.elementToBeClickable(accountSettingToggle)
        ).click();

        return this;
    }

    public NotesPage pressChangeAccountUsername(String newName){
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(accountUsernameInput)
                //driver.findElement(accountUsernameInput)
        );

        input.clear();
        input.sendKeys(newName);

        return this;
    }

    public NotesPage pressChangeAccountPassword(String newPass){
        wait.until(
                ExpectedConditions.elementToBeClickable(accountPasswordInput)
                //driver.findElement(accountPasswordInput).sendKeys(newPass)
        ).sendKeys(newPass);
        return this;
    }

    public NotesPage pressSaveAccountChanges(){
        wait.until(
                ExpectedConditions.elementToBeClickable(saveAccountButton)
                //driver.findElement(saveAccountButton).click()
        ).click();

        return this;
    }

    private By noteByTitle(String title) {
        return By.cssSelector("[data-note-title='" + title + "']");
    }

    public boolean isNoteVisible(String title) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(noteByTitle(title))
        ).isDisplayed();
    }

    public boolean isNoteGone(String title) {
        return wait.until(
                ExpectedConditions.invisibilityOfElementLocated(noteByTitle(title))
        );
    }

    public String getUserName(){
        return driver.findElement(accountUsernameInput).getText();
    }

    public String getErrorForDupNote() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public boolean hasGreetingText(String greeting){
        return wait.until(ExpectedConditions.textToBe(greetingText, greeting));
    }

}
