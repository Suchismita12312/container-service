package com.maersk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="external.availability")
@Data
public class AvailabilityClientProperties {
    private String baseUrl;
    private int connectTimeoutMs=1000, responseTimeoutMs=3000, readTimeoutMs=3000, writeTimeoutMs=3000;
}
