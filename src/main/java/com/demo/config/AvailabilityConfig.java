package com.demo.config;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class AvailabilityConfig {

    @Bean
    CircuitBreakerRegistry cbRegistry() { return CircuitBreakerRegistry.ofDefaults(); }
    @Bean CircuitBreaker availabilityCb(CircuitBreakerRegistry r) { return r.circuitBreaker("availability"); }

    @Bean
    @Qualifier("availabilityWebClient")
    WebClient availabilityWebClient(WebClient.Builder builder,
                                    AvailabilityClientProperties props,
                                    CircuitBreaker cb) {

        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.getResponseTimeoutMs()))
                .httpResponseDecoder(dec -> dec
                        .maxHeaderSize(32 * 1024)
                        .maxInitialLineLength(16 * 1024)
                )
                .doOnConnected(c -> c
                        .addHandlerLast(new ReadTimeoutHandler(props.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(props.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)));

        ExchangeFilterFunction cbFilter = (req, next) ->
                next.exchange(req).transformDeferred(CircuitBreakerOperator.of(cb));

        return builder
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(http))
                .filter(cbFilter)
                .build();
    }
}
