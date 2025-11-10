package com.demo.client;

import com.demo.model.AvailabilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvailabilityClientTest {

    private ExchangeFunction exchangeFunction;
    private AvailabilityClient availabilityClient;

    @BeforeEach
    void setUp() {
        exchangeFunction = Mockito.mock(ExchangeFunction.class);
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        availabilityClient = new AvailabilityClient(webClient);
    }

    @Test
    void testIsAvailable_SuccessfulResponseWithAvailableSpace() {
        AvailabilityClient.AvailableSpace body = new AvailabilityClient.AvailableSpace();
        body.availableSpace = 5;

        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("{\"availableSpace\":5}")
                .build();

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        AvailabilityRequest req = new AvailabilityRequest();

        StepVerifier.create(availabilityClient.isAvailable(req))
                .expectNext(true)
                .verifyComplete();

        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void testIsAvailable_SuccessfulResponseWithNoAvailableSpace() {
        AvailabilityClient.AvailableSpace body = new AvailabilityClient.AvailableSpace();
        body.availableSpace = 0;

        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("{\"availableSpace\":0}")
                .build();

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        StepVerifier.create(availabilityClient.isAvailable(new AvailabilityRequest()))
                .expectNext(false)
                .verifyComplete();

        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void testIsAvailable_EmptyResponseBody() {
        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse));

        StepVerifier.create(availabilityClient.isAvailable(new AvailabilityRequest()))
                .expectNext(false) // switchIfEmpty -> false
                .verifyComplete();
    }

    @Test
    void testIsAvailable_5xxResponseTriggersRetry() {
        ClientResponse errorResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();

        // Fail first 2 attempts, succeed on 3rd
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(errorResponse))
                .thenReturn(Mono.just(errorResponse))
                .thenReturn(Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .body("{\"availableSpace\":10}")
                                .build()
                ));

        StepVerifier.withVirtualTime(() ->
                        availabilityClient.isAvailable(new AvailabilityRequest())
                )
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(true)
                .verifyComplete();

        verify(exchangeFunction, times(3)).exchange(any());
    }

    @Test
    void testIsAvailable_NonRetryable4xxError() {
        ClientResponse errorResponse = ClientResponse.create(HttpStatus.BAD_REQUEST).build();
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(errorResponse));

        StepVerifier.create(availabilityClient.isAvailable(new AvailabilityRequest()))
                .expectError(WebClientResponseException.class)
                .verify();

        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void testIsAvailable_RetryOnTimeoutException() {
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.error(new TimeoutException("Timeout")))
                .thenReturn(Mono.error(new TimeoutException("Timeout")))
                .thenReturn(Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .body("{\"availableSpace\":3}")
                                .build()
                ));

        StepVerifier.withVirtualTime(() ->
                        availabilityClient.isAvailable(new AvailabilityRequest())
                )
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(true)
                .verifyComplete();

        verify(exchangeFunction, times(3)).exchange(any());
    }

}
