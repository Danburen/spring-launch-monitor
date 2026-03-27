package io.github.danburen.springlaunchmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringLaunchMonitorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringLaunchMonitorApplication.class);
        app.run(args);
    }
}
