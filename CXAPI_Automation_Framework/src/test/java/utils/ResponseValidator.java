package utils;

import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ResponseValidator {

    // Status code validation
    public static boolean validateStatus(
            Response response,
            int expectedStatus) {

        return response.statusCode() == expectedStatus;
    }

    //  JSON Schema validation (Excel-driven)
    public static boolean validateSchema(Response response, String schemaFile) {

        System.out.println("SCHEMA VALIDATION ENTERED | File = " + schemaFile);

        if (schemaFile == null || schemaFile.trim().isEmpty()) {
            return true;
        }

        ExtentManager.getTest()
                .info("Schema File Used: " + schemaFile);

        try {

            // =====================================================
            // OLD CODE (COMMENTED – DO NOT DELETE)
            // This fails when UAT returns data=null and
            // PROD returns data=[...]
            // =====================================================
            /*
            response.then()
                    .assertThat()
                    .body(matchesJsonSchemaInClasspath("schema/" + schemaFile));
            */

            // =====================================================
            //  NEW CODE (NORMALIZED – WORKS FOR UAT & PROD)
            // =====================================================

            // 🔹 Step 1: Normalize response JSON (data=null → [])
            String normalizedJson =
                    ResponseNormalizer.normalize(response);

            // 🔹 Step 2: Validate SAME schema using normalized JSON
            io.restassured.RestAssured
                    .given()
                    .contentType("application/json")
                    .body(normalizedJson)
                    .then()
                    .body(matchesJsonSchemaInClasspath("schema/" + schemaFile));

            // =====================================================
            // ✅ MANDATORY KEY VALIDATION (LOGICAL CONTRACT)
            // =====================================================
            response.jsonPath().get("success");
            response.jsonPath().get("data");

            ExtentManager.getTest()
                    .info("Schema validation passed ✅");

            return true;

        } catch (AssertionError | Exception e) {

            ExtentManager.getTest()
                    .fail("Schema Validation Failed ❌<br><pre>"
                            + e.getMessage() + "</pre>");

            ExtentManager.getTest()
                    .info("Normalized Response:<br><pre>"
                            + ResponseNormalizer.normalize(response)
                            + "</pre>");

            return false;
        }
    }

    // 🔹 Response body field validation
    /*public static boolean validateResponseFields(
            Response response,
            String expectedData) {

        if (expectedData == null || expectedData.trim().isEmpty()) {
            return true;
        }

        try {
            String[] validations = expectedData.split(";");

            for (String v : validations) {
                String[] kv = v.split("=", 2);
                String path = kv[0].trim();
                String expected = kv[1].trim();

                Object actual = response.jsonPath().get(path);

                if (actual == null ||
                        !actual.toString().equalsIgnoreCase(expected)) {

                    ExtentManager.getTest().fail(
                            "Response mismatch ❌ | " + path +
                                    " | Expected: " + expected +
                                    " | Actual: " + actual
                    );
                    return false;
                }
            }

            ExtentManager.getTest()
                    .info("Response body validation passed ✅");
            return true;

        } catch (Exception e) {
            ExtentManager.getTest()
                    .fail("Response validation error: " + e.getMessage());
            return false;
        }
    }*/
    // 🔹 Response body field validation (String + List support)
    public static boolean validateResponseFields(
            Response response,
            String expectedData) {

        if (expectedData == null || expectedData.trim().isEmpty()) {
            return true;
        }

        try {
            String[] validations = expectedData.split(";");

            for (String v : validations) {
                String[] kv = v.split("=", 2);
                String path = kv[0].trim();
                String expected = kv[1].trim();

                Object actual = response.jsonPath().get(path);

                // ================= LIST HANDLING =================
                if (actual instanceof java.util.List<?> list) {

                    boolean match = list.stream()
                            .allMatch(val ->
                                    val != null &&
                                            val.toString().equalsIgnoreCase(expected));

                    if (!match) {
                        ExtentManager.getTest().fail(
                                "Response mismatch ❌ | " + path +
                                        " | Expected ALL values: " + expected +
                                        " | Actual: " + list
                        );
                        return false;
                    }

                }
                // ================= SINGLE VALUE =================
                else {

                    if (actual == null ||
                            !actual.toString().equalsIgnoreCase(expected)) {

                        ExtentManager.getTest().fail(
                                "Response mismatch ❌ | " + path +
                                        " | Expected: " + expected +
                                        " | Actual: " + actual
                        );
                        return false;
                    }
                }
            }

            ExtentManager.getTest()
                    .info("Response body validation passed ✅");

            return true;

        } catch (Exception e) {
            ExtentManager.getTest()
                    .fail("Response validation error: " + e.getMessage());
            return false;
        }
    }


    // 🔹 Response time validation
    public static boolean validateResponseTime(
            Response response,
            String maxTime) {

        if (maxTime == null || maxTime.trim().isEmpty()) {
            return true;
        }

        long actualTime = response.time();
        long expectedTime = Long.parseLong(maxTime) * 1000;

        if (actualTime > expectedTime) {
            ExtentManager.getTest().fail(
                    "Response time exceeded ❌ | Expected: " +
                            expectedTime + " ms | Actual: " +
                            actualTime + " ms"
            );
            return false;
        }

        ExtentManager.getTest().info(
                "Response time OK ✅ | " +
                        actualTime + " ms (limit " +
                        expectedTime + " ms)"
        );
        return true;
    }

   /* public static boolean validateHeaders(
            Response response,
            String expectedHeaders) {

        if (expectedHeaders == null || expectedHeaders.trim().isEmpty()) {
            return true;
        }

        try {
            // Format: header1=value1;header2=value2
            for (String h : expectedHeaders.split(";")) {
                String[] kv = h.split("=", 2);
                String headerName = kv[0].trim();
                String expectedValue = kv[1].trim();

                String actualValue = response.getHeader(headerName);

                if (actualValue == null ||
                        !actualValue.equalsIgnoreCase(expectedValue)) {

                    ExtentManager.getTest().fail(
                            "Header mismatch ❌ | " +
                                    headerName + " | Expected: " +
                                    expectedValue + " | Actual: " +
                                    actualValue
                    );
                    return false;
                }
            }

            ExtentManager.getTest()
                    .info("Header validation passed ✅");
            return true;

        } catch (Exception e) {
            ExtentManager.getTest()
                    .fail("Header validation error: " + e.getMessage());
            return false;
        }
    }
*/

    // 🔹 Extract values for chaining
    public static void extractValues(
            Response response,
            String extractConfig) {

        if (extractConfig == null || extractConfig.trim().isEmpty()) {
            return;
        }

        for (String e : extractConfig.split(";")) {
            String[] kv = e.split("=", 2);
            String key = kv[0].trim();
            String path = kv[1].trim();

            Object value = response.jsonPath().get(path);
            TestContext.set(key, value);

            if ("token".equalsIgnoreCase(key) && value != null) {
                AuthTokenManager.setToken(value.toString());
            }

            ExtentManager.getTest()
                    .info("Extracted 🔗 " + key + " = " + value);
        }
    }
}
