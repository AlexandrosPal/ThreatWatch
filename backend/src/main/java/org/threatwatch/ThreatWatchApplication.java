package org.threatwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class ThreatWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreatWatchApplication.class, args);
    }

}
