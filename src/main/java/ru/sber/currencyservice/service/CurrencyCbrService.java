package ru.sber.currencyservice.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.currencyservice.client.CbrCacheService;
import ru.sber.currencyservice.dto.CbrDailyResponse;
import ru.sber.currencyservice.dto.ValuteRecord;
import ru.sber.proto.CbrCurrencyServiceGrpc;
import ru.sber.proto.ConvertRequest;
import ru.sber.proto.ConvertResponse;
import ru.sber.proto.ExchangeRateInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@GrpcService
@RequiredArgsConstructor
public class CurrencyCbrService extends CbrCurrencyServiceGrpc.CbrCurrencyServiceImplBase {

    private final CbrCacheService cacheService;

    @Override
    public void convert(ConvertRequest request, StreamObserver<ConvertResponse> responseObserver) {
        BigDecimal rubles = new BigDecimal(request.getAmount());
        String targetCurrency = request.getTargetCurrency().toUpperCase();

        CbrDailyResponse rates = cacheService.getRates();

        ValuteRecord valute = rates.valute().get(targetCurrency);

        if (valute == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Currency " + targetCurrency + " not found")
                    .asRuntimeException());
            return;
        }

        BigDecimal nominal = BigDecimal.valueOf(valute.nominal());
        BigDecimal converted = rubles.multiply(nominal).divide(valute.value(), 2, RoundingMode.HALF_EVEN);

        ValuteRecord usd = rates.valute().get("USD");
        ValuteRecord eur = rates.valute().get("EUR");
        BigDecimal rateUsdRub = usd.value().divide(BigDecimal.valueOf(usd.nominal()), 2, RoundingMode.HALF_EVEN);
        BigDecimal rateEurRub = eur.value().divide(BigDecimal.valueOf(eur.nominal()), 2, RoundingMode.HALF_EVEN);

        String dateStr = formatDate(rates.date());

        ExchangeRateInfo info = ExchangeRateInfo.newBuilder()
                .setRateUsdRub(rateUsdRub.toPlainString())
                .setRateEurRub(rateEurRub.toPlainString())
                .setDate(dateStr)
                .build();



        ConvertResponse response = ConvertResponse.newBuilder()
                .setOriginalRubles(rubles.toPlainString())
                .setConvertedAmount(converted.toPlainString())
                .setTargetCurrency(targetCurrency)
                .setExchangeRateInfo(info)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private String formatDate(String date) {
            return LocalDate.parse(date.substring(0, 10))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
