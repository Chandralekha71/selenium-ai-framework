package utils;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitUtils {

	private static final Logger log = LogManager.getLogger(WaitUtils.class);

	private static final int DEFAULT_TIMEOUT = ConfigReader.elementWaitTimeout();

	private WaitUtils() {}

	public static WebDriverWait defaultWait(WebDriver driver) {
		return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
	}

	public static WebElement waitForVisibility(WebDriver driver, By locator) {
		log.debug("Waiting for visibility of: {}", locator);
		return defaultWait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	public static WebElement waitForClickability(WebDriver driver, By locator) {
		log.debug("Waiting for clickability of: {}", locator);
		return defaultWait(driver).until(ExpectedConditions.elementToBeClickable(locator));
	}

	public static void waitAndType(WebDriver driver, By locator, String value) {
		WebElement element = waitForVisibility(driver, locator);
		log.debug("Typing '{}' into: {}", value, locator);
		element.sendKeys(value);
	}

	public static void waitAndClick(WebDriver driver, By locator) {
		WebElement element = waitForClickability(driver, locator);
		try {
			element.click();
			log.debug("Clicked: {}", locator);
		} catch (ElementClickInterceptedException e) {
			log.warn("Direct click intercepted — falling back to JS click for: {}", locator);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		}
	}
}
