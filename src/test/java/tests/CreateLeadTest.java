package tests;

import java.util.Map;

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
		
		// Append to company for unique value
	    leadData.setCompany(leadData.getCompany() + " " + uniqueNum);
	}


	@Test(priority = 1)
	public void testLeadCreate() {
		
		// Step 1: Login
		SalesforceLoginPage sfLogin = new SalesforceLoginPage(driver);
		sfLogin.login();
		
		// Step 2: Create lead with AI-generated data
		LeadCreationPage leadCreationPage = new LeadCreationPage(driver);
		leadCreationPage.createLead(leadData); //pass the AI  data
		leadCreationPage.saveLeadRecord();
		
		// Step 3: Verify
		 boolean isCreated = leadCreationPage.isLeadCreatedSuccessfully(leadData.getFullName());
	     String recordId   = leadCreationPage.getCreatedLeadId();
	     
	     System.out.println("Lead created: "+isCreated+" "+recordId);
		Assert.assertTrue(isCreated, "Lead "+leadData.getFullName()+" was not created.");
	}
	
	@Test(priority = 2, dependsOnMethods = "testLeadCreate")
	public void fetchLeadsAndGenerateReport() {
		
		// Step 1: Navigate to All Leads list view
		LeadsListViewPage leadsListView = new LeadsListViewPage(driver);
		leadsListView.openAllLeadsListView();
		
		// Step 2: Get status counts and total
		Map<String,Integer> statusCounts = leadsListView.getLeadStatusCounts();
		int total = leadsListView.getTotalLeadsCount();
		
		// Step 3: Print Lead distribution to console
		System.out.println("=== Lead Distribution Report ===");
		System.out.println("Total Leads: " + total);
		for(Map.Entry<String, Integer> entry: statusCounts.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		
		// Step 4: Generate AI summary and print it
		String aiSummary = aiClient.generateLeadSummary(total, statusCounts);
		System.out.println("\n=== AI Summary ===");
	    System.out.println(aiSummary);
	    
	 // Step 5: Assert at least one lead exists
	    Assert.assertTrue(total > 0, "No leads found in the All Open Leads list view.");
		
	}

}
