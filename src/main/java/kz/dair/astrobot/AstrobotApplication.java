package kz.dair.astrobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AstrobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstrobotApplication.class, args);
    }

}
