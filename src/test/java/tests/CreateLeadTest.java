package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ai.OpenAIClient;
import base.BaseTest;
import models.LeadData;
import pages.LeadCreationPage;
import pages.SalesforceLoginPage;

public class CreateLeadTest extends BaseTest {
	
	private OpenAIClient aiClient;
	private LeadData leadData;
	
	@BeforeClass
	public void initAI() {
		aiClient = new OpenAIClient();
		leadData = aiClient.generateLeadData(); // call OpenAI ONCE before test runs
		
		// Generate a random 5-digit number — different every run
		int uniqueNum = (int)(Math.random() * 9000) + 10000;
		
		// Append to email
		leadData.setEmail(leadData.getEmail().replace("@", uniqueNum+"@"));
		
		// Append to company
	    leadData.setCompany(leadData.getCompany() + " " + uniqueNum);
	}


	@Test
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

}
