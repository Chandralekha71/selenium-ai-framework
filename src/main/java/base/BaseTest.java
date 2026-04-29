package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import utils.ConfigReader;

public class BaseTest {
	
	protected WebDriver driver;
	
	@BeforeMethod
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
		driver.get(ConfigReader.get("sf_url"));
	}
	
	@AfterMethod
	public void tearDown() {
		//if driver is still open then quit the driver
//		if(driver!=null) {
//			driver.quit();
//		}
		
	}

}
