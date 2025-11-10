package com.demo.model;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class BookingRequest extends AvailabilityRequest {

    @NotBlank
    @Pattern(
            regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
            message = "timestamp must be ISO-8601 UTC like 2020-10-12T13:53:09Z")
    private String timestamp;

}