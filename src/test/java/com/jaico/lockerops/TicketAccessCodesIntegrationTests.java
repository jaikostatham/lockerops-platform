package com.jaico.lockerops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.compartment.infrastructure.persistence.repository.LockerCompartmentRepository;
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

    @BeforeEach
    void setUp() {
        accessCodeRepository.deleteAll();
        ticketRepository.deleteAll();
        reservationRepository.deleteAll();
        lockerCompartmentRepository.deleteAll();
        lockerStationRepository.deleteAll();
    }

    @Test
    void createReservationIssuesTicketAndReusableAccessCode() throws Exception {
        LockerCompartment lockerCompartment = createAvailableLockerCompartment();

        String createReservationResponse = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lockerCompartmentId", lockerCompartment.getId(),
                                "durationMinutes", 60,
                                "customerReference", "customer-001"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").isNumber())
                .andExpect(jsonPath("$.ticketCode").isNotEmpty())
                .andExpect(jsonPath("$.accessCode").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(createReservationResponse);
        String accessCode = response.get("accessCode").asText();

        validateAccessCode(accessCode);
        validateAccessCode(accessCode);

        assertThat(accessCodeRepository.findByCode(accessCode))
                .isPresent()
                .get()
                .extracting(storedAccessCode -> storedAccessCode.getUseCount())
                .isEqualTo(2);
    }

    @Test
    void cancelReservationRevokesActiveAccessCode() throws Exception {
        LockerCompartment lockerCompartment = createAvailableLockerCompartment();

        String createReservationResponse = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lockerCompartmentId", lockerCompartment.getId(),
                                "durationMinutes", 60
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(createReservationResponse);
        Long reservationId = response.get("reservationId").asLong();
        String accessCode = response.get("accessCode").asText();

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", accessCode
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(5103));
    }

    private void validateAccessCode(String accessCode) throws Exception {
        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", accessCode
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true))
                .andExpect(jsonPath("$.ticketCode").isNotEmpty());
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
