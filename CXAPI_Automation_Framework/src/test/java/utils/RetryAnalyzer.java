package utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 0;

    @Override
    public boolean retry(ITestResult result) {

        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            result.setAttribute("RETRIED", true); // ✅ MARK RETRY
            return true;
        }

        result.setAttribute("FINAL_FAIL", true); // ✅ FINAL FAILURE
        return false;
    }
}
