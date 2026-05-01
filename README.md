Selenium AI Framework — Salesforce QA Automation
Overview
This framework built on the Page Object Model, automates two QA scenarios against a Salesforce org using Selenium WebDriver, TestNG, and OpenAI GPT-4o-mini. 

Task 1 — Lead Creation & Reporting: Generates realistic test data via AI, creates a Salesforce Lead record through the Lightning UI, extracts the record ID, then navigates to the All Open Leads list view and produces an AI-written Lead distribution summary.
Task 2 — Agentforce Chat Automation: Drives the Agentforce chat widget embedded in the Salesforce Help Portal (a shadow-DOM–heavy LWC component), sends five conversational scenarios, captures each agent response, and validates it against a plain-English intent description using an AI assertion.

Agentforce Intent-Validation Strategy
Traditional test assertions check whether a response contains a specific word or phrase. That approach breaks the moment the agent rephrases something, which conversational AI does constantly. So instead of asserting on exact text, I validate on meaning.
After each Agentforce scenario, the agent's full response and a plain-English description of what that response should mean are sent to GPT-4o-mini. The model acts as a QA tester reading the response and deciding whether it satisfies the intent — returning a simple JSON verdict with a pass/fail flag and a one-sentence reasoning. That reasoning is logged alongside the test result, so if something fails you immediately know why rather than just seeing a string mismatch.
The intent descriptions are written to reflect real-world agent behaviour. For example, if a valid deflection can be either explicit ("I can't help with that") or implicit ("Here's what I can help you with"), the description says so — otherwise the AI would fail a test that a human tester would pass. This makes the strategy resilient to phrasing variation while still catching genuinely wrong responses, and it means maintaining test intent is a one-line description change rather than a regex update.
