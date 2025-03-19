package kz.dair.astrobot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveService {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${bot.base.url}")
    private String baseUrl;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void keepAlive() {
        String url = baseUrl + "/healthz";
        try {
            restTemplate.getForObject(url, String.class);
            System.out.println("Ping successful: " + url);
        } catch (Exception e) {
            System.err.println("Ping failed: " + e.getMessage());
        }
    }
}
