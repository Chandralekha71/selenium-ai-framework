package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;

import java.time.Duration;

public class SalesforceLoginPage {

    private WebDriver driver;

    private By usernameField = By.id("username");
    private By passwordField = By.id("password");
    private By loginButton   = By.id("Login");

    public SalesforceLoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void login() {


        // Wait for the login form to appear before interacting
//        new WebDriverWait(driver, Duration.ofSeconds(15))
//                .until(ExpectedConditions.visibilityOfElementLocated(usernameField));

        driver.findElement(usernameField).sendKeys(ConfigReader.get("sf_username"));
        driver.findElement(passwordField).sendKeys(ConfigReader.get("sf_password"));
        driver.findElement(loginButton).click();

        // Wait until redirected to Lightning after login
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.urlContains("/lightning/"));
    }
}
