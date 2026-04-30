package ai;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import models.LeadData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.ConfigReader;

public class OpenAIClient {

	private static final Logger log = LogManager.getLogger(OpenAIClient.class);

	private static final String API_URL = "https://api.openai.com/v1/chat/completions";
	// This request body contains JSON data encoded in UTF-8
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	private final OkHttpClient http;
	private final String model;

	public OpenAIClient() {

		this.model = ConfigReader.get("openai_model");
		this.http = new OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				.build();
	}

	public LeadData generateLeadData() {
		log.info("Calling OpenAI to generate lead data");

		String prompt = """
		        Generate realistic test data for a Salesforce Lead record.
		        Return ONLY a valid JSON object with exactly these fields:
		        {
		          "firstName": "string (a realistic first name, e.g. Sarah, James, Priya)",
		          "lastName": "string (a realistic last name, e.g. Johnson, Patel, Williams)",
		          "company": "string (a realistic company name, e.g. Acme Corp, BlueSky Solutions)",
		          "email": "string (email using the company domain, e.g. sarah@acmecorp.com)",
		          "phone": "string (US format: +1-XXX-XXX-XXXX)",
		          "leadStatus": "Working - Contacted"
		        }
		        No explanation, markdown, or text outside the JSON object.
		        """;

		String raw = callAPI(prompt, 200);

		// Remove markdown fences if model accidentally wraps the JSON
		String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();

		JsonObject j = JsonParser.parseString(cleaned).getAsJsonObject();

		//Reading the values mapped to the field names from JsonObject
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
	
	public String generateLeadSummary(int totalCount, Map<String,Integer> statusCounts) {
		log.info("Calling OpenAI for lead distribution summary");

		// Build a plain text breakdown of the status counts
		String breakdown = "";
		for(Map.Entry<String,Integer> entry : statusCounts.entrySet()) {
			breakdown = breakdown + " - " + entry.getKey() + " : " + entry.getValue() + "\n";
		}
		String prompt = """
	            You are a QA reporting assistant.
	            Write a concise 2-3 sentence paragraph summarising this Salesforce Lead distribution.
	            Mention the total count and the most common status.
	            Write only the paragraph — no headings or bullet points.

	            Total Leads: %d
	            Breakdown:
	            %s
	            """.formatted(totalCount, breakdown);
		
		return callAPI(prompt, 200).trim();
	}

	// Pass the agent's response text and a plain-English intent description to OpenAI.
	// Returns true if the AI confirms the response satisfies the intent, false otherwise.
	// The AI's reasoning is logged so it appears in the test report.
	public boolean validateIntent(String agentResponse, String expectedIntent) {
		log.info("Validating intent: {}", expectedIntent);

		String prompt = """
		        You are a QA validation assistant evaluating a chatbot response.

		        Expected intent: %s

		        Chatbot response:
		        \"\"\"%s\"\"\"

		        Does the chatbot response satisfy the expected intent?
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

	private String callAPI(String userPrompt, int maxTokens) {
		// Build the request body as JSON
		JsonObject body = new JsonObject();
		body.addProperty("model", model);
		body.addProperty("max_tokens", maxTokens);

		JsonObject msg = new JsonObject();
		msg.addProperty("role", "user");
		msg.addProperty("content", userPrompt);

		JsonArray messages = new JsonArray();
		messages.add(msg);
		body.add("messages", messages);

		// Build and send the HTTP request
		Request request = new Request.Builder()
				.url(API_URL)
				.header("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
				.header("Content-Type", "application/json")
				.post(RequestBody.create(body.toString(), JSON))
				.build();

		try (Response response = http.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				String errorBody = response.body().string();
				log.error("OpenAI API error: HTTP {} — {}", response.code(), errorBody);
				throw new RuntimeException("OpenAI API error: HTTP " + response.code() + " — " + errorBody);
			}

			// Extract the text from choices[0].message.content
			return JsonParser.parseString(response.body().string())
					.getAsJsonObject()
					.getAsJsonArray("choices").get(0)
					.getAsJsonObject().get("message")
					.getAsJsonObject().get("content")
					.getAsString();

		} catch (IOException e) {
			log.error("OpenAI API call failed: {}", e.getMessage());
			throw new RuntimeException("OpenAI call failed: " + e.getMessage(), e);
		}

	}

}
