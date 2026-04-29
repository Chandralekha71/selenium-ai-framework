package pages;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;
import utils.WaitUtils;

public class LeadsListViewPage {
	
	private WebDriver driver;
	
	public LeadsListViewPage(WebDriver driver) {
        this.driver = driver;
    }
	
    // Locator for every Lead Status cell in the table
    private By statusCells = By.xpath("//td[@data-label='Lead Status']");
    
    // Scrollable container of the list view
    private By listContainer = By.xpath("//div[contains(@class,'slds-scrollable_y')]");

    // Every data row in the table
    private By rowsLocator = By.xpath("//table[contains(@class,'slds-table')]//tbody/tr");
	
	public void openAllLeadsListView() {
		
		driver.navigate().to(ConfigReader.get("leads_list_view_url"));
		
	    // Wait for the list container to be visible
	    WebElement container = WaitUtils.waitForVisibility(driver, listContainer);
	    
	    JavascriptExecutor js = (JavascriptExecutor) driver;
	    int previousCount = 0;
	    
	    while(true) {
	    	
	    	List<WebElement> currentRows = driver.findElements(rowsLocator);
	    	if(previousCount == currentRows.size()) {
	    		break;
	    	}
	    	previousCount = currentRows.size();
	    	
	    	js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight", container);
	    	
	    	try {
	    		new WebDriverWait(driver, Duration.ofSeconds(3))
	    		.until(ExpectedConditions.numberOfElementsToBeMoreThan(rowsLocator, previousCount));
	    	}catch(Exception e){
	    		break; // no new rows appeared — all rows loaded
	    	}
	    	
	    }
	    
	}	
	
	public Map<String, Integer> getLeadStatusCounts(){
		
		Map<String, Integer> statusCounts = new HashMap<String, Integer>();
		
        // All Lead Status cells currently visible on the page
		List<WebElement> cells = driver.findElements(statusCells);
		
		
		for(WebElement cell : cells) {
			String status = cell.getText().trim();
			
			if(!status.isEmpty()) {
				// If status already in map, increment count by 1
                // If not in map, add it with count 1
				if(statusCounts.containsKey(status)) {
					statusCounts.put(status, statusCounts.get(status)+1);
				}else {
					statusCounts.put(status, 1);
				}
			}
		}
		
		return statusCounts;		
		
	}
	
	public int getTotalLeadsCount() {
		return driver.findElements(statusCells).size();
	}
	
	
	

}
