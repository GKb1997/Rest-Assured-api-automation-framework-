package client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiClient {

    public static Response executeWithRetry(
            String method,
            String url,
            RequestSpecification request) {

        int maxRetry = 0; // total attempts = 1 initial + 2 retries
        Response response = null;

        for (int attempt = 0; attempt <= maxRetry; attempt++) {

            response = execute(method, url, request);
            int status = response.statusCode();

            //  Success → stop retrying
            if (status < 500 && status != 401) {
                break;
            }

            //  Retry 401 only once (token-related issues)
            if (status == 401 && attempt >= 1) {
                System.out.println(
                        "Stopping retry for 401 after one attempt");
                break;
            }

            System.out.println(
                    "Retrying API | Attempt: " + (attempt + 1)
                            + " | Status: " + status);

            //  Small delay before retry (helps unstable systems)
            try {
                Thread.sleep(1000); // 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return response;
    }

    public static Response execute(
            String method,
            String url,
            RequestSpecification request) {

        switch (method.toUpperCase()) {
            case "GET":
                return request.get(url);
            case "POST":
                return request.post(url);
            case "PUT":
                return request.put(url);
            case "DELETE":
                return request.delete(url);
            default:
                throw new IllegalArgumentException(
                        "Invalid HTTP Method: " + method);
        }
    }
}
