package kz.dair.astrobot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class ThreadsService {

    @Value("${threads.api.token}")
    private String threadsToken;

    @Value("${threads.api.user_id}")
    private String threadsUserId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))  // Timeout for connection
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_RETRIES = 5; // Number of retry attempts
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds delay between retries

    /**
     * Creates a media container for a text-based post in Threads.
     * Automatically retries if an exception occurs.
     *
     * @param message The text to be posted.
     * @return The media container ID.
     */
    public String createMediaContainer(String message) {
        String url = String.format("https://graph.threads.net/v1.0/%s/threads", threadsUserId);
        String requestBody;

        try {
            requestBody = objectMapper.writeValueAsString(Map.of(
                    "media_type", "TEXT",
                    "text", message,
                    "access_token", threadsToken
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create request body", e);
        }

        return executeWithRetry(() -> sendPostRequest(url, requestBody).get("id").asText(), "create media container");
    }

    /**
     * Publishes a post to Threads using the given media container ID.
     * Automatically retries if an exception occurs.
     *
     * @param containerId The container ID returned from createMediaContainer().
     * @return The response from Threads API.
     */
    public String publishContainer(String containerId) {
        String url = String.format("https://graph.threads.net/v1.0/%s/threads_publish", threadsUserId);
        String requestBody;

        try {
            requestBody = objectMapper.writeValueAsString(Map.of(
                    "creation_id", containerId,
                    "access_token", threadsToken
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create request body", e);
        }

        return executeWithRetry(() -> sendPostRequest(url, requestBody).get("id").asText(), "publish container");
    }

    /**
     * Executes an HTTP request with retry logic.
     *
     * @param requestSupplier A lambda function that sends the request.
     * @param actionName A descriptive name of the action for logging.
     * @return The API response if successful.
     */
    private String executeWithRetry(RequestSupplier requestSupplier, String actionName) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                return requestSupplier.send();
            } catch (Exception e) {
                System.err.printf("Attempt %d/%d: Failed to %s. Retrying in %d ms...%n",
                        attempt, MAX_RETRIES, actionName, RETRY_DELAY_MS);

                if (attempt == MAX_RETRIES) {
                    return "Error: Failed to " + actionName + " after " + MAX_RETRIES + " attempts.";
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return "Unexpected error while trying to " + actionName;
    }

    /**
     * Sends an HTTP POST request and returns the API response.
     *
     * @param url The request URL.
     * @param requestBody The JSON payload.
     * @return The API response.
     */
    private JsonNode sendPostRequest(String url, String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readTree(response.body());
    }

    /**
     * Functional interface for retryable requests.
     */
    @FunctionalInterface
    private interface RequestSupplier {
        String send() throws Exception;
    }
}
