package ru.sber.currencyservice.config;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.stereotype.Component;

@GrpcGlobalServerInterceptor
@Component
@RequiredArgsConstructor
public class GrpcMetricsConfig implements ServerInterceptor {

    private final MeterRegistry meterRegistry;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String service = call.getMethodDescriptor().getServiceName();
        String method = call.getMethodDescriptor().getBareMethodName();
        Timer.Sample sample = Timer.start(meterRegistry);

        ServerCall<ReqT, RespT> wrapped = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                sample.stop(Timer.builder("grpc.server.calls")
                        .tag("grpcService", service != null ? service : "unknown")
                        .tag("grpcMethod", method != null ? method : "unknown")
                        .tag("statusCode", status.getCode().name())
                        .register(meterRegistry));
                super.close(status, trailers);
            }
        };

        return next.startCall(wrapped, headers);
    }
}
