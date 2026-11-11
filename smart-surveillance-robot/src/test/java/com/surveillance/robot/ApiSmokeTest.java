package com.surveillance.robot;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Runs against the in-memory H2 database (profile "dev"). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiSmokeTest {

    private static final String KEY = "change-this-robot-key";

    @Autowired
    MockMvc mvc;

    @Test
    void robotEndpointsRequireApiKey() throws Exception {
        mvc.perform(post("/api/robot/SPSR-01/heartbeat")).andExpect(status().isUnauthorized());
    }

    @Test
    void operatorEndpointsRequireLogin() throws Exception {
        mvc.perform(get("/api/robots")).andExpect(status().isUnauthorized());
    }

    @Test
    void detectionCreatesAlertAndStopCommand() throws Exception {
        mvc.perform(post("/api/robot/SPSR-01/detections")
                        .header("X-API-KEY", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectClass\":\"person\",\"confidence\":0.9,\"latitude\":17.54,\"longitude\":78.57}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertSent").value(true));

        mvc.perform(get("/api/robot/SPSR-01/commands/next").header("X-API-KEY", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.command").value("STOP"));

        mvc.perform(get("/api/alerts?acknowledged=false").with(httpBasic("operator", "Operator@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void invalidDetectionIsRejected() throws Exception {
        mvc.perform(post("/api/robot/SPSR-01/detections")
                        .header("X-API-KEY", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objectClass\":\"\",\"confidence\":2}"))
                .andExpect(status().isBadRequest());
    }
}
