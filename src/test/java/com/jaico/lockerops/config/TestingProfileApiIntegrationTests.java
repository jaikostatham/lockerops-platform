package com.jaico.lockerops.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentSize;
import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.compartment.infrastructure.persistence.repository.LockerCompartmentRepository;
import com.jaico.lockerops.station.domain.enums.LockerStationStatus;
import com.jaico.lockerops.station.domain.model.LockerStation;
import com.jaico.lockerops.station.infrastructure.persistence.repository.LockerStationRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lockerops_public_api_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "DB_MIGRATION_USERNAME=sa",
        "DB_MIGRATION_PASSWORD=",
        "CORS_ALLOWED_ORIGINS=http://localhost:9000",
        "ACCESS_CODE_HASH_SECRET=test-only-hash-secret-for-integration-tests"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "testing"})
class TestingProfileApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LockerStationRepository lockerStationRepository;

    @Autowired
    private LockerCompartmentRepository lockerCompartmentRepository;

    @Test
    void testingProfileExposesTheKioskFlowAndHidesAdministrativeEndpoints() throws Exception {
        mockMvc.perform(get("/api/locker-stations")
                        .header("Origin", "http://localhost:9000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:9000"));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/locker-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testingProfileCompletesReservationPaymentAndAccessValidation() throws Exception {
        LockerStation station = new LockerStation();
        station.setName("Testing kiosk station");
        station.setModel("LOCKER PRO");
        station.setManufacturer("LOCKEROPS");
        station.setStatus(LockerStationStatus.ACTIVE);
        station.setLocation("Testing environment");
        LockerStation savedStation = lockerStationRepository.save(station);

        LockerCompartment compartment = lockerCompartmentRepository.save(new LockerCompartment(
                1,
                LockerCompartmentSize.MEDIUM,
                LockerCompartmentStatus.AVAILABLE,
                savedStation
        ));

        String reservationResponse = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lockerCompartmentId", compartment.getId(),
                                "durationMinutes", 60,
                                "customerReference", "TESTING-KIOSK-FLOW"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long reservationId = objectMapper.readTree(reservationResponse).get("id").asLong();

        String paymentResponse = mockMvc.perform(post("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reservationId", reservationId,
                                "outcome", "APPROVED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.ticket.accessCode").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode ticket = objectMapper.readTree(paymentResponse).get("ticket");

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "ticketCode", ticket.get("ticketCode").asText(),
                                "accessCode", ticket.get("accessCode").asText()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));
    }
}
