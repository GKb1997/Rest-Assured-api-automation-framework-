package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class UiTokenGenerator {

    public static String generateTokenFromUI() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            //driver.get(ConfigReader.get("ui.login.url"));
            driver.get(EnvironmentResolver.getUiLoginUrl());
            // 🔥 STEP 1: Patch fetch BEFORE login
            ((JavascriptExecutor) driver).executeScript("""
                (function() {
                    const originalFetch = window.fetch;
                    window.__authToken = null;

                    window.fetch = function() {
                        const args = arguments;
                        const headers = args[1] && args[1].headers;
                        if (headers && headers.Authorization) {
                            window.__authToken = headers.Authorization;
                        }
                        return originalFetch.apply(this, args);
                    };
                })();
            """);
            driver.findElement(By.xpath("//button[contains(text(),'HRMS')]")).click();


            // 🔐 STEP 2: Login
            driver.findElement(By.xpath("//input[@id='userNameInput']"))
                    .sendKeys(ConfigReader.get("ui_username"));

            driver.findElement(By.xpath("//input[@id='passwordInput']"))
                    .sendKeys(ConfigReader.get("ui_password"));

            driver.findElement(By.id("submitButton")).click();

            // ⏳ wait for dashboard APIs
            Thread.sleep(8000);

            String token = null;

            // ✅ STEP 3: LocalStorage (best)
            token = (String) ((JavascriptExecutor) driver)
                    .executeScript(
                            "return window.localStorage.getItem('token') || " +
                                    "window.localStorage.getItem('access_token');"
                    );

            // ✅ STEP 4: Cookie fallback
            if (token == null) {
                Cookie cookie = driver.manage().getCookieNamed("token");
                if (cookie == null) {
                    cookie = driver.manage().getCookieNamed("CXUserCookie");
                }
                if (cookie != null) {
                    token = cookie.getValue();
                }
            }

            // ✅ STEP 5: Fetch patch fallback
            if (token == null) {
                String authHeader = (String) ((JavascriptExecutor) driver)
                        .executeScript("return window.__authToken;");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.replace("Bearer ", "");
                }
            }

            if (token == null) {
                throw new RuntimeException("❌ Token not found from UI");
            }

            System.out.println("✅ Token successfully captured");
            return token;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}
