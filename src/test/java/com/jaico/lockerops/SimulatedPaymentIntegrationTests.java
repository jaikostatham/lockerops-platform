package com.jaico.lockerops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.compartment.infrastructure.persistence.repository.LockerCompartmentRepository;
import com.jaico.lockerops.payment.domain.enums.PaymentStatus;
import com.jaico.lockerops.payment.infrastructure.persistence.repository.PaymentAttemptRepository;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.jaico.lockerops.station.domain.enums.LockerStationStatus;
import com.jaico.lockerops.station.domain.model.LockerStation;
import com.jaico.lockerops.station.infrastructure.persistence.repository.LockerStationRepository;
import com.jaico.lockerops.ticket.infrastructure.persistence.repository.AccessCodeRepository;
import com.jaico.lockerops.ticket.infrastructure.persistence.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimulatedPaymentIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LockerStationRepository lockerStationRepository;

    @Autowired
    private LockerCompartmentRepository lockerCompartmentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AccessCodeRepository accessCodeRepository;

    @BeforeEach
    void setUp() {
        accessCodeRepository.deleteAll();
        ticketRepository.deleteAll();
        paymentAttemptRepository.deleteAll();
        reservationRepository.deleteAll();
        lockerCompartmentRepository.deleteAll();
        lockerStationRepository.deleteAll();
    }

    @Test
    void approvedPaymentConfirmsReservationAndIssuesCredentials() throws Exception {
        LockerCompartment compartment = createAvailableCompartment(LockerCompartmentSize.LARGE);
        long reservationId = createPendingReservation(compartment, 90);

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationReference", reservationReferenceFor(reservationId),
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("APPROVED"))
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.amountMinor").value(600))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.ticket.ticketCode").isNotEmpty())
                .andExpect(jsonPath("$.ticket.accessCode").isNotEmpty());

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(paymentAttemptRepository.findAll())
                .singleElement()
                .extracting(payment -> payment.getStatus())
                .isEqualTo(PaymentStatus.APPROVED);
        assertThat(ticketRepository.findByReservation_Id(reservationId)).isPresent();
    }

    @Test
    void declinedPaymentKeepsReservationPendingAndAllowsRetry() throws Exception {
        LockerCompartment compartment = createAvailableCompartment(LockerCompartmentSize.SMALL);
        long reservationId = createPendingReservation(compartment, 60);

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationReference", reservationReferenceFor(reservationId),
                                "outcome", "DECLINED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("DECLINED"))
                .andExpect(jsonPath("$.reservationStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.ticket").doesNotExist());

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationReference", reservationReferenceFor(reservationId),
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"));

        assertThat(paymentAttemptRepository.findAll()).hasSize(2);
    }

    @Test
    void confirmedReservationCannotBePaidTwice() throws Exception {
        LockerCompartment compartment = createAvailableCompartment(LockerCompartmentSize.MEDIUM);
        long reservationId = createPendingReservation(compartment, 60);

        simulateApprovedPayment(reservationId);

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationReference", reservationReferenceFor(reservationId),
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(6001));
    }

    @Test
    void expiredPaymentWindowIsRejected() throws Exception {
        LockerCompartment compartment = createAvailableCompartment(LockerCompartmentSize.MEDIUM);
        Instant now = Instant.now();
        Reservation reservation = new Reservation(
                compartment,
                ReservationStatus.PENDING_PAYMENT,
                now,
                now.plus(1, ChronoUnit.HOURS),
                "expired-payment-window",
                200L,
                "EUR",
                now.minus(1, ChronoUnit.MINUTES)
        );
        reservationRepository.saveAndFlush(reservation);

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationReference", reservation.getReservationReference().toString(),
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(6002));

        assertThat(paymentAttemptRepository.findAll()).isEmpty();
        assertThat(ticketRepository.findByReservation_Id(reservation.getId())).isEmpty();
    }

    @Test
    void paymentCannotBeRequestedWithPredictableInternalReservationId() throws Exception {
        LockerCompartment compartment = createAvailableCompartment(LockerCompartmentSize.MEDIUM);
        long reservationId = createPendingReservation(compartment, 60);

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationId", reservationId,
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isBadRequest());

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(paymentAttemptRepository.findAll()).isEmpty();
        assertThat(ticketRepository.findByReservation_Id(reservationId)).isEmpty();
    }

    private long createPendingReservation(
            LockerCompartment compartment,
            int durationMinutes
    ) throws Exception {
        String response = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "lockerCompartmentId", compartment.getId(),
                                "durationMinutes", durationMinutes
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode reservation = objectMapper.readTree(response);
        return reservation.get("id").asLong();
    }

    private void simulateApprovedPayment(long reservationId) throws Exception {
        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reservationReference", reservationReferenceFor(reservationId),
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isOk());
    }

    private String reservationReferenceFor(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow()
                .getReservationReference()
                .toString();
    }

    private LockerCompartment createAvailableCompartment(LockerCompartmentSize size) {
        LockerStation station = new LockerStation();
        station.setName("Locker Station Payment Test");
        station.setModel("LOCKER PRO");
        station.setManufacturer("LOCKEROPS");
        station.setStatus(LockerStationStatus.ACTIVE);
        station.setLocation("Madrid Test");
        LockerStation savedStation = lockerStationRepository.saveAndFlush(station);

        LockerCompartment compartment = new LockerCompartment(
                1,
                size,
                LockerCompartmentStatus.AVAILABLE,
                savedStation
        );

        return lockerCompartmentRepository.saveAndFlush(compartment);
    }

    private String json(Map<String, Object> payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }
}
