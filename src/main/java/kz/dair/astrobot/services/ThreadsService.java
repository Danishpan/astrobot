package kz.dair.astrobot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class ThreadsService {

    @Value("${threads.api.token}")
    private String threadsToken;

    @Value("${threads.api.user_id}")
    private String threadsUserId;

    private final WebClient webClient = WebClient.builder().build();

    public Mono<String> createMediaContainer(String message) {
        String url = String.format("https://graph.threads.net/v1.0/%s/threads", threadsUserId);

        return webClient.post()
                .uri(url)
                .bodyValue(Map.of(
                        "media_type", "TEXT",
                        "text", message,
                        "access_token", threadsToken
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("id")); // Returns container ID
    }

    public Mono<String> publishContainer(String containerId) {
        String url = String.format("https://graph.threads.net/v1.0/%s/threads_publish", threadsUserId);

        return webClient.post()
                .uri(url)
                .bodyValue(Map.of(
                        "creation_id", containerId,
                        "access_token", threadsToken
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> "Post published successfully: " + response);
    }
}
