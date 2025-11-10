package com.demo.service.impl;

import com.demo.client.AvailabilityClient;
import com.demo.model.AvailabilityRequest;
import com.demo.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    @Autowired
    private AvailabilityClient availabilityClient;

    @Override
    public Mono<Boolean> checkAvailability(AvailabilityRequest req) {
        return availabilityClient.isAvailable(req);
    }
}
