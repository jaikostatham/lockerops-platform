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

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractIntegrationTests {

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
    void lockerStationCrudExposesExpectedContract() throws Exception {
        String createResponse = mockMvc.perform(post("/api/locker-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Locker Station Contract",
                                "model", "LOCKER PRO",
                                "manufacturer", "LOCKEROPS",
                                "status", "ACTIVE",
                                "location", "Madrid Centro",
                                "imageUrl", "https://example.com/station.jpg"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Locker Station Contract"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long stationId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/locker-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(stationId));

        mockMvc.perform(get("/api/locker-stations/{id}", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stationId))
                .andExpect(jsonPath("$.location").value("Madrid Centro"));

        mockMvc.perform(put("/api/locker-stations/{id}", stationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Locker Station Updated",
                                "model", "LOCKER PRO V2",
                                "manufacturer", "LOCKEROPS",
                                "status", "MAINTENANCE",
                                "location", "Madrid Norte",
                                "imageUrl", "https://example.com/station-updated.jpg"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stationId))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        mockMvc.perform(delete("/api/locker-stations/{id}", stationId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/locker-stations/{id}", stationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(2001))
                .andExpect(jsonPath("$.path").value("/api/locker-stations/" + stationId));
    }

    @Test
    void lockerCompartmentEndpointsExposeExpectedContract() throws Exception {
        LockerStation lockerStation = createLockerStation();

        String createResponse = mockMvc.perform(post("/api/locker-stations/{lockerStationId}/compartments", lockerStation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "compartmentNumber", 1,
                                "size", "MEDIUM",
                                "status", "AVAILABLE"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.lockerStationId").value(lockerStation.getId()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long compartmentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(post("/api/locker-stations/{lockerStationId}/compartments", lockerStation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "compartmentNumber", 1,
                                "size", "SMALL",
                                "status", "AVAILABLE"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(3002));

        mockMvc.perform(get("/api/locker-stations/{lockerStationId}/compartments", lockerStation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(compartmentId));

        mockMvc.perform(get("/api/locker-compartments/{id}", compartmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartmentNumber").value(1))
                .andExpect(jsonPath("$.size").value("MEDIUM"));

        mockMvc.perform(put("/api/locker-compartments/{id}", compartmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "compartmentNumber", 2,
                                "size", "LARGE",
                                "status", "OUT_OF_SERVICE"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartmentNumber").value(2))
                .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"));

        mockMvc.perform(delete("/api/locker-compartments/{id}", compartmentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void reservationCreationAndCancellationExposeExpectedContract() throws Exception {
        LockerCompartment lockerCompartment = createLockerCompartment(LockerCompartmentStatus.AVAILABLE);

        String createResponse = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "lockerCompartmentId", lockerCompartment.getId(),
                                "durationMinutes", 120,
                                "customerReference", "KIOSK-CONTRACT-001"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").isNumber())
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.lockerCompartmentId").value(lockerCompartment.getId()))
                .andExpect(jsonPath("$.ticketCode").isNotEmpty())
                .andExpect(jsonPath("$.accessCode").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(createResponse);
        Long reservationId = response.get("reservationId").asLong();
        String ticketCode = response.get("ticketCode").asText();
        String accessCode = response.get("accessCode").asText();

        mockMvc.perform(get("/api/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.lockerCompartmentId").value(lockerCompartment.getId()));

        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "ticketCode", ticketCode,
                                "accessCode", accessCode
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(5103));
    }

    @Test
    void accessCodeValidationRequiresTicketCodeAndAccessCodeFields() throws Exception {
        mockMvc.perform(post("/api/access-codes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.path").value("/api/access-codes/validate"))
                .andExpect(jsonPath("$.fieldErrors", hasKey("ticketCode")))
                .andExpect(jsonPath("$.fieldErrors", hasKey("accessCode")));
    }

    @Test
    void invalidRequestBodyUsesApiErrorResponseContract() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lockerCompartmentId": "invalid-id",
                                  "durationMinutes": 120
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.path").value("/api/reservations"))
                .andExpect(jsonPath("$.fieldErrors.requestBody").isArray());
    }

    @Test
    void reservationCreationDoesNotExposeStoredAccessCodeFields() throws Exception {
        LockerCompartment lockerCompartment = createLockerCompartment(LockerCompartmentStatus.AVAILABLE);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "lockerCompartmentId", lockerCompartment.getId(),
                                "durationMinutes", 60
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessCode").isNotEmpty())
                .andExpect(jsonPath("$", not(hasKey("codeHash"))))
                .andExpect(jsonPath("$", not(hasKey("codePreview"))));
    }

    private String json(Map<String, Object> payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }

    private LockerStation createLockerStation() {
        LockerStation lockerStation = new LockerStation();
        lockerStation.setName("Locker Station Contract Seed");
        lockerStation.setModel("LOCKER PRO");
        lockerStation.setManufacturer("LOCKEROPS");
        lockerStation.setStatus(LockerStationStatus.ACTIVE);
        lockerStation.setLocation("Madrid Test");

        return lockerStationRepository.save(lockerStation);
    }

    private LockerCompartment createLockerCompartment(LockerCompartmentStatus status) {
        LockerStation lockerStation = createLockerStation();
        LockerCompartment lockerCompartment = new LockerCompartment(
                1,
                LockerCompartmentSize.MEDIUM,
                status,
                lockerStation
        );

        return lockerCompartmentRepository.save(lockerCompartment);
    }
}
