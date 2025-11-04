package com.maersk.service.impl;

import com.maersk.client.AvailabilityClient;
import com.maersk.model.AvailabilityRequest;
import com.maersk.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    AvailabilityClient availabilityClient;

    @InjectMocks
    AvailabilityServiceImpl service; // class under test

    @Test
    void checkAvailability_whenClientReturnsTrue_emitsTrueAndCompletes() {
        // Given
        AvailabilityRequest req = Mockito.mock(AvailabilityRequest.class);
        when(availabilityClient.isAvailable(req)).thenReturn(Mono.just(true));

        // When / Then
        StepVerifier.create(service.checkAvailability(req))
                .expectNext(true)
                .verifyComplete();

        verify(availabilityClient).isAvailable(req);
        verifyNoMoreInteractions(availabilityClient);
    }

    @Test
    void checkAvailability_whenClientReturnsFalse_emitsFalseAndCompletes() {
        // Given
        AvailabilityRequest req = Mockito.mock(AvailabilityRequest.class);
        when(availabilityClient.isAvailable(req)).thenReturn(Mono.just(false));

        // When / Then
        StepVerifier.create(service.checkAvailability(req))
                .expectNext(false)
                .verifyComplete();

        verify(availabilityClient).isAvailable(req);
        verifyNoMoreInteractions(availabilityClient);
    }

    @Test
    void checkAvailability_whenClientErrors_propagatesError() {
        // Given
        AvailabilityRequest req = Mockito.mock(AvailabilityRequest.class);
        RuntimeException boom = new RuntimeException("upstream down");
        when(availabilityClient.isAvailable(req)).thenReturn(Mono.error(boom));

        // When / Then
        StepVerifier.create(service.checkAvailability(req))
                .expectErrorMatches(ex -> ex == boom || (ex instanceof RuntimeException && "upstream down".equals(ex.getMessage())))
                .verify();

        verify(availabilityClient).isAvailable(req);
        verifyNoMoreInteractions(availabilityClient);
    }

    @Test
    void checkAvailability_passesRequestThroughUnmodified() {
        // Given
        AvailabilityRequest req = Mockito.mock(AvailabilityRequest.class);
        when(availabilityClient.isAvailable(req)).thenReturn(Mono.just(true));

        // When
        service.checkAvailability(req).block(); // blocking only in test to verify interaction

        // Then
        verify(availabilityClient, times(1)).isAvailable(same(req)); // exact same instance
    }
}