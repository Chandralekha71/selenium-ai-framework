package base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import utils.ConfigReader;

public class BaseTest {

	private static final Logger log = LogManager.getLogger(BaseTest.class);

	protected WebDriver driver;

	@BeforeClass
	public void setUp() {
		log.info("Setting up Chrome browser");

		// Build ChromeOptions FIRST before creating the driver
		ChromeOptions opts = new ChromeOptions();
		//opts.setPageLoadStrategy(PageLoadStrategy.EAGER);  // return early when DOM is ready

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

	@AfterClass
	public void tearDown() {
		if (driver != null) {
			log.info("Closing browser");
			driver.quit();
		}
	}

}
