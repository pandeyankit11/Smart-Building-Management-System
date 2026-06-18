package com.smartbuilding.api;

import com.smartbuilding.service.BuildingManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * Spring Boot starter for the SBMS REST wrapper.
 * It creates a singleton BuildingManager, runs the demo‑data initializer,
 * and then leaves the manager ready for the REST controller to query.
 */
@SpringBootApplication
public class SmartBuildingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBuildingApiApplication.class, args);
    }

    /**
     * Provide a singleton BuildingManager bean.
     * The constructor mirrors the console app (building name & address).
     */
    @Bean
    public BuildingManager buildingManager(
            @Value("${sbms.data-directory:data}") String dataDirectory) {
        BuildingManager mgr = new BuildingManager(
                "Smart Residential Complex",
                "123 Innovation Drive",
                dataDirectory,
                new Scanner(System.in));
        // Initialise demo data exactly as the console app does.
        mgr.initializeDemoData();
        return mgr;
    }

    /** Optional start‑up log so we know the API is up. */
    @Bean
    public CommandLineRunner startupLogger(BuildingManager mgr) {
        return args -> System.out.println("[SBMS‑API] Demo data loaded – API ready");
    }
}
