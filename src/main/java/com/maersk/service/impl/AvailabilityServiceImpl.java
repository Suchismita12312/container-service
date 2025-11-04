package com.maersk.service.impl;

import com.maersk.client.AvailabilityClient;
import com.maersk.model.AvailabilityRequest;
import com.maersk.service.AvailabilityService;
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
