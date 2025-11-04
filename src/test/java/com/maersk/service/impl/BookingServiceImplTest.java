package com.maersk.service.impl;

import com.maersk.model.Booking;
import com.maersk.model.BookingRequest;
import com.maersk.model.ContainerType;
import com.maersk.repository.BookingRepository;
import com.maersk.service.SequenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    BookingRepository repo;

    @Mock
    SequenceService seq;

    @InjectMocks
    BookingServiceImpl service;

    private static BookingRequest req(
            String containerSize, String origin,
            String destination, int quantity, String timestamp) {
        BookingRequest r = new BookingRequest();
        r.setContainerType(ContainerType.DRY);
        r.setContainerSize(containerSize);
        r.setOrigin(origin);
        r.setDestination(destination);
        r.setQuantity(quantity);
        r.setTimestamp(timestamp);
        return r;
    }

    @Test
    void create_success_mapsAllFields_andReturnsRef() {
        // given
        var request = req("20", "Chennai", "Singapore", 5, "2020-10-12T13:53:09Z");
        when(seq.nextBookingRef()).thenReturn(Mono.just("957000001"));

        // repository should return the saved entity (with the same ref)
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        when(repo.save(any(Booking.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // when / then
        StepVerifier.create(service.create(request))
                .expectNext("957000001")
                .verifyComplete();

        // verify sequence was called once
        verify(seq, times(1)).nextBookingRef();

        // verify repo.save called with expected entity
        verify(repo, times(1)).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.getBookingRef()).isEqualTo("957000001");
        assertThat(saved.getContainerType()).isEqualTo(ContainerType.DRY);
        assertThat(saved.getContainerSize()).isEqualTo(20); // parsed from String
        assertThat(saved.getOrigin()).isEqualTo("Chennai");
        assertThat(saved.getDestination()).isEqualTo("Singapore");
        assertThat(saved.getQuantity()).isEqualTo(5);
        assertThat(saved.getTimestamp()).isEqualTo("2020-10-12T13:53:09Z");

        verifyNoMoreInteractions(seq, repo);
    }

    @Test
    void create_whenRepoSaveErrors_propagatesError() {
        var request = req("20", "Chennai", "Singapore", 5, "2020-10-12T13:53:09Z");
        when(seq.nextBookingRef()).thenReturn(Mono.just("957000002"));
        RuntimeException boom = new RuntimeException("mongo down");
        when(repo.save(any(Booking.class))).thenReturn(Mono.error(boom));

        StepVerifier.create(service.create(request))
                .expectErrorMatches(ex -> ex == boom || "mongo down".equals(ex.getMessage()))
                .verify();

        verify(seq).nextBookingRef();
        verify(repo).save(any(Booking.class));
        verifyNoMoreInteractions(seq, repo);
    }

    @Test
    void create_whenSequenceErrors_propagatesError_andDoesNotCallRepo() {
        var request = req("20", "Chennai", "Singapore", 5, "2020-10-12T13:53:09Z");
        IllegalStateException seqErr = new IllegalStateException("seq service unavailable");
        when(seq.nextBookingRef()).thenReturn(Mono.error(seqErr));

        StepVerifier.create(service.create(request))
                .expectError(IllegalStateException.class)
                .verify();

        verify(seq).nextBookingRef();
        verifyNoInteractions(repo); // repo.save must NOT be called
    }

    @Test
    void create_whenContainerSizeInvalid_emitsNumberFormatException_andDoesNotCallRepo() {
        var bad = req("XX", "Chennai", "Singapore", 5, "2020-10-12T13:53:09Z");
        when(seq.nextBookingRef()).thenReturn(Mono.just("957000003"));

        StepVerifier.create(service.create(bad))
                .expectError(NumberFormatException.class)
                .verify();

        verify(seq).nextBookingRef();
        verifyNoInteractions(repo);
    }
}