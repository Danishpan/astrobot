package kz.dair.astrobot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class HoroscopeService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)) // Set timeout
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_RETRIES = 5; // Number of retries
    private static final long RETRY_DELAY_MS = 5000; // Delay between retries (5 sec)

    // List of zodiac signs
    private static final List<String> ZODIAC_SIGNS = List.of(
            "Овен", "Телец", "Близнецы", "Рак", "Лев", "Дева",
            "Весы", "Скорпион", "Стрелец", "Козерог", "Водолей", "Рыбы"
    );

    /**
     * Randomly selects a zodiac sign from the list.
     *
     * @return A randomly chosen zodiac sign.
     */
    private String getRandomZodiacSign() {
        Random random = new Random();
        return ZODIAC_SIGNS.get(random.nextInt(ZODIAC_SIGNS.size()));
    }

    /**
     * Generates a daily horoscope with retry logic.
     *
     * @return A generated horoscope.
     */
    public String generateHoroscope() {
        String selectedZodiac = getRandomZodiacSign();
        String prompt = buildPrompt(selectedZodiac);

        return executeWithRetry(() -> sendRequestToGroq(prompt), "generate horoscope");
    }

    /**
     * Builds the prompt string for the given zodiac sign.
     *
     * @param zodiacSign The selected zodiac sign.
     * @return The formatted prompt.
     */
    private String buildPrompt(String zodiacSign) {
        return String.format("""
Ты — экспертная языковая модель, которая создает краткие, остроумные и оригинальные гороскопы. Твоя задача — сгенерировать прогноз только для знака зодиака %s, используя разнообразные формулировки и стили.

### Правила генерации:
- Краткость — прогноз должен состоять из одного предложения.
- Разнообразие — не используй однотипные конструкции, меняй структуру предложений.
- Легкая ирония и сарказм — иногда добавляй нотки юмора, но без оскорблений.
- Начни предложение с маленькой буквы, если это возможно.

### Пример:
- сегодня твоя энергия на высоте, овен, но не забывай, что даже самые быстрые устают.
- если что-то идёт не так, скорпион, просто сделай вид, что так и было задумано.
- телец, деньги любят тишину, так что хватит жаловаться, что их нет.

Теперь сгенерируй 1 уникальный гороскоп для знака зодиака %s, соблюдая все правила и отправь только его без дополнительного текста.
        """, zodiacSign, zodiacSign);
    }

    /**
     * Sends a request to Groq API and retrieves the response.
     *
     * @param prompt The request payload.
     * @return The generated horoscope.
     * @throws Exception If an error occurs.
     */
    private String sendRequestToGroq(String prompt) throws Exception {
        // Prepare JSON request body
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", "llama-3.3-70b-specdec",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_completion_tokens", 1024,
                "temperature", 1,
                "top_p", 1
        ));

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(groqApiUrl))
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send request and get response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse JSON response
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        return jsonResponse.get("choices").get(0).get("message").get("content").asText();
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
     * Functional interface for retryable requests.
     */
    @FunctionalInterface
    private interface RequestSupplier {
        String send() throws Exception;
    }
}
