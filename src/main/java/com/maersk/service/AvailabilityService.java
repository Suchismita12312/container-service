package com.maersk.service;

import com.maersk.model.AvailabilityRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

public interface AvailabilityService {

    public Mono<Boolean> checkAvailability(AvailabilityRequest req) ;
}