package tests;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ai.OpenAIClient;
import base.BaseTest;
import models.LeadData;
import pages.LeadCreationPage;
import pages.LeadsListViewPage;
import pages.SalesforceLoginPage;

public class CreateLeadTest extends BaseTest {

	private static final Logger log = LogManager.getLogger(CreateLeadTest.class);

	private OpenAIClient aiClient;
	private LeadData leadData;

	@BeforeClass
	public void initAI() {
		aiClient = new OpenAIClient();
		leadData = aiClient.generateLeadData(); // call OpenAI ONCE before test runs

		// Generate a random 5-digit number
		int uniqueNum = (int)(Math.random() * 9000) + 10000;

		// Append to email for unique value
		leadData.setEmail(leadData.getEmail().replace("@", uniqueNum+"@"));
		log.info("Lead data ready — name: {}, email: {}", leadData.getFullName(), leadData.getEmail());

		// Append to company for unique value
	    //leadData.setCompany(leadData.getCompany() + " " + uniqueNum);
	}

	@Test(priority = 1)
	public void testLeadCreate() {
		log.info("=== Starting testLeadCreate ===");

		// Step 1: Login
		SalesforceLoginPage sfLogin = new SalesforceLoginPage(driver);
		sfLogin.login();

		// Step 2: Create lead with AI-generated data
		LeadCreationPage leadCreationPage = new LeadCreationPage(driver);
		leadCreationPage.createLead(leadData);
		leadCreationPage.saveLeadRecord();

		// Step 3: Verify
		boolean isCreated = leadCreationPage.isLeadCreatedSuccessfully(leadData.getFullName());
		String recordId   = leadCreationPage.getCreatedLeadId();

		log.info("testLeadCreate result — created: {}, record ID: {}", isCreated, recordId);
		Assert.assertTrue(isCreated, "Lead " + leadData.getFullName() + " was not created.");
	}

	@Test(priority = 2, dependsOnMethods = "testLeadCreate")
	public void fetchLeadsAndGenerateReport() {
		log.info("=== Starting fetchLeadsAndGenerateReport ===");

		// Step 1: Navigate to All Leads list view
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
