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

	// Recursive JS script to pierce nested shadow roots to find a single element
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
	public void resetConversation() {
		log.info("Checking for existing Agentforce conversation to reset");
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;

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

	// Type message and send via Enter, then wait for Agentforce to finish responding.
	// Textarea selection via placeholder
	public void sendMessage(String message) {
		log.info("Sending message: {}", message);
		WebElement textarea = deepFind("[placeholder='Type your message...']");
		if (textarea.getSize().getWidth() == 0) {
			log.info("Chat modal not open — using main page input");
			textarea = deepFind("[placeholder='Ask Agentforce']");
		} else {
			log.info("Chat modal is open — using modal textarea");
		}
		new Actions(driver).click(textarea).sendKeys(message).sendKeys(Keys.RETURN).perform();
		log.info("Message sent successfully");

		// Wait for typing indicator to appear (Agentforce started generating)
		deepFind("[class*=\"typing-ind\"]");
		log.info("Agentforce is typing...");

		// Wait for typing indicator to disappear 
		new WebDriverWait(driver, Duration.ofSeconds(30))
			.until(d -> (Boolean) ((JavascriptExecutor) d).executeScript("""
				function deepFind(root, selector) {
				  let el = root.querySelector(selector);
				  if (el) return el;
				  for (let child of root.querySelectorAll('*')) {
				    if (child.shadowRoot) { el = deepFind(child.shadowRoot, selector); if (el) return el; }
				  }
				  return null;
				}
				return deepFind(document, '[class*="typing-ind"]') === null;
				"""));
		log.info("Agentforce finished responding");
	}

	// Returns the last Agent message text 
	public String getAgentResponse() {
		log.info("Fetching Agentforce response...");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String response = (String) js.executeScript("""
			function deepFindAll(root, selector, results = []) {
			  root.querySelectorAll(selector).forEach(el => results.push(el));
			  for (let child of root.querySelectorAll('*')) {
			    if (child.shadowRoot) deepFindAll(child.shadowRoot, selector, results);
			  }
			  return results;
			}
			let msgs = deepFindAll(document, '.bot-message [data-testid="message"]');
			return msgs.length > 0 ? msgs[msgs.length - 1].innerText : null;
			""");
		log.info("Agentforce response received: {}", response);
		return response;
	}

}