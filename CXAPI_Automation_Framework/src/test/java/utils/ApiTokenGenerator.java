package utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ApiTokenGenerator {

    public static String generateToken() {

        Response response =
                RestAssured
                        .given()
                        .contentType("application/json")
                        .body("""
                                    {
                                      "username": "%s",
                                      "password": "%s"
                                    }
                                """.formatted(
                                ConfigReader.get("api.username"),
                                ConfigReader.get("api.password")
                        ))
                        .post(ConfigReader.get("api.login.url"));

        String token = response.jsonPath().getString("token");

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("❌ API token generation failed");
        }

        System.out.println("✅ Token generated via API");
        return token;
    }
}
