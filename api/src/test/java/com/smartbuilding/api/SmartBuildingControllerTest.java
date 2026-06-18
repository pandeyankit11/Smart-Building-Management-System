package com.smartbuilding.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "sbms.data-directory=target/test-data/api")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SmartBuildingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void frontendServesAllTwelveMenuOptions() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("['login', 'Login']")))
                .andExpect(content().string(containsString("['logout', 'Logout']")))
                .andExpect(content().string(containsString("['building', 'Building & Equipment']")))
                .andExpect(content().string(containsString("['occupancy', 'Occupancy']")))
                .andExpect(content().string(containsString("['lighting', 'Lighting']")))
                .andExpect(content().string(containsString("['security', 'Security']")))
                .andExpect(content().string(containsString("['alerts', 'Alerts']")))
                .andExpect(content().string(containsString("['reports', 'Reports']")))
                .andExpect(content().string(containsString("['status', 'View Complete Status']")))
                .andExpect(content().string(containsString("['save', 'Save Data']")))
                .andExpect(content().string(containsString("['load', 'Load Data']")))
                .andExpect(content().string(containsString("['exit', 'Exit']")));
    }

    @Test
    void loginAndLogoutMenuOptionsWorkWithFixedDemoUsers() throws Exception {
        mockMvc.perform(get("/api/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedIn").value(false));

        login("admin", "admin123", "ADMINISTRATOR");
        mockMvc.perform(get("/api/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedIn").value(true))
                .andExpect(jsonPath("$.username").value("admin"));

        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_ERROR"));
    }

    @Test
    void buildingAndEquipmentMenuSupportsViewCreateEditDeleteAndValidation() throws Exception {
        login("admin", "admin123", "ADMINISTRATOR");

        mockMvc.perform(get("/api/building").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floorCount").value(2));

        mockMvc.perform(put("/api/building")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Complex\",\"address\":\"456 Test Avenue\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Complex"));

        mockMvc.perform(post("/api/floors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Second Floor\",\"number\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floorName").value("Second Floor"));

        mockMvc.perform(put("/api/floors/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Operations Floor\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floorName").value("Operations Floor"));

        String roomId = json(postJson("/api/rooms",
                "{\"floorNumber\":3,\"name\":\"Ops Lab\",\"location\":\"Floor 3 East\",\"capacity\":4}"), "$.componentId");

        mockMvc.perform(put("/api/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ops Lab Updated\",\"location\":\"Floor 3 West\",\"capacity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ops Lab Updated"));

        String equipmentId = json(postJson("/api/equipment",
                "{\"roomId\":\"" + roomId + "\",\"name\":\"Air Sensor\",\"type\":\"SENSOR\",\"energyConsumption\":3.5}"),
                "$.equipmentId");

        mockMvc.perform(put("/api/equipment/" + equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Air Sensor 2\",\"location\":\"Ops Lab\",\"type\":\"SENSOR\",\"energyConsumption\":4.0,\"status\":\"MAINTENANCE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        mockMvc.perform(post("/api/floors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Duplicate\",\"number\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Floor number already exists")));

        mockMvc.perform(delete("/api/equipment/" + equipmentId)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/rooms/" + roomId)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/floors/3")).andExpect(status().isOk());
    }

    @Test
    void occupancyMenuSupportsViewSetIncrementDecrementAndBounds() throws Exception {
        login("admin", "admin123", "ADMINISTRATOR");
        String roomId = json(postJson("/api/rooms",
                "{\"floorNumber\":1,\"name\":\"Tiny Room\",\"location\":\"Test Wing\",\"capacity\":2}"), "$.componentId");

        mockMvc.perform(get("/api/rooms").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(6)));

        mockMvc.perform(post("/api/occupancy/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentOccupancy").value(1));

        mockMvc.perform(post("/api/occupancy/" + roomId + "/increment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentOccupancy").value(2));

        mockMvc.perform(post("/api/occupancy/" + roomId + "/increment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("full capacity")));

        mockMvc.perform(post("/api/occupancy/" + roomId + "/decrement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentOccupancy").value(1));

        mockMvc.perform(post("/api/occupancy/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Expected format: 0-2")));
    }

    @Test
    void lightingMenuSupportsAllControlsAndValidation() throws Exception {
        login("staff", "staff123", "MAINTENANCE");
        String lightId = json(postJson("/api/lights", "{\"location\":\"Test Courtyard\"}"), "$.lightId");

        mockMvc.perform(get("/api/lighting").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(7)));

        mockMvc.perform(put("/api/lights/" + lightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"Updated Courtyard\",\"brightness\":75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brightness").value(75));

        mockMvc.perform(post("/api/lights/" + lightId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.on").value(true));

        mockMvc.perform(post("/api/lights/" + lightId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.on").value(false));

        mockMvc.perform(post("/api/lights/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"on\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].on").value(true));

        mockMvc.perform(post("/api/lighting/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"onTime\":\"18:00\",\"offTime\":\"06:00\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/lighting/auto")).andExpect(status().isOk());

        mockMvc.perform(put("/api/lights/" + lightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"Updated Courtyard\",\"brightness\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("brightness")));

        mockMvc.perform(delete("/api/lights/" + lightId)).andExpect(status().isOk());
    }

    @Test
    void securityMenuSupportsLogsIncidentsAlarmsSurveillanceAndRoles() throws Exception {
        login("user", "user123", "GENERAL_USER");
        mockMvc.perform(post("/api/security/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TEST\",\"location\":\"Lobby\"}"))
                .andExpect(status().isForbidden());

        login("security", "sec123", "SECURITY");
        String logId = json(postJson("/api/security/access-logs",
                "{\"userId\":\"VISITOR-1\",\"location\":\"Lobby\",\"accessType\":\"ENTRY\",\"authorized\":false}"),
                "$.logId");

        String incidentId = json(postJson("/api/security/incidents",
                "{\"type\":\"ACCESS\",\"description\":\"Unauthorized entry\",\"severity\":\"HIGH\"}"),
                "$.incidentId");

        mockMvc.perform(put("/api/security/incidents/" + incidentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVESTIGATING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVESTIGATING"));

        String alarmId = json(postJson("/api/security/alarms",
                "{\"type\":\"INTRUSION\",\"location\":\"Lobby\"}"), "$.alarmId");

        mockMvc.perform(post("/api/security/alarms/" + alarmId + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/security/surveillance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surveillanceActive").value(false));

        mockMvc.perform(get("/api/security").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessLogs.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.incidents.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.alarms.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/security/access-logs/" + logId)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/security/incidents/" + incidentId)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/security/alarms/" + alarmId)).andExpect(status().isOk());
    }

    @Test
    void alertsMenuSupportsActiveResolvedLifecycleAndValidation() throws Exception {
        login("admin", "admin123", "ADMINISTRATOR");

        String alertId = json(postJson("/api/alerts",
                "{\"type\":\"SYSTEM_ERROR\",\"message\":\"Smoke test alert\",\"severity\":\"INFO\"}"),
                "$.alertId");

        mockMvc.perform(put("/api/alerts/" + alertId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Updated alert\",\"severity\":\"WARNING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("WARNING"));

        mockMvc.perform(post("/api/alerts/" + alertId + "/ack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true));

        mockMvc.perform(post("/api/alerts/" + alertId + "/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/alerts/" + alertId)).andExpect(status().isOk());

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SYSTEM_ERROR\",\"message\":\"Bad severity\",\"severity\":\"BAD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid alert severity")));
    }

    @Test
    void reportsStatusSaveLoadAndExitRelatedActionsWork() throws Exception {
        login("admin", "admin123", "ADMINISTRATOR");

        mockMvc.perform(get("/api/reports/ENERGY").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ENERGY"))
                .andExpect(jsonPath("$.text", containsString("ENERGY CONSUMPTION REPORT")));

        mockMvc.perform(get("/api/reports/combined").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", containsString("COMBINED SYSTEM REPORT")));

        mockMvc.perform(post("/api/reports/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ENERGY\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", containsString("Building:")));

        mockMvc.perform(post("/api/save")).andExpect(status().isOk());
        mockMvc.perform(post("/api/load")).andExpect(status().isOk());

        mockMvc.perform(post("/api/logout")).andExpect(status().isOk());
        mockMvc.perform(post("/api/save")).andExpect(status().isForbidden());
    }

    private void login(String username, String password, String role) throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(role));
    }

    private MvcResult postJson(String path, String json) throws Exception {
        return mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String json(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }
}
