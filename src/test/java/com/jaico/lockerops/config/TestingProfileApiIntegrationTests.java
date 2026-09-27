package com.jaico.lockerops.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lockerops_public_api_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "testing"})
class TestingProfileApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testingProfileExposesOnlyThePublicCatalog() throws Exception {
        mockMvc.perform(get("/api/locker-stations")
                        .header("Origin", "http://localhost:9000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:9000"));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/locker-stations"))
                .andExpect(status().isMethodNotAllowed());
    }
}
