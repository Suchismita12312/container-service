package com.maersk.client;

import com.maersk.model.AvailabilityRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class AvailabilityClient {
    private final WebClient client;
    public AvailabilityClient(@Qualifier("availabilityWebClient") WebClient client) { this.client = client; }

    public Mono<Boolean> isAvailable(AvailabilityRequest req) {
        return client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(AvailableSpace.class)
                                .map(as -> as != null && as.availableSpace > 0)
                                .switchIfEmpty(Mono.just(false));
                    }
                    return resp.createException().flatMap(Mono::error);
                })
                .retryWhen(
                        reactor.util.retry.Retry
                                .backoff(3, Duration.ofMillis(200))
                                .jitter(0.5)
                                .filter(this::isTransientError)
                );
    }

    private boolean isTransientError(Throwable t) {
        if (t instanceof TimeoutException) return true;
        if (t instanceof WebClientRequestException) return true;

        if (t instanceof WebClientResponseException wre) {
            return wre.getStatusCode().is5xxServerError();
        }
        return false;
    }
    static class AvailableSpace { public int availableSpace; }
}
