package pages;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;

public class AgentforceChatPage {

	private static final Logger log = LogManager.getLogger(AgentforceChatPage.class);

	private WebDriver driver;

	// Snapshot of the last bot response text captured just before each sendMessage() call.
	// getAgentResponse() waits until this text changes, guaranteeing it returns the NEW
	// reply rather than an already-visible response from a previous turn.
	private String lastBotResponseText = "";

	// [1] DEEP_FIND_SCRIPT — used by deepFind() to locate a single element across all shadow roots
	private static final String DEEP_FIND_SCRIPT = """
		function deepFind(root, selector) {
		  let el = root.querySelector(selector);
		  if (el) return el;
		  for (let child of root.querySelectorAll('*')) {
		    if (child.shadowRoot) {
		      el = deepFind(child.shadowRoot, selector);
		      if (el) return el;
		    }
		  }
		  return null;
		}
		return deepFind(document, arguments[0]);
		""";


	public AgentforceChatPage(WebDriver driver) {
		this.driver = driver;
	}

	// Navigate to Salesforce Help Portal
	public void open() {
		driver.navigate().to(ConfigReader.get("help_portal_url"));
		log.info("Navigated to: {}", driver.getCurrentUrl());
	}

	// End any previous conversation via the three-dots menu
	// Guarded with try/catch — if no previous conversation exists, the menu
	// won't be present and we skip the reset silently
	public void resetConversation() {
		log.info("Checking for existing Agentforce conversation to reset");
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;

			// [2] resetConversation() — click options menu
			js.executeScript("""
				function deepFind(root, selector) {
				  let el = root.querySelector(selector);
				  if (el) return el;
				  for (let child of root.querySelectorAll('*')) {
				    if (child.shadowRoot) {
				      el = deepFind(child.shadowRoot, selector);
				      if (el) return el;
				    }
				  }
				  return null;
				}
				deepFind(document, '[aria-label="options menu"]').click();
				""");
			log.info("Options menu opened");

			// Small pause to allow dropdown animation to complete
			Thread.sleep(500);

			// [3] resetConversation() — click "End conversation" button
			js.executeScript("""
				function deepFind(root, selector) {
				  let el = root.querySelector(selector);
				  if (el) return el;
				  for (let child of root.querySelectorAll('*')) {
				    if (child.shadowRoot) {
				      el = deepFind(child.shadowRoot, selector);
				      if (el) return el;
				    }
				  }
				  return null;
				}
				deepFind(document, '.dropdown-item:last-child button').click();
				""");
			log.info("End conversation clicked — redirected back to help portal");

			deepFind("[placeholder='Ask Agentforce']");
			log.info("Conversation reset — widget ready for new session");
		} catch (Exception e) {
			log.info("No existing conversation found — widget already in fresh state");
		}
	}

	// Find an element by piercing through all nested shadow roots (waits up to 20s)
	private WebElement deepFind(String cssSelector) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return new WebDriverWait(driver, Duration.ofSeconds(20))
			.until(d -> (WebElement) js.executeScript(DEEP_FIND_SCRIPT, cssSelector));
	}


	// [placeholder='Type your message...'] width > 0 → chat modal is open, use modal textarea
	// [placeholder='Type your message...'] width = 0 → chat modal not open, fall back to main page input
	public void sendMessage(String message) {
		log.info("Sending message: {}", message);
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// [4] sendMessage() — snapshot last bot response text before sending
		Object captured = js.executeScript("""
			function deepFindAll(root, selector) {
			  let results = Array.from(root.querySelectorAll(selector));
			  for (let child of root.querySelectorAll('*')) {
			    if (child.shadowRoot) results = results.concat(deepFindAll(child.shadowRoot, selector));
			  }
			  return results;
			}
			let msgs = deepFindAll(document, '.bot-message [data-testid="message"]');
			return msgs.length > 0 ? msgs[msgs.length - 1].innerText : '';
			""");
		lastBotResponseText = (captured != null) ? (String) captured : "";
		log.info("Snapshot before send ({}chars)", lastBotResponseText.length());

		WebElement textarea = deepFind("[placeholder='Type your message...']");
		if (textarea.getSize().getWidth() == 0) {
			log.info("Chat modal not open — using main page input");
			textarea = deepFind("[placeholder='Ask Agentforce']");
		} else {
			log.info("Chat modal is open — using modal textarea");
		}
		new Actions(driver).click(textarea).sendKeys(message).sendKeys(Keys.RETURN).perform();
		log.info("Message sent successfully");
	}

	// Wait for and return the Agentforce response to the most recently sent message.
	// Passes the pre-send text snapshot as arguments[0] so JS can compare without
	// escaping issues. Returns only when the last bot message text has changed.
	public String getAgentResponse() {
		log.info("Waiting for new Agentforce response (snapshot: {}chars)...", lastBotResponseText.length());
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// [5] getAgentResponse() — wait for last bot message text to differ from snapshot
		String script = """
			function deepFindAll(root, selector) {
			  let results = Array.from(root.querySelectorAll(selector));
			  for (let child of root.querySelectorAll('*')) {
			    if (child.shadowRoot) results = results.concat(deepFindAll(child.shadowRoot, selector));
			  }
			  return results;
			}
			let msgs = deepFindAll(document, '.bot-message [data-testid="message"]');
			let last = msgs.length > 0 ? msgs[msgs.length - 1].innerText : null;
			return (last && last !== arguments[0]) ? last : null;
			""";

		String response = (String) new WebDriverWait(driver, Duration.ofSeconds(30))
			.until(d -> (String) js.executeScript(script, lastBotResponseText));

		lastBotResponseText = response;
		log.info("Agentforce response received: {}", response);
		return response;
	}

}
