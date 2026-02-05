package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;

public class ResponseNormalizer {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Normalizes API response JSON so the SAME schema
     * works in both UAT & PROD without modification
     */
    public static String normalize(Response response) {

        try {
            String body = response.getBody().asString();
            JsonNode root = mapper.readTree(body);

            // 🔥 CASE: data = null → data = []
            if (root.has("data") && root.get("data").isNull()) {
                ((ObjectNode) root).putArray("data");
            }

            return root.toPrettyString();

        } catch (Exception e) {
            // Fail-safe: return original body
            return response.getBody().asString();
        }
    }
}
