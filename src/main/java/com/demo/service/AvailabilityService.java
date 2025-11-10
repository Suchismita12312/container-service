package com.demo.service;

import com.demo.model.AvailabilityRequest;
import reactor.core.publisher.Mono;

public interface AvailabilityService {

    public Mono<Boolean> checkAvailability(AvailabilityRequest req) ;
}