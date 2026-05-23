package ru.sber.currencyservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class WebConfig {

    @Bean
    public WebClient cbrWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jacksonJsonDecoder(
                            new JacksonJsonDecoder(new JsonMapper(),
                                    MimeTypeUtils.parseMimeType("application/javascript"))
                    );
                    configurer.defaultCodecs().jacksonJsonEncoder(
                            new JacksonJsonEncoder(new JsonMapper(),
                                    MimeTypeUtils.parseMimeType("application/javascript"))
                    );
                })
                .build();
        return WebClient
                .builder()
                .exchangeStrategies(strategies)
                .baseUrl("https://www.cbr-xml-daily.ru")
                .build();
    }
}
