package com.demo.service.impl;

import com.demo.model.Booking;
import com.demo.model.BookingRequest;
import com.demo.repository.BookingRepository;
import com.demo.service.BookingService;
import com.demo.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository repo;

    @Autowired
    private SequenceService seq;



    public Mono<String> create(BookingRequest req) {
        return seq.nextBookingRef()
                .flatMap(ref -> {
                    Booking b = new Booking();
                    b.setBookingRef(ref);
                    b.setContainerSize(Integer.parseInt(req.getContainerSize()));
                    b.setContainerType(req.getContainerType());
                    b.setOrigin(req.getOrigin());
                    b.setDestination(req.getDestination());
                    b.setQuantity(req.getQuantity());
                    b.setTimestamp(req.getTimestamp());
                    return repo.save(b).map(Booking::getBookingRef);
                });
    }
}
