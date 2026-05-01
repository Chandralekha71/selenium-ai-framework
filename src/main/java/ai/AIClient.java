package ai;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionCreateParams.Builder;

import models.LeadData;
import utils.ConfigReader;

public class AIClient {

	private static final Logger log = LogManager.getLogger(AIClient.class);

	private final OpenAIClient client;
	private final String model;

	public AIClient() {
		this.model = ConfigReader.get("openai_model");
		// SDK handles auth, retries, and HTTP transport internally
		this.client = OpenAIOkHttpClient.builder()
				.apiKey(System.getenv("OPENAI_API_KEY"))
				.build();
	}

	public LeadData generateLeadData() {
		log.info("Calling OpenAI SDK to generate lead data");

		String prompt = """
		        Generate realistic test data for a Salesforce Lead record.
		        Return ONLY a valid JSON object with exactly these fields:
		        {
		          "firstName": "string (a realistic first name, e.g. Sarah, James, Priya)",
		          "lastName": "string (a realistic last name, e.g. Johnson, Patel, Williams)",
		          "company": "string (a realistic company name, e.g. Acme Corp, BlueSky Solutions)",
		          "email": "string (email using the company domain, e.g. sarah@acmecorp.com)",
		          "phone": "string (US format: +1-XXX-XXX-XXXX)",
		          "leadStatus": "Open - Not Contacted"
		        }
		        No explanation, markdown, or text outside the JSON object.
		        """;

		String raw = callAPI(prompt, 200);

		// Remove markdown fences if model wraps the JSON
		String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();

		JsonObject j = JsonParser.parseString(cleaned).getAsJsonObject();

		// Reading the values mapped to the field names from JsonObject
		LeadData leadData = new LeadData.Builder()
				.firstName(j.get("firstName").getAsString())
				.lastName(j.get("lastName").getAsString())
				.company(j.get("company").getAsString())
				.email(j.get("email").getAsString())
				.phone(j.get("phone").getAsString())
				.leadStatus(j.get("leadStatus").getAsString())
				.build();

		log.info("AI generated lead: {}", leadData.getFullName());
		return leadData;
	}

	public String generateLeadSummary(int totalCount, Map<String, Integer> statusCounts) {
		log.info("Calling OpenAI SDK for lead distribution summary");

		// Build a plain text breakdown of the status counts
		String breakdown = "";
		for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
			breakdown = breakdown + " - " + entry.getKey() + " : " + entry.getValue() + "\n";
		}

		String prompt = """
		        You are a QA tester reviewing Salesforce data as part of a test execution report.
		        Write a concise 2-3 sentence paragraph summarising this Salesforce Lead distribution.
		        Mention the total count and the most common status.
		        Write only the paragraph — no headings or bullet points.

		        Total Leads: %d
		        Breakdown:
		        %s
		        """.formatted(totalCount, breakdown);

		return callAPI(prompt, 200).trim();
	}

	// Passes the agent's response text and a plain-English intent description to OpenAI.
	// Returns true if the AI confirms the response satisfies the intent, false otherwise.
	// The AI's reasoning is logged in the test report.
	public boolean validateIntent(String agentResponse, String expectedIntent) {
		log.info("Validating intent: {}", expectedIntent);

		String prompt = """
		        You are a QA tester evaluating a conversational AI agent response.

		        Expected intent: %s

		        Agent response:
		        \"\"\"%s\"\"\"

		        Does the agent response satisfy the expected intent?
		        Reply with a JSON object in exactly this format (no markdown, no extra text):
		        {"pass": true, "reasoning": "one sentence explanation"}
		        or
		        {"pass": false, "reasoning": "one sentence explanation"}
		        """.formatted(expectedIntent, agentResponse);

		String raw = callAPI(prompt, 150).replaceAll("```json", "").replaceAll("```", "").trim();
		JsonObject result = JsonParser.parseString(raw).getAsJsonObject();

		boolean pass = result.get("pass").getAsBoolean();
		String reasoning = result.get("reasoning").getAsString();

		log.info("Intent validation — pass: {} | reasoning: {}", pass, reasoning);
		return pass;
	}

	// Sends a prompt to the OpenAI API via Java SDK and returns the response text.
	// The SDK handles authentication, HTTP transport, and error handling internally.
	private String callAPI(String userPrompt, int maxTokens) {
		Builder params = ChatCompletionCreateParams.builder()
				.model(model)
				.maxCompletionTokens(maxTokens)
				.addUserMessage(userPrompt);

		ChatCompletion completion = client.chat().completions().create(params.build());

		return completion.choices().get(0).message().content().orElseThrow(
				() -> new RuntimeException("OpenAI SDK returned empty content"));
	}

}
