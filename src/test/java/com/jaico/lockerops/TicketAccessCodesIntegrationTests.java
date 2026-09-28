package com.jaico.lockerops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.compartment.infrastructure.persistence.repository.LockerCompartmentRepository;
import com.jaico.lockerops.payment.infrastructure.persistence.repository.PaymentAttemptRepository;
import com.jaico.lockerops.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.jaico.lockerops.station.domain.enums.LockerStationStatus;
import com.jaico.lockerops.station.domain.model.LockerStation;
import com.jaico.lockerops.station.infrastructure.persistence.repository.LockerStationRepository;
import com.jaico.lockerops.ticket.application.service.AccessCodeHasher;
import com.jaico.lockerops.ticket.domain.enums.AccessCodeStatus;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketAccessCodesIntegrationTests {

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
    private AccessCodeHasher accessCodeHasher;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

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
    void createReservationIssuesTicketAndReusableAccessCode() throws Exception {
        LockerCompartment lockerCompartment = createAvailableLockerCompartment();

        String createReservationResponse = createAndApproveReservation(
                lockerCompartment,
                "customer-001"
        );

        JsonNode response = objectMapper.readTree(createReservationResponse);
        String ticketCode = response.get("ticketCode").asText();
        String accessCode = response.get("accessCode").asText();

        validateAccessCode(ticketCode, accessCode);
        validateAccessCode(ticketCode, accessCode);

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode).orElseThrow();
        AccessCode storedAccessCode = accessCodeRepository
                .findByTicket_IdAndCodeHash(ticket.getId(), accessCodeHasher.hash(accessCode))
                .orElseThrow();

        assertThat(storedAccessCode.getCodeHash()).isNotEqualTo(accessCode);
        assertThat(storedAccessCode.getCodePreview()).isNotEqualTo(accessCode);
        assertThat(storedAccessCode.getCodePreview()).isNotBlank();
        assertThat(storedAccessCode.getStatus()).isEqualTo(AccessCodeStatus.ACTIVE);
        assertThat(storedAccessCode.getUseCount()).isEqualTo(2);
    }

    @Test
    void validateAccessCodeWithInvalidCredentialsReturnsGenericNotFoundError() throws Exception {
        LockerCompartment lockerCompartment = createAvailableLockerCompartment();

        String createReservationResponse = createAndApproveReservation(lockerCompartment, null);

        JsonNode response = objectMapper.readTree(createReservationResponse);
        String ticketCode = response.get("ticketCode").asText();
        String accessCode = response.get("accessCode").asText();

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "ticketCode", "TCK-INVALID",
                                "accessCode", accessCode
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(5106));

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "ticketCode", ticketCode,
                                "accessCode", "BADCODE"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(5106));

        assertThat(accessCodeRepository.findAll())
                .allMatch(storedAccessCode -> !storedAccessCode.getCodeHash().equals(accessCode));
    }

    @Test
    void cancelReservationRevokesActiveAccessCode() throws Exception {
        LockerCompartment lockerCompartment = createAvailableLockerCompartment();

        String createReservationResponse = createAndApproveReservation(lockerCompartment, null);

        JsonNode response = objectMapper.readTree(createReservationResponse);
        Long reservationId = response.get("reservationId").asLong();
        String ticketCode = response.get("ticketCode").asText();
        String accessCode = response.get("accessCode").asText();

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "ticketCode", ticketCode,
                                "accessCode", accessCode
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(5103));
    }

    private void validateAccessCode(String ticketCode, String accessCode) throws Exception {
        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "ticketCode", ticketCode,
                                "accessCode", accessCode
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true))
                .andExpect(jsonPath("$.ticketCode").isNotEmpty());
    }

    private String createAndApproveReservation(
            LockerCompartment lockerCompartment,
            String customerReference
    ) throws Exception {
        Map<String, Object> reservationPayload = new java.util.LinkedHashMap<>();
        reservationPayload.put("lockerCompartmentId", lockerCompartment.getId());
        reservationPayload.put("durationMinutes", 60);
        if (customerReference != null) {
            reservationPayload.put("customerReference", customerReference);
        }

        String pendingReservation = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reservationId = objectMapper.readTree(pendingReservation).get("id").asLong();

        String paymentResponse = mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reservationId", reservationId,
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("APPROVED"))
                .andExpect(jsonPath("$.ticket.ticketCode").isNotEmpty())
                .andExpect(jsonPath("$.ticket.accessCode").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payment = objectMapper.readTree(paymentResponse);
        return payment.get("ticket").toString();
    }

    private LockerCompartment createAvailableLockerCompartment() {
        LockerStation lockerStation = new LockerStation();
        lockerStation.setName("Locker Station Test");
        lockerStation.setModel("LOCKER PRO");
        lockerStation.setManufacturer("LOCKEROPS");
        lockerStation.setStatus(LockerStationStatus.ACTIVE);
        lockerStation.setLocation("Madrid Test");

        LockerStation savedLockerStation = lockerStationRepository.save(lockerStation);

        LockerCompartment lockerCompartment = new LockerCompartment(
                1,
                LockerCompartmentSize.MEDIUM,
                LockerCompartmentStatus.AVAILABLE,
                savedLockerStation
        );

        return lockerCompartmentRepository.save(lockerCompartment);
    }
}
