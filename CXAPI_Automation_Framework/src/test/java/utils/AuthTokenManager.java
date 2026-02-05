package utils;

public class AuthTokenManager {

    private static String token;

    // 🔹 Used by API tests
    public static synchronized String getToken() {

        if (token == null || token.isEmpty()) {
            token = UiTokenGenerator.generateTokenFromUI();
        }
        return token;
    }

    // 🔹 Used when token is extracted from API response
    public static synchronized void setToken(String newToken) {
        token = newToken;
    }

    // 🔹 Optional helper (good practice)
    public static synchronized void clearToken() {
        token = null;
    }
}
