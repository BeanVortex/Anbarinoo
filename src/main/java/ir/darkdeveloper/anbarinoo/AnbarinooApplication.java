package ir.darkdeveloper.anbarinoo;

import ir.darkdeveloper.anbarinoo.config.SecurityHints;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@EnableConfigurationProperties(AdminUserProperties.class)
@ImportRuntimeHints(SecurityHints.class)
public class AnbarinooApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AnbarinooApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AnbarinooApplication.class);
    }

}