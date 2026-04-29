package base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import utils.ConfigReader;

public class BaseTest {
	
	protected WebDriver driver;
	
	@BeforeClass
	public void setUp() {
		// Build ChromeOptions FIRST before creating the driver
		ChromeOptions opts = new ChromeOptions();
		String profileDir = ConfigReader.get("chrome_profile_dir");
        if (profileDir != null && !profileDir.isBlank()) {
            // Resolve relative paths against the project root (where Maven runs from)
            java.io.File dir = new java.io.File(profileDir);
            String absolutePath = dir.isAbsolute() ? dir.getAbsolutePath()
                                                   : new java.io.File(System.getProperty("user.dir"), profileDir).getAbsolutePath();
            opts.addArguments("--user-data-dir=" + absolutePath);
        }

		// Pass opts into ChromeDriver so the profile is applied
		driver = new ChromeDriver(opts);
		driver.manage().window().maximize();
		
		// Applying page load timeout from config
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.pageLoadTimeout()));
		
		driver.get(ConfigReader.get("sf_url"));
	}
	
	@AfterClass
	public void tearDown() {
		//if driver is still open then quit the driver
		if(driver!=null) {
			driver.quit();
		}
		
	}

}
