package com.demo.service;

import com.demo.model.BookingRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface BookingService {
    Mono<String> create(BookingRequest req);
}