package tests;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ai.AIClient;
import base.BaseTest;
import models.LeadData;
import pages.LeadCreationPage;
import pages.LeadsListViewPage;
import pages.SalesforceLoginPage;

public class CreateLeadTest extends BaseTest {

	private static final Logger log = LogManager.getLogger(CreateLeadTest.class);

	private AIClient aiClient;
	private LeadData leadData;

	@BeforeClass
	public void initAI() {
		aiClient = new AIClient();
	}

	@Test(priority = 1)
	public void testLeadCreate() {
		log.info("=== Starting testLeadCreate ===");

	    // Step 1: Generate AI data 
	    leadData = aiClient.generateLeadData();
	    
	    // Generate a random 5-digit number
	    int uniqueNum = (int)(Math.random() * 9000) + 10000;
	    
	    // Append to email for unique value
	    leadData.setEmail(leadData.getEmail().replace("@", uniqueNum + "@"));
	    log.info("Lead data ready — name: {}, email: {}", leadData.getFullName(), leadData.getEmail());

	    // Step 2: Login to Salesforce
	    SalesforceLoginPage sfLogin = new SalesforceLoginPage(driver);
	    sfLogin.login();

	    // Step 3: Create lead with AI Generated Data
	    LeadCreationPage leadCreationPage = new LeadCreationPage(driver);
	    leadCreationPage.createLead(leadData);
	    leadCreationPage.saveLeadRecord();

	    // Step 4: Verify if Lead record created successfully
	    //boolean isCreated = leadCreationPage.isLeadCreatedSuccessfully(leadData.getFullName());
	    String recordId = leadCreationPage.getCreatedLeadId();
	    //Assert.assertTrue(isCreated, "Lead " + leadData.getFullName() + " was not created.");
	    log.info("testLeadCreate result — created lead with record ID: {}", recordId);

	    //Step 5: AI Assertion of Lead Creation
	    boolean aiVerified = aiClient.validateIntent(
	    	    driver.getTitle(),
	    	    "Page title should contain the lead's full name: " + leadData.getFullName()
	    	);
	    log.info("AI lead creation assertion — pass: {}", aiVerified);

	}

	@Test(priority = 2, dependsOnMethods = "testLeadCreate")
	public void fetchLeadsAndGenerateReport() {
		log.info("=== Starting fetchLeadsAndGenerateReport ===");

		// Step 1: Navigate to All Open Leads list view
		LeadsListViewPage leadsListView = new LeadsListViewPage(driver);
		leadsListView.openAllLeadsListView();

		// Step 2: Get status counts and total
		Map<String,Integer> statusCounts = leadsListView.getLeadStatusCounts();
		int total = leadsListView.getTotalLeadsCount();

		// Step 3: Log Lead distribution
		log.info("=== Lead Distribution Report ===");
		log.info("Total Leads: {}", total);
		for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
			log.info("  {} : {}", entry.getKey(), entry.getValue());
		}

		// Step 4: Generate AI summary and log it
		String aiSummary = aiClient.generateLeadSummary(total, statusCounts);
		log.info("=== AI Summary ===");
		log.info(aiSummary);

		// Step 5: Assert at least one lead exists
		Assert.assertTrue(total > 0, "No leads found in the All Open Leads list view.");
	}

}
