package ir.darkdeveloper.anbarinoo;

import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminUserProperties.class)
public class AnbarinooApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnbarinooApplication.class, args);
    }

}