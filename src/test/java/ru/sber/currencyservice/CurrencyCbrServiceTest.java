package ru.sber.currencyservice;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.currencyservice.client.CbrCacheService;
import ru.sber.currencyservice.dto.CbrDailyResponse;
import ru.sber.currencyservice.dto.ValuteRecord;
import ru.sber.currencyservice.service.CurrencyCbrService;
import ru.sber.proto.ConvertRequest;
import ru.sber.proto.ConvertResponse;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CurrencyCbrServiceTest {

    @Mock
    private CbrCacheService cacheService;

    @Mock
    private StreamObserver<ConvertResponse> responseObserver;

    @InjectMocks
    private CurrencyCbrService service;

    @Captor
    private ArgumentCaptor<ConvertResponse> responseCaptor;

    private CbrDailyResponse testRates;


    @BeforeEach
    void setUp() {
        Map<String, ValuteRecord> valuteMap = Map.of(
                "USD", new ValuteRecord("USD", 1, BigDecimal.valueOf(88.50)),
                "EUR", new ValuteRecord("EUR", 1, BigDecimal.valueOf(95.10))
        );

        testRates = new CbrDailyResponse(
                "2026-04-27T11:30:00+03:00",
                valuteMap
        );
    }


    @Test
    void shouldConvertRublesToUsd() {
        when(cacheService.getRates()).thenReturn(testRates);

        ConvertRequest request = ConvertRequest.newBuilder()
                .setAmount("1000")
                .setTargetCurrency("USD")
                .build();

        service.convert(request, responseObserver);

        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verifyNoMoreInteractions(responseObserver);

        ConvertResponse response = responseCaptor.getValue();

        assertThat(response.getOriginalRubles()).isEqualTo("1000");
        assertThat(new BigDecimal(response.getConvertedAmount()))
                .isEqualByComparingTo(new BigDecimal("11.30"));
        assertThat(response.getTargetCurrency()).isEqualTo("USD");
        assertThat(response.getExchangeRateInfo().getRateUsdRub()).isEqualTo("88.50");
        assertThat(response.getExchangeRateInfo().getRateEurRub()).isEqualTo("95.10");
        assertThat(response.getExchangeRateInfo().getDate()).isEqualTo("2026-04-27");
    }

    @Test
    void shouldFailOnUnknownCurrency() {
        when(cacheService.getRates()).thenReturn(testRates);

        ConvertRequest request = ConvertRequest.newBuilder()
                .setAmount("500")
                .setTargetCurrency("EXP")
                .build();

        service.convert(request, responseObserver);

        verify(responseObserver).onError(any(Throwable.class));
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
}


