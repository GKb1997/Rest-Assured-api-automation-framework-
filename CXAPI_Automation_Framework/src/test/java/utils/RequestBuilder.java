package utils;

import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class RequestBuilder {

    public static RequestSpecification build(
            String headers,
            String token,
            String queryParams,
            String body) {

        RequestSpecification request = given();

        // ✅ HEADERS (SAFE + DYNAMIC)
        if (headers != null && !headers.trim().isEmpty()) {
            for (String h : headers.split(",")) {

                h = h.trim();
                if (h.isEmpty() || !h.contains("=")) {
                    System.out.println("Skipping invalid header: " + h);
                    continue;
                }

                String[] kv = h.split("=", 2);
                request.header(
                        kv[0].trim(),
                        resolveDynamicValues(kv[1].trim())
                );
            }
        }

        // ✅ AUTH TOKEN (AUTO / STATIC / CHAINED)
        String finalToken = AuthTokenManager.getToken();

        if (finalToken != null && !finalToken.trim().isEmpty()) {

            request.header(
                    "Authorization",
                    "Bearer " + resolveDynamicValues(finalToken.trim())
            );
        }



        // ✅ QUERY PARAMS (SAFE + DYNAMIC)
        if (queryParams != null && !queryParams.trim().isEmpty()) {
            for (String qp : queryParams.split(",")) {

                qp = qp.trim();
                if (qp.isEmpty() || !qp.contains("=")) {
                    System.out.println("Skipping invalid query param: " + qp);
                    continue;
                }

                String[] kv = qp.split("=", 2);
                request.queryParam(
                        kv[0].trim(),
                        resolveDynamicValues(kv[1].trim())
                );
            }
        }

        // ✅ REQUEST BODY (STATIC + CHAINED)
        if (body != null && !body.trim().isEmpty()) {
            request.body(resolveDynamicValues(body));
        }

        return request;
    }

    // ===================================================
    // 🔥 DYNAMIC VALUE RESOLVER (API CHAINING CORE)
    // ===================================================
    private static String resolveDynamicValues(String input) {

        if (input == null) return null;

        for (Map.Entry<String, Object> entry :
                TestContext.getAll().entrySet()) {

            if (entry.getValue() != null) {
                input = input.replace(
                        "${" + entry.getKey() + "}",
                        entry.getValue().toString()
                );
            }
        }
        return input;
    }
}
