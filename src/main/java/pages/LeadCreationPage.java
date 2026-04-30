package pages;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import models.LeadData;
import utils.ConfigReader;
import utils.WaitUtils;

public class LeadCreationPage {

    private static final Logger log = LogManager.getLogger(LeadCreationPage.class);

    private WebDriver driver;

    private By firstName    = By.xpath("//input[@name='firstName']");
    private By lastName     = By.xpath("//input[@name='lastName']");
    private By company      = By.xpath("//input[@name='Company']");
    private By email        = By.xpath("//input[@name='Email']");
    private By phone        = By.xpath("//input[@name='Phone']");
    private By statusButton = By.xpath("//button[@aria-label='Lead Status']");
    private By saveButton   = By.xpath("//button[@name='SaveEdit']");
    private By modalHeader  = By.xpath("//h2[contains(@class,'title') and normalize-space()='New Lead']");

    public LeadCreationPage(WebDriver driver) {
        this.driver = driver;
    }

    private void setLeadValue(By locator, String value) {
        if (value == null || value.isBlank()) return;
        WaitUtils.waitAndType(driver, locator, value);
    }

    private void selectPicklistValue(By locator, String value) {
        WaitUtils.waitAndClick(driver, locator);
        By option = By.xpath("//lightning-base-combobox-item[@data-value='" + value + "']");
        WaitUtils.waitAndClick(driver, option);
    }

    public void createLead(LeadData data) {
        log.info("Navigating to New Lead form");
        driver.navigate().to(ConfigReader.get("new_lead_url"));
        WaitUtils.waitForVisibility(driver, modalHeader);

        log.info("Filling lead form for: {} at {}", data.getFullName(), data.getCompany());
        setLeadValue(firstName, data.getFirstName());
        setLeadValue(lastName, data.getLastName());
        setLeadValue(company, data.getCompany());
        setLeadValue(email, data.getEmail());
        setLeadValue(phone, data.getPhone());
        selectPicklistValue(statusButton, data.getLeadStatus());
    }

    public void saveLeadRecord() {
        log.info("Saving lead record");
        WaitUtils.waitAndClick(driver, saveButton);

        // Check if Salesforce shows a duplicate warning — if yes, save anyway
        try {
            WaitUtils.waitAndClick(driver, saveButton);
        } catch (Exception e) {
            // No duplicate warning appeared — normal save, do nothing
        }

        // Wait for navigation to the record detail page
        new WebDriverWait(driver, Duration.ofSeconds(15))
            .until(ExpectedConditions.urlContains("/r/"));
    }

    public boolean isLeadCreatedSuccessfully(String expectedName) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.titleContains(expectedName));
            log.info("Lead created successfully: {}", expectedName);
            return true;
        } catch (Exception e) {
            log.warn("Lead creation could not be verified for: {}", expectedName);
            return false;
        }
    }

    public String getCreatedLeadId() {
        String url = driver.getCurrentUrl();
        if (url.contains("/r/")) {
            String[] parts = url.split("/r/");
            if (parts.length > 1) {
                String recordId = parts[1].replace("/view", "").trim();
                log.info("Created lead record ID: {}", recordId);
                return recordId;
            }
        }
        log.warn("Could not extract record ID from URL: {}", url);
        return "UNKNOWN";
    }
 
}
