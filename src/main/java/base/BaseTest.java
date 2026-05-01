package base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import utils.ConfigReader;

public class BaseTest {

	private static final Logger log = LogManager.getLogger(BaseTest.class);

	protected WebDriver driver;

	@BeforeClass
	public void setUp() {
		log.info("Setting up Chrome browser");

		// Build ChromeOptions before creating the driver
		ChromeOptions opts = new ChromeOptions();

		String profileDir = ConfigReader.get("chrome_profile_dir");

		if (profileDir != null && !profileDir.isBlank()) {
			// Resolve relative paths against the project root (where Maven runs from)
			java.io.File dir = new java.io.File(profileDir);
			String absolutePath = dir.isAbsolute() ? dir.getAbsolutePath()
					: new java.io.File(System.getProperty("user.dir"), profileDir).getAbsolutePath();
			opts.addArguments("--user-data-dir=" + absolutePath);
			log.info("Using Chrome profile at: {}", absolutePath);
		}

		// Pass opts into ChromeDriver so the profile is applied
		driver = new ChromeDriver(opts);
		driver.manage().window().maximize();

	}

	// Captures a screenshot when a test method fails and saves it to test-output/screenshots/
	@AfterMethod
	public void captureScreenshotOnFailure(ITestResult result) {
		if (result.getStatus() == ITestResult.FAILURE && driver != null) {
			try {
				File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				String fileName = result.getTestClass().getRealClass().getSimpleName()
						+ "_" + result.getMethod().getMethodName()
						+ "_" + System.currentTimeMillis() + ".png";
				File dest = new File("test-output/screenshots/" + fileName);
				dest.getParentFile().mkdirs();
				Files.copy(screenshot.toPath(), dest.toPath());
				log.info("Screenshot saved: {}", dest.getPath());
			} catch (IOException e) {
				log.warn("Failed to save screenshot for {}: {}", result.getMethod().getMethodName(), e.getMessage());
			}
		}
	}

	@AfterClass
	public void tearDown() {
		if (driver != null) {
			log.info("Closing browser");
			driver.quit();
		}
	}

}
