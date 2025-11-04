package com.maersk.web;

import com.maersk.model.*;
import com.maersk.service.AvailabilityService;
import com.maersk.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Bookings")
public class BookingController {

    @Autowired
    private AvailabilityService availability;

    @Autowired
    private BookingService bookings;

    @PostMapping(path = "/check-availability", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check available container space via external service")
    public Mono<Map<String, Boolean>> check(@Valid @RequestBody AvailabilityRequest request) {
        return availability.checkAvailability(request)
                .map(av -> Map.of("available", av));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a booking and persist to MongoDB")
    public Mono<Map<String, String>> create(@Valid @RequestBody BookingRequest request) {
        return bookings.create(request)
                .map(ref -> Map.of("bookingRef", ref));
    }
}