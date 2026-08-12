package com.noteapptoy.noteapptoy;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By switchButton = By.id("createAccountTab");
    private final By loginButton =By.id("loginButton");
    private final By errorMessage = By.cssSelector("p.notice.error");
    private final By signInTab = By.id("signInTab");


    public LoginPage(WebDriver driver){
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }


    public LoginPage open() {
        driver.get("http://localhost:3000");
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        return this;
    }

    public LoginPage chooseCreateAccount() {
        driver.findElement(switchButton).click();
        return this;
    }

    public LoginPage enterUsername(String username) {
        driver.findElement(usernameInput).sendKeys(username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        driver.findElement(passwordInput).sendKeys(password);
        return this;
    }


    public NotesPage submitExpectingSuccess() {
        driver.findElement(loginButton).click();
        return new NotesPage(driver);
    }

    public LoginPage submitExpectingFailure() {
        driver.findElement(loginButton).click();
        return this;
    }

    public String getErrorMessage() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(errorMessage)
        ).getText();
    }

    public LoginPage chooseSignIn() {
        driver.findElement(signInTab).click();
        return this;
    }

    public boolean existsUsernameField(){
        return driver.findElement(usernameInput).getText().isEmpty();
    }

    public WebDriverWait getWait(){
        return this.wait;
    }

}
