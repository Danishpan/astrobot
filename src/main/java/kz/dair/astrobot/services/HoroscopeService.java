package kz.dair.astrobot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HoroscopeService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PROMPT = """
Ты — экспертная языковая модель, которая создает краткие, остроумные и оригинальные гороскопы. Твоя задача — генерировать прогнозы для разных знаков зодиака, используя разнообразные формулировки и стили.

Правила генерации:
Краткость — каждый прогноз должен состоять из одного предложения.
Разнообразие — не используй однотипные конструкции, а меняй структуру предложений.
Случайность — выбирай знак зодиака случайным образом и вставляй его в разные части предложения.
Легкая ирония и сарказм — иногда добавляй нотки юмора, но без оскорблений.
Актуальность (редко) — в 10% случаев можешь интегрировать отсылки к современным реалиям Казахстана (например, пробки в городах, курс тенге, популярные темы и т.д.).
Стиль — начинай каждое предложение с маленькой буквы, кроме случаев, когда начинается с имени знака.
Примеры:
сегодня твоя энергия на высоте, овен, но не забывай, что даже самые быстрые устают.
близнецы, хватит метаться между вариантами, иначе скоро окажешься в роли казахстанского избирателя.
если что-то идёт не так, скорпион, просто сделай вид, что так и было задумано.
телец, деньги любят тишину, так что хватит жаловаться, что их нет.
ты можешь не верить в судьбу, стрелец, но почему-то она всё равно верит в тебя.
рак, сегодня идеально подойдёт фраза: «Я же говорил».
если хочется кардинальных перемен, водолей, начни с малого — например, удали ненужные чаты.
лев, твой авторитет сегодня непоколебим… если, конечно, ты не опоздаешь на важную встречу.
не пытайся угодить всем, весы, у тебя же нет бюджета акимата.
козерог, иногда отдыхать — это тоже продуктивно, попробуй и увидишь.
не доверяй всему, что слышишь, рыбы, особенно если это новости из случайного чата.
дева, твоя пунктуальность сегодня кого-то удивит, особенно если ты сам себя.
Теперь сгенерируй 1 уникальный гороскоп, соблюдая все правила и отправь только его без дополнительного текста.
        """;

    public String generateHoroscope() {
        try {
            // Prepare JSON request body
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", "llama-3.3-70b-specdec",
                    "messages", List.of(Map.of("role", "user", "content", PROMPT)),
                    "max_tokens", 150
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

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating horoscope";
        }
    }
}
