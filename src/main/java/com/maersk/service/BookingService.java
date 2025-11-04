package com.maersk.service;

import com.maersk.model.Booking;
import com.maersk.model.BookingRequest;
import com.maersk.repository.BookingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface BookingService {
    Mono<String> create(BookingRequest req);
}