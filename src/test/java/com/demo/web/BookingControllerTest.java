package com.demo.web;

import com.demo.model.AvailabilityRequest;
import com.demo.model.BookingRequest;
import com.demo.service.AvailabilityService;
import com.demo.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = BookingController.class)
@Import(GlobalErrorHandler.class)
class BookingControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    AvailabilityService availability;

    @MockBean
    BookingService bookings;

    @Test
    void checkAvailability_returns200_andBoolean() {
        Mockito.when(availability.checkAvailability(Mockito.any(AvailabilityRequest.class)))
                .thenReturn(Mono.just(true));

        String body = """
        {
          "containerType":"DRY",
          "containerSize":"20",
          "origin":"Chennai",
          "destination":"Singapore",
          "quantity":5
        }
        """;

        webTestClient.post()
                .uri("/api/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.available").isEqualTo(true);
    }

    @Test
    void checkAvailability_validationError_returns400_withErrorMessage() {
        // Missing containerType to trigger @Valid failure
        String invalid = """
        {
          "containerSize":"20",
          "origin":"Chennai",
          "destination":"Singapore",
          "quantity":5
        }
        """;

        webTestClient.post()
                .uri("/api/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").exists(); // GlobalErrorHandler returns {"error": "..."}
    }

    @Test
    void checkAvailability_serviceError_returns500_withMessage() {
        Mockito.when(availability.checkAvailability(Mockito.any(AvailabilityRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        String body = """
        {
          "containerType":"DRY",
          "containerSize":"20",
          "origin":"Chennai",
          "destination":"Singapore",
          "quantity":5
        }
        """;

        webTestClient.post()
                .uri("/api/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Sorry there was a problem processing your request");
    }

    @Test
    void createBooking_returns201_withBookingRef_andNoLocation() {
        Mockito.when(bookings.create(Mockito.any(BookingRequest.class)))
                .thenReturn(Mono.just("957000001"));

        String body = """
        {
          "containerType":"DRY",
          "containerSize":"20",
          "origin":"Chennai",
          "destination":"Singapore",
          "quantity":5,
          "timestamp":"2020-10-12T13:53:09Z"
        }
        """;

        webTestClient.post()
                .uri("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.bookingRef").isEqualTo("957000001");
    }

    @Test
    void createBooking_validationError_returns400_withErrorMessage() {
        String invalid = """
        {
          "containerType":"DRY",
          "containerSize":"20",
          "origin":"Chennai",
          "destination":"Singapore",
          "quantity":5
        }
        """;

        webTestClient.post()
                .uri("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(invalid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void createBooking_dataAccessError_returns500_withMessage() {
        Mockito.when(bookings.create(Mockito.any(BookingRequest.class)))
                .thenReturn(Mono.error(new DataAccessException("mongo down"){}));

        String body = """
        {
          "containerType":"DRY",
          "containerSize":"20",
          "origin":"Chennai",
          "destination":"Singapore",
          "quantity":5,
          "timestamp":"2020-10-12T13:53:09Z"
        }
        """;

        webTestClient.post()
                .uri("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Sorry there was a problem processing your request");
    }
}