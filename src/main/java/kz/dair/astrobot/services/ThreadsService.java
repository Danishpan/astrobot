package kz.dair.astrobot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class ThreadsService {

    @Value("${threads.api.token}")
    private String threadsToken;

    @Value("${threads.api.user_id}")
    private String threadsUserId;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a media container for a text-based post in Threads.
     *
     * @param message The text to be posted.
     * @return The media container ID.
     */
    public String createMediaContainer(String message) {
        try {
            String url = String.format("https://graph.threads.net/v1.0/%s/threads", threadsUserId);

            // Convert request body to JSON
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "media_type", "TEXT",
                    "text", message,
                    "access_token", threadsToken
            ));

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send request and parse response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonResponse = objectMapper.readTree(response.body());

            return jsonResponse.get("id").asText(); // Return the container ID

        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating media container";
        }
    }

    /**
     * Publishes a post to Threads using the given media container ID.
     *
     * @param containerId The container ID returned from createMediaContainer().
     * @return The response from Threads API.
     */
    public String publishContainer(String containerId) {
        try {
            String url = String.format("https://graph.threads.net/v1.0/%s/threads_publish", threadsUserId);

            // Convert request body to JSON
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "creation_id", containerId,
                    "access_token", threadsToken
            ));

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send request and parse response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonResponse = objectMapper.readTree(response.body());

            return "Post published successfully: " + jsonResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error publishing post";
        }
    }
}