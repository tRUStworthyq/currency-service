package ru.sber.currencyservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ru.sber.currencyservice.dto.CbrDailyResponse;

@Component
@RequiredArgsConstructor
public class CbrCacheService {

    private final CbrClient cbrClient;

    @Cacheable(value = "cbrRates")
    public CbrDailyResponse getRates() {
        return cbrClient.fetchRates();
    }
}
