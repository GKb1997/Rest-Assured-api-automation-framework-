package base;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import utils.AuthTokenManager;
import utils.ExecutionSummaryNotifier;
import utils.ExtentManager;

public class BaseTest {

    @BeforeSuite
    public void beforeSuite() {

        // 1️⃣ Initialize Extent
        ExtentManager.init();

        // 2️⃣ 🔐 Generate UI token ONCE
        String token = AuthTokenManager.getToken();

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("❌ Auth token generation failed");
        }

        System.out.println("✅ Auth token generated successfully");
    }

    @AfterSuite
    public void afterSuite() {

        // 3️⃣ Flush Extent report
        ExtentManager.flush();

        // 4️⃣ Send execution summary to Slack
        ExecutionSummaryNotifier.sendFinalResult();
        AuthTokenManager.clearToken();
    }
}
