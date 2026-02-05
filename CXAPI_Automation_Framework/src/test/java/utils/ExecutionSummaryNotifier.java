package utils;

public class ExecutionSummaryNotifier {

    public static void sendFinalResult() {

        String overallStatus =
                ExtentManager.hasFailures()
                        ? "❌ FAILURE"
                        : "✅ SUCCESS";

        // ✅ Jenkins provides this automatically
        String buildUrl = System.getenv("BUILD_URL");

        // ✅ Fallback for local run
        String reportLink =
                (buildUrl != null)
                        ? buildUrl + "artifact/reports/CX_API_Report.html"
                        : "Local Run - Report available in reports folder";

        String message =
                "*CX API Automation Execution Result*\n" +
                        "----------------------------------\n" +
                        ExtentManager.getSummary() + "\n" +
                        "Overall Status: " + overallStatus + "\n" +
                        "Report: " + reportLink;

        SlackNotifier.sendMessage(message);
    }
}
