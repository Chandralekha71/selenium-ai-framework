package tests;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ai.OpenAIClient;
import base.BaseTest;
import pages.AgentforceChatPage;

public class AgentforceTest extends BaseTest {

	private AgentforceChatPage chatPage;
	private OpenAIClient ai;

	@BeforeClass
	public void init() {
		chatPage = new AgentforceChatPage(driver);
		ai = new OpenAIClient();
		chatPage.open();
	}

	// Scenario 1 — Greeting & Onboarding
	// Validates the agent acknowledges the greeting and describes its capability areas
	@Test(priority = 1)
	public void testGreetingAndOnboarding() {
		chatPage.sendMessage("Hi, what can you help me with?");
		String response = chatPage.getAgentResponse();
		boolean pass = ai.validateIntent(response,
			"Response must acknowledge the greeting AND describe at least 2 capability areas " +
			"such as troubleshooting, documentation search, or account help. Intent: capability overview.");
		Assert.assertTrue(pass, "Scenario 1 failed — agent did not provide a capability overview");
	}

	// Scenario 2 — Documentation / Feature Search
	// Validates the agent returns relevant guidance or a link for Flow Builder
	@Test(priority = 2)
	public void testDocumentationSearch() {
		chatPage.sendMessage("How do I set up Flow Builder in Salesforce?");
		String response = chatPage.getAgentResponse();
		boolean pass = ai.validateIntent(response,
			"Response should contain guidance or a link related to Flow Builder or Salesforce automation. " +
			"Intent: feature guidance with topic relevance to Flow Builder.");
		Assert.assertTrue(pass, "Scenario 2 failed — agent did not provide Flow Builder guidance");
	}

	// Scenario 3 — Troubleshooting / Error Handling
	// Validates the agent suggests at least one actionable fix for a login issue
	@Test(priority = 3)
	public void testTroubleshootingLoginIssue() {
		chatPage.sendMessage("I cannot log in to my Salesforce org. What should I do?");
		String response = chatPage.getAgentResponse();
		boolean pass = ai.validateIntent(response,
			"Response must suggest at least one actionable troubleshooting step such as password reset, " +
			"clearing browser cache, or checking MFA settings. Intent: troubleshooting steps provided.");
		Assert.assertTrue(pass, "Scenario 3 failed — agent did not provide troubleshooting steps");
	}

	// Scenario 4 — Out-of-Scope / Fallback Handling
	// Validates the agent declines or redirects rather than hallucinating capabilities
	@Test(priority = 4)
	public void testOutOfScopeFallback() {
		chatPage.sendMessage("Can you book me a flight to New York?");
		String response = chatPage.getAgentResponse();
		boolean pass = ai.validateIntent(response,
			"Response should gracefully decline or redirect the request. The agent must NOT promise " +
			"to book flights or perform unrelated actions. Intent: out of scope deflection — no hallucinated capabilities.");
		Assert.assertTrue(pass, "Scenario 4 failed — agent did not handle out-of-scope request correctly");
	}

	// Scenario 5 — Product Information Query
	// Validates the agent describes Agentforce as an AI-powered platform and highlights differentiation
	@Test(priority = 5)
	public void testProductInformationQuery() {
		chatPage.sendMessage("What is Agentforce and how is it different from regular chatbots?");
		String response = chatPage.getAgentResponse();
		boolean pass = ai.validateIntent(response,
			"Response should describe Agentforce as an AI-powered agent platform and highlight " +
			"key differentiators such as reasoning or grounding. Intent: product description with key concept coverage.");
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
