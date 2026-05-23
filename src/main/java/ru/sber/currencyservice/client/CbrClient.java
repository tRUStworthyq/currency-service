package ru.sber.currencyservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.sber.currencyservice.dto.CbrDailyResponse;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CbrClient {
    private final WebClient webClient;

    public CbrDailyResponse fetchRates() {
        return webClient.get()
                .uri("/daily_json.js")
                .retrieve()
                .bodyToMono(CbrDailyResponse.class)
                .block(Duration.ofSeconds(10));
    }
}
