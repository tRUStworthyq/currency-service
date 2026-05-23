package ru.sber.currencyservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record CbrDailyResponse(
        @JsonProperty("Date") String date,
        @JsonProperty("Valute") Map<String, ValuteRecord> valute
) {
}
