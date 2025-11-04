package com.maersk.model;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class AvailabilityRequest {

    @NotNull
    @Pattern(regexp = "20|40", message = "containerSize must be 20 or 40")
    private String containerSize; // keep as string to validate regex easily

    @NotNull
    private ContainerType containerType;

    @NotNull @Size(min=5, max=20)
    private String origin;

    @NotNull @Size(min=5, max=20)
    private String destination;

    @NotNull @Min(1) @Max(100)
    private Integer quantity;

}