package utils;

public class EnvironmentResolver {

    public static String getBaseUrl() {

        String env = ConfigReader.get("env").toUpperCase();

        switch (env) {
            case "PROD":
                return ConfigReader.get("api.base.url.prod");

            case "UAT":
                return ConfigReader.get("api.base.url.uat");

            default:
                throw new RuntimeException(
                        "❌ Invalid env value: " + env + " (allowed: UAT / PROD)"
                );
        }
    }

    public static String getUiLoginUrl() {

        String env = ConfigReader.get("env").toUpperCase();

        switch (env) {
            case "PROD":
                return ConfigReader.get("ui.login.url.prod");

            case "UAT":
                return ConfigReader.get("ui.login.url.uat");

            default:
                throw new RuntimeException(
                        "❌ Invalid env value: " + env + " (allowed: UAT / PROD)"
                );
        }
    }
}
