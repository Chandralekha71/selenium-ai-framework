package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ai.OpenAIClient;
import base.BaseTest;
import pages.AgentforceChatPage;

public class AgentforceTest extends BaseTest {

	private static final Logger log = LogManager.getLogger(AgentforceTest.class);

	private AgentforceChatPage chatPage;
	private OpenAIClient ai;

	@BeforeClass
	public void init() {
		chatPage = new AgentforceChatPage(driver);
		ai = new OpenAIClient();
		chatPage.open();
	}

	// Appends a scenario name and agent response to test-output/agent-responses.txt as evidence
	private void logAgentResponse(String scenario, String response) {
		try {
			Path file = Path.of("test-output/agent-responses.txt");
			Files.createDirectories(file.getParent());
			String entry = "=== " + scenario + " ===\n" + response + "\n\n";
			Files.writeString(file, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.warn("Could not write agent response to file: {}", e.getMessage());
		}
	}

	// Scenario 1 — Greeting & Onboarding
	// Validates the agent acknowledges the greeting and offers to help with Salesforce-related questions
	@Test(priority = 1)
	public void testGreetingAndOnboarding() {
		chatPage.sendMessage("Hi, what can you help me with?");
		String response = chatPage.getAgentResponse();
		logAgentResponse("Scenario 1 — Greeting & Onboarding", response);
		boolean pass = ai.validateIntent(response, """
			Response should acknowledge the greeting and offer to help with Salesforce-related questions.
			A general offer to assist with Salesforce support is sufficient.
			Intent: greeting acknowledged with offer to help.
			""");
		Assert.assertTrue(pass, "Scenario 1 failed — agent did not provide a capability overview");
	}

	// Scenario 2 — Documentation / Feature Search
	// Validates the agent returns relevant guidance or a link for Flow Builder
	@Test(priority = 2)
	public void testDocumentationSearch() {
		chatPage.sendMessage("How do I set up Flow Builder in Salesforce?");
		String response = chatPage.getAgentResponse();
		logAgentResponse("Scenario 2 — Documentation / Feature Search", response);
		boolean pass = ai.validateIntent(response, """
			Response should contain guidance or a link related to Flow Builder or Salesforce automation.
			Intent: feature guidance with topic relevance to Flow Builder.
			""");
		Assert.assertTrue(pass, "Scenario 2 failed — agent did not provide Flow Builder guidance");
	}

	// Scenario 3 — Troubleshooting / Error Handling
	// Validates the agent suggests at least one actionable fix for a login issue
	@Test(priority = 3)
	public void testTroubleshootingLoginIssue() {
		chatPage.sendMessage("I cannot log in to my Salesforce org. What should I do?");
		String response = chatPage.getAgentResponse();
		logAgentResponse("Scenario 3 — Troubleshooting / Error Handling", response);
		boolean pass = ai.validateIntent(response, """
			Response must suggest at least one actionable troubleshooting step
			such as password reset, clearing browser cache, or checking MFA settings.
			Intent: troubleshooting steps provided.
			""");
		Assert.assertTrue(pass, "Scenario 3 failed — agent did not provide troubleshooting steps");
	}

	// Scenario 4 — Out-of-Scope / Fallback Handling
	// Validates the agent declines or redirects rather than hallucinating capabilities
	@Test(priority = 4)
	public void testOutOfScopeFallback() {
		chatPage.sendMessage("Can you book me a flight to New York?");
		String response = chatPage.getAgentResponse();
		logAgentResponse("Scenario 4 — Out-of-Scope / Fallback Handling", response);
		boolean pass = ai.validateIntent(response, """
			Response should deflect the out-of-scope request by redirecting to what the agent CAN help with
			(e.g. Salesforce support, products, or features).
			The agent does NOT need to explicitly say 'I cannot book flights' —
			redirecting to its own capabilities counts as a valid deflection.
			The agent must NOT offer to book flights or perform any unrelated action.
			Intent: out of scope deflection with no hallucinated capabilities.
			""");
		Assert.assertTrue(pass, "Scenario 4 failed — agent did not handle out-of-scope request correctly");
	}

	// Scenario 5 — Product Information Query
	// Validates the agent describes Agentforce as an AI-powered platform and highlights differentiation
	@Test(priority = 5)
	public void testProductInformationQuery() {
		chatPage.sendMessage("What is Agentforce and how is it different from regular chatbots?");
		String response = chatPage.getAgentResponse();
		logAgentResponse("Scenario 5 — Product Information Query", response);
		boolean pass = ai.validateIntent(response, """
			Response should describe Agentforce as an AI-powered agent platform and highlight
			key differentiators such as advanced AI capabilities, integration, low-code setup, or security.
			Intent: product description with key concept coverage.
			""");
		Assert.assertTrue(pass, "Scenario 5 failed — agent did not provide an adequate product description");
	}

	// Reset the conversation after all tests complete so the next run starts clean
	@AfterClass
	public void cleanup() {
		if (chatPage != null) {
			try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
			chatPage.resetConversation();
		}
	}

}
