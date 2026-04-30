package pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;
import utils.WaitUtils;

import java.time.Duration;

public class SalesforceLoginPage {

    private static final Logger log = LogManager.getLogger(SalesforceLoginPage.class);

    private WebDriver driver;

    private By usernameField = By.id("username");
    private By passwordField = By.id("password");
    private By loginButton   = By.id("Login");

    public SalesforceLoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void login() {
    	
    	log.info("Navigating to Salesforce login page");
        driver.get(ConfigReader.get("sf_url"));
        
        log.info("Logging in as: {}", ConfigReader.get("sf_username"));

        WaitUtils.waitAndType(driver, usernameField, ConfigReader.get("sf_username"));
        WaitUtils.waitAndType(driver, passwordField, ConfigReader.get("sf_password"));
        WaitUtils.waitAndClick(driver, loginButton);

        // Wait until redirected to Lightning after login
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.urlContains("/lightning/"));

        log.info("Login successful — redirected to Lightning");
    }
}
