package utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class SlackNotifier {

    public static void sendMessage(String message) {

        if (!"true".equalsIgnoreCase(ConfigReader.get("slack.enabled"))) {
            return;
        }

        String webhookUrl = ConfigReader.get("slack.webhook.url");

        String payload = "{ \"text\": \"" + message + "\" }";

        try {
            RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                    .post(webhookUrl);

        } catch (Exception e) {
            System.out.println("⚠️ Slack notification failed: " + e.getMessage());
        }
    }
}
