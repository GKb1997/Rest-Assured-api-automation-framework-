package utils;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    // ✅ Execution counters
    private static int passed = 0;
    private static int failed = 0;

    // ✅ NEW: total test counter
    private static int total = 0;

//    public static void init() {
//        extent = new ExtentReports();
//        ExtentSparkReporter spark =
//                new ExtentSparkReporter("reports/CX_API_Report.html");
//        extent.attachReporter(spark);
//    }
public static void init() {

    if (extent == null) {   // 🔒 VERY IMPORTANT
        extent = new ExtentReports();

        ExtentSparkReporter spark =
                new ExtentSparkReporter("reports/CX_API_Report.html");

        extent.attachReporter(spark);
    }
    }

    // ✅ UPDATED: count total tests here
    public static synchronized ExtentTest createTest(String testName) {
        total++; // 👈 counts every test
        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
        return extentTest;
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    // ✅ mark pass
    public static synchronized void markPass() {
        passed++;
    }

    // ✅ mark fail
    public static synchronized void markFail() {
        failed++;
    }

    // ✅ execution summary (Slack-friendly)
    public static String getSummary() {
        return "Total: " + total +
                " | Passed: " + passed +
                " | Failed: " + failed;
    }

    public static boolean hasFailures() {
        return failed > 0;
    }

    // ✅ NEW: getters (optional but useful)
    public static int getTotal() {
        return total;
    }

    public static int getPassed() {
        return passed;
    }

    public static int getFailed() {
        return failed;
    }

    public static void flush() {
        extent.flush();
    }
}
