package tests;

import base.BaseTest;
import client.ApiClient;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.*;

import java.util.List;
import java.util.Map;

public class ApiExecutorTest extends BaseTest {

    // ================== DATA PROVIDER ==================
    @DataProvider(name = "apiData", parallel = true)
    public Object[][] apiData() throws Exception {

        String sheetName = System.getProperty("sheet", "UAT_API");
            System.out.println("====="+sheetName);

        List<Map<String, String>> testData =
                ExcelReader.readExcel(sheetName);

        return testData.stream()
                .filter(data -> data.get("Execute").equalsIgnoreCase("Y"))
                .map(data -> new Object[]{data})
                .toArray(Object[][]::new);
    }
    // ================================================ ===

    @Test(
            dataProvider = "apiData",
            retryAnalyzer = utils.RetryAnalyzer.class
    )
    public void executeApis(Map<String, String> data) throws Exception {

        SoftAssert softAssert = new SoftAssert();

        // ===== CREATE EXTENT TEST EARLY =====
        ExtentManager.createTest(data.get("TestCaseID"));

        // ✅ ADDED: SET BASE URI FROM SHEET NAME
       /* String sheetName = System.getProperty("sheet", "UAT_API");
        RestAssured.baseURI =
                EnvironmentResolver.getBaseUrl(sheetName);

        ExtentManager.getTest()
                .info("Sheet Name: " + sheetName)
                .info("Base URL: " + RestAssured.baseURI);*/
        // ✅ SET BASE URI FROM ENV (UAT / PROD)
        RestAssured.baseURI = EnvironmentResolver.getBaseUrl();

        ExtentManager.getTest()
                .info("Environment: " + ConfigReader.get("env"))
                .info("Base URL: " + RestAssured.baseURI);

        // ✅ END OF ADDED CODE

        // 🔥 CAPTURE cURL COMMAND
        String curlCommand = buildCurlCommand(data);
        ExtentManager.getTest()
                .info("cURL Command")
                .info("<pre>" + curlCommand + "</pre>");

        var request = RequestBuilder.build(
                data.get("Headers"),
                data.get("AuthToken"),
                data.get("QueryParams"),
                data.get("RequestBody")
        );

        request.log().all();

        Response response = ApiClient.executeWithRetry(
                data.get("Method"),
                data.get("URL"),
                request
        );
      //  response time validation
        boolean responseTimeValid =
                ResponseValidator.validateResponseTime(
                        response,
                        data.get("MaxResponseTime")
                );
        //  Capture response body once
//        String responseBody = response.getBody().asPrettyString();
//
////   Log response body in Extent (for BOTH pass & fail)
//        ExtentManager.getTest()
//                .info("Response Body")
//                .info("<pre>" + responseBody + "</pre>");
        // ===== COLLAPSIBLE RESPONSE BODY (PASS / FAIL COLOR) =====
        String responseBody = response.getBody().asPrettyString();

        String bgColor;
        String title;

        if (response.statusCode() == Integer.parseInt(data.get("ExpectedStatus"))) {
            bgColor = "#e6fffa";   // light green
            title = "Response Body (PASS - click to expand)";
        } else {
            bgColor = "#ffe6e6";   // light red
            title = "Response Body (FAIL - click to expand)";
        }

        ExtentManager.getTest().info(
                "<details>" +
                        "<summary style='cursor:pointer; font-weight:bold;'>" + title + "</summary>" +
                        "<pre style='background:" + bgColor +
                        "; padding:10px; border-radius:5px; max-height:400px; overflow:auto;'>" +
                        responseBody +
                        "</pre>" +
                        "</details>"
        );
// =========================================================




        int actual = response.statusCode();
        int expected = Integer.parseInt(data.get("ExpectedStatus"));

        boolean statusValid =
                ResponseValidator.validateStatus(response, expected);

        boolean schemaValid =
                ResponseValidator.validateSchema(
                        response,
                        data.get("SchemaFile")
                );
        boolean responseBodyValid =
                ResponseValidator.validateResponseFields(
                        response,
                        data.get("ExpectedResponse")
                );
        /*ExtentManager.getTest()
                .info("Expected Headers from Excel: " + data.get("ExpectedHeaders"));*/
/*

        boolean headersValid =
                ResponseValidator.validateHeaders(
                        response,
                        data.get("ExpectedHeaders")
                );
*/




        // 🔁 RETRY LOGGING
        if (!statusValid || !schemaValid) {
            ExtentManager.getTest()
                    .warning("Test failed. Retry will be attempted if retries are remaining.");
        }

        /*String result =
                (statusValid && schemaValid && responseTimeValid)
                        ? "PASS" : "FAIL";
*/

        String result =
                (statusValid && schemaValid && responseBodyValid && responseTimeValid)
                        ? "PASS" : "FAIL";

        // ===== EXCEL RESULT =====
        ResultExcelWriter.write(
                data.get("TestCaseID"),
                data.get("Method"),
                data.get("URL"),
                expected,
                actual,
                result
        );

        // ===== EXTENT REPORT DETAILS =====
        ExtentManager.getTest().info("Method: " + data.get("Method"));
        ExtentManager.getTest().info("URL: " + data.get("URL"));
        ExtentManager.getTest().info("Expected Status: " + expected);
        ExtentManager.getTest().info("Actual Status: " + actual);

        if ("PASS".equals(result)) {

            ExtentManager.markPass();
            ExtentManager.getTest().pass("API Passed");

        } else {

            ExtentManager.markFail();   // ✅ ALWAYS count fail here
            ExtentManager.getTest().fail("API Failed");
        }


        // ==================================

        softAssert.assertTrue(
                statusValid,
                "Status code mismatch for TC: " + data.get("TestCaseID")
        );

        softAssert.assertTrue(
                schemaValid,
                "Schema validation failed for TC: " + data.get("TestCaseID")
        );
        softAssert.assertTrue(
                responseBodyValid,
                "Response body validation failed for TC: " + data.get("TestCaseID")
        );


        softAssert.assertTrue(
                responseTimeValid,
                "Response time SLA breached for TC: " + data.get("TestCaseID")
        );



        softAssert.assertAll();
    }

    // ===================================================
    // 🔥 STEP 1: cURL BUILDER METHOD
    // ===================================================
    private String buildCurlCommand(Map<String, String> data) {

        StringBuilder curl = new StringBuilder("curl -X ");
        curl.append(data.get("Method"))
                .append(" \"")
                .append(data.get("URL"))
                .append("\"");

        // Headers (from Excel)
        if (data.get("Headers") != null && !data.get("Headers").isEmpty()) {
            String[] headers = data.get("Headers").split(",");
            for (String header : headers) {
                curl.append(" -H \"").append(header.trim()).append("\"");
            }
        }

        // Authorization (masked for safety)
        if (data.get("AuthToken") != null && !data.get("AuthToken").isEmpty()) {
            curl.append(" -H \"Authorization: Bearer ****MASKED****\"");
        }

        // Request Body
        if (data.get("RequestBody") != null && !data.get("RequestBody").isEmpty()) {
            curl.append(" -d '").append(data.get("RequestBody")).append("'");
        }

        return curl.toString();
    }
}
