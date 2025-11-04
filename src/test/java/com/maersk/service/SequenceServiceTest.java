package com.maersk.service;

import com.maersk.model.Sequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceServiceTest {

    @Mock
    ReactiveMongoOperations ops;

    @InjectMocks
    SequenceService service;

    @Test
    void nextBookingRef_returnsBasePlusCounter_whenDocumentExists() {
        // given: existing sequence doc with value=5
        Sequence existing = new Sequence("bookingRef", 5L);
        when(ops.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenReturn(Mono.just(existing));

        // when / then
        StepVerifier.create(service.nextBookingRef())
                .expectNext("957000005")  // BASE (957000000) + 5
                .verifyComplete();

        // verify query/update/options
        ArgumentCaptor<Query> qCap = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> uCap = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<FindAndModifyOptions> oCap = ArgumentCaptor.forClass(FindAndModifyOptions.class);

        verify(ops).findAndModify(qCap.capture(), uCap.capture(), oCap.capture(), eq(Sequence.class));

        Query usedQuery = qCap.getValue();
        var queryObj = usedQuery.getQueryObject();
        assertThat(queryObj.get("_id")).isEqualTo("bookingRef");

        Update usedUpdate = uCap.getValue();
        // Update doesn’t expose increments directly; at least ensure we didn’t pass null
        assertThat(usedUpdate).isNotNull();

        FindAndModifyOptions usedOpts = oCap.getValue();
        assertThat(usedOpts.isReturnNew()).isTrue();
        assertThat(usedOpts.isUpsert()).isTrue();

        verifyNoMoreInteractions(ops);
    }

    @Test
    void nextBookingRef_usesDefaultWhenDocumentMissing() {
        // given: no existing doc -> Mono.empty()
        when(ops.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenReturn(Mono.empty());

        // when / then: defaultIfEmpty(new Sequence("bookingRef", 1)) => BASE + 1
        StepVerifier.create(service.nextBookingRef())
                .expectNext("957000001")
                .verifyComplete();

        verify(ops).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class));
        verifyNoMoreInteractions(ops);
    }

    @Test
    void nextBookingRef_propagatesErrorFromMongo() {
        RuntimeException boom = new RuntimeException("mongo down");
        when(ops.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenReturn(Mono.error(boom));

        StepVerifier.create(service.nextBookingRef())
                .expectErrorMatches(ex -> ex == boom || "mongo down".equals(ex.getMessage()))
                .verify();

        verify(ops).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class));
        verifyNoMoreInteractions(ops);
    }

    @Test
    void nextBookingRef_handlesLargeCounterValues() {
        long large = 123_456_789L;
        when(ops.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class)))
                .thenReturn(Mono.just(new Sequence("bookingRef", large)));

        StepVerifier.create(service.nextBookingRef())
                .expectNext(Long.toString(957_000_000L + large))
                .verifyComplete();

        verify(ops).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Sequence.class));
        verifyNoMoreInteractions(ops);
    }
}