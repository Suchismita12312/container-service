package com.maersk.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AvailabilityConfigTest {

    private AvailabilityConfig availabilityConfig;
    private AvailabilityClientProperties props;
    private WebClient.Builder builder;

    @BeforeEach
    void setup() {
        availabilityConfig = new AvailabilityConfig();
        props = new AvailabilityClientProperties();
        builder = WebClient.builder();

        props.setBaseUrl("http://localhost:8080");
        props.setConnectTimeoutMs(1000);
        props.setResponseTimeoutMs(2000);
        props.setReadTimeoutMs(3000);
        props.setWriteTimeoutMs(4000);
    }

    @Test
    void testCircuitBreakerRegistryBean() {
        CircuitBreakerRegistry registry = availabilityConfig.cbRegistry();

        assertThat(registry).isNotNull();
        assertThat(registry.getAllCircuitBreakers()).isEmpty(); // no CB yet
    }

    @Test
    void testCircuitBreakerBean() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker cb = availabilityConfig.availabilityCb(registry);

        assertThat(cb).isNotNull();
        assertThat(cb.getName()).isEqualTo("availability");
        assertThat(registry.getAllCircuitBreakers()).contains(cb);
    }

    @Test
    void testCircuitBreakerFilterIntegration() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker cb = registry.circuitBreaker("availability");

        WebClient webClient = availabilityConfig.availabilityWebClient(builder, props, cb);

        // Create a test request
        WebClient.RequestHeadersSpec<?> request = webClient.get().uri("/test");

        // Verify the call completes normally (since no real server)
        StepVerifier.create(request.exchangeToMono(response -> Mono.just(1)))
                .expectNext(1)
                .verifyComplete();
    }


    /**
     * Helper method to extract base URL from WebClient using reflection.
     */
    private String getBaseUrl(WebClient webClient) {
        try {
            Field exchangeFunctionField = webClient.getClass().getDeclaredField("exchangeFunction");
            exchangeFunctionField.setAccessible(true);
            Object exchangeFunction = exchangeFunctionField.get(webClient);
            if (exchangeFunction == null) return null;

            Field baseUrlField = webClient.getClass().getDeclaredField("baseUrl");
            baseUrlField.setAccessible(true);
            return (String) baseUrlField.get(webClient);
        } catch (Exception e) {
            return null;
        }
    }
}
