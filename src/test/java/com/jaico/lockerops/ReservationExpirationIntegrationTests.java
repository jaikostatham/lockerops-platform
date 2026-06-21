package com.jaico.lockerops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.compartment.infrastructure.persistence.repository.LockerCompartmentRepository;
import com.jaico.lockerops.reservation.application.service.ReservationExpirationService;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.jaico.lockerops.station.domain.enums.LockerStationStatus;
import com.jaico.lockerops.station.domain.model.LockerStation;
import com.jaico.lockerops.station.infrastructure.persistence.repository.LockerStationRepository;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
import com.jaico.lockerops.ticket.application.service.AccessCodeHasher;
import com.jaico.lockerops.ticket.application.service.TicketService;
import com.jaico.lockerops.ticket.domain.enums.AccessCodeStatus;
import com.jaico.lockerops.ticket.domain.enums.TicketStatus;
import com.jaico.lockerops.ticket.domain.model.AccessCode;
import com.jaico.lockerops.ticket.domain.model.Ticket;
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
class ReservationExpirationIntegrationTests {

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
    private TicketRepository ticketRepository;

    @Autowired
    private AccessCodeRepository accessCodeRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ReservationExpirationService reservationExpirationService;

    @Autowired
    private AccessCodeHasher accessCodeHasher;

    @BeforeEach
    void setUp() {
        accessCodeRepository.deleteAll();
        ticketRepository.deleteAll();
        reservationRepository.deleteAll();
        lockerCompartmentRepository.deleteAll();
        lockerStationRepository.deleteAll();
    }

    @Test
    void expireDueReservationUpdatesFullFlow() {
        TestReservation testReservation = createReservationWithTicket(
                ReservationStatus.CONFIRMED,
                LockerCompartmentStatus.RESERVED,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );

        int expiredCount = reservationExpirationService.expireDueReservations();

        Reservation reservation = reservationRepository.findById(testReservation.reservation().getId()).orElseThrow();
        LockerCompartment lockerCompartment = lockerCompartmentRepository
                .findById(testReservation.lockerCompartment().getId())
                .orElseThrow();
        Ticket ticket = ticketRepository.findByReservation_Id(reservation.getId()).orElseThrow();
        AccessCode accessCode = accessCodeRepository
                .findByTicket_IdAndCodeHash(ticket.getId(), accessCodeHasher.hash(testReservation.accessCode()))
                .orElseThrow();

        assertThat(expiredCount).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(reservation.getExpiredAt()).isNotNull();
        assertThat(lockerCompartment.getStatus()).isEqualTo(LockerCompartmentStatus.AVAILABLE);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        assertThat(ticket.getExpiredAt()).isNotNull();
        assertThat(accessCode.getStatus()).isEqualTo(AccessCodeStatus.EXPIRED);
    }

    @Test
    void notDueReservationIsNotExpired() {
        TestReservation testReservation = createReservationWithTicket(
                ReservationStatus.CONFIRMED,
                LockerCompartmentStatus.RESERVED,
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        int expiredCount = reservationExpirationService.expireDueReservations();

        Reservation reservation = reservationRepository.findById(testReservation.reservation().getId()).orElseThrow();
        Ticket ticket = ticketRepository.findByReservation_Id(reservation.getId()).orElseThrow();

        assertThat(expiredCount).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getExpiredAt()).isNull();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ISSUED);
    }

    @Test
    void cancelledReservationIsNotChanged() {
        Reservation reservation = createReservation(
                ReservationStatus.CANCELLED,
                LockerCompartmentStatus.AVAILABLE,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );
        Instant cancelledAt = Instant.now().minus(20, ChronoUnit.MINUTES);
        reservation.setCancelledAt(cancelledAt);
        reservationRepository.saveAndFlush(reservation);

        int expiredCount = reservationExpirationService.expireDueReservations();

        Reservation storedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(expiredCount).isZero();
        assertThat(storedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(storedReservation.getCancelledAt()).isNotNull();
        assertThat(storedReservation.getExpiredAt()).isNull();
    }

    @Test
    void expiredReservationProcessIsIdempotent() {
        TestReservation testReservation = createReservationWithTicket(
                ReservationStatus.CONFIRMED,
                LockerCompartmentStatus.RESERVED,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );

        int firstRunCount = reservationExpirationService.expireDueReservations();
        int secondRunCount = reservationExpirationService.expireDueReservations();

        Reservation reservation = reservationRepository.findById(testReservation.reservation().getId()).orElseThrow();
        LockerCompartment lockerCompartment = lockerCompartmentRepository
                .findById(testReservation.lockerCompartment().getId())
                .orElseThrow();

        assertThat(firstRunCount).isEqualTo(1);
        assertThat(secondRunCount).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(lockerCompartment.getStatus()).isEqualTo(LockerCompartmentStatus.AVAILABLE);
    }

    @Test
    void schedulerCanRunWithoutDueReservations() {
        int expiredCount = reservationExpirationService.expireDueReservations();

        assertThat(expiredCount).isZero();
    }

    @Test
    void accessCodeValidationAfterExpirationFails() throws Exception {
        TestReservation testReservation = createReservationWithTicket(
                ReservationStatus.CONFIRMED,
                LockerCompartmentStatus.RESERVED,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );

        reservationExpirationService.expireDueReservations();

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "ticketCode", testReservation.ticketCode(),
                                "accessCode", testReservation.accessCode()
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(5102));
    }

    private TestReservation createReservationWithTicket(
            ReservationStatus reservationStatus,
            LockerCompartmentStatus lockerCompartmentStatus,
            Instant reservedFrom,
            Instant reservedUntil
    ) {
        Reservation reservation = createReservation(
                reservationStatus,
                lockerCompartmentStatus,
                reservedFrom,
                reservedUntil
        );
        ReservationTicketResponse ticketResponse = ticketService.issueTicketForReservation(reservation);

        return new TestReservation(
                reservation,
                reservation.getLockerCompartment(),
                ticketResponse.getTicketCode(),
                ticketResponse.getAccessCode()
        );
    }

    private Reservation createReservation(
            ReservationStatus reservationStatus,
            LockerCompartmentStatus lockerCompartmentStatus,
            Instant reservedFrom,
            Instant reservedUntil
    ) {
        LockerCompartment lockerCompartment = createLockerCompartment(lockerCompartmentStatus);
        Reservation reservation = new Reservation(
                lockerCompartment,
                reservationStatus,
                reservedFrom,
                reservedUntil,
                "customer-expiration-test"
        );

        return reservationRepository.saveAndFlush(reservation);
    }

    private LockerCompartment createLockerCompartment(LockerCompartmentStatus status) {
        LockerStation lockerStation = new LockerStation();
        lockerStation.setName("Locker Station Expiration Test");
        lockerStation.setModel("LOCKER PRO");
        lockerStation.setManufacturer("LOCKEROPS");
        lockerStation.setStatus(LockerStationStatus.ACTIVE);
        lockerStation.setLocation("Madrid Test");

        LockerStation savedLockerStation = lockerStationRepository.saveAndFlush(lockerStation);

        LockerCompartment lockerCompartment = new LockerCompartment(
                1,
                LockerCompartmentSize.MEDIUM,
                status,
                savedLockerStation
        );

        return lockerCompartmentRepository.saveAndFlush(lockerCompartment);
    }

    private record TestReservation(
            Reservation reservation,
            LockerCompartment lockerCompartment,
            String ticketCode,
            String accessCode
    ) {
    }
}
