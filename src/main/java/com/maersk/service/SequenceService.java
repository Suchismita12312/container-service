package com.maersk.service;

import com.maersk.model.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SequenceService {

    @Autowired
    private ReactiveMongoOperations ops;
    private static final String BOOKING_SEQ = "bookingRef";
    private static final long BASE = 957000000L;

    public Mono<String> nextBookingRef() {
        Query q = Query.query(Criteria.where("_id").is(BOOKING_SEQ));
        Update u = new Update().inc("value", 1);
        var opts = FindAndModifyOptions.options().upsert(true).returnNew(true);
        return ops.findAndModify(q, u, opts, Sequence.class)
                .defaultIfEmpty(new Sequence(BOOKING_SEQ, 1))
                .map(seq -> Long.toString(BASE + seq.getValue()));
    }
}