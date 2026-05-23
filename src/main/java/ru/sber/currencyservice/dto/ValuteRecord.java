package ru.sber.currencyservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ValuteRecord(
        @JsonProperty("CharCode") String charCode,
        @JsonProperty("Nominal") Integer nominal,
        @JsonProperty("Value") BigDecimal value
        
) {
}
