package ir.darkdeveloper.anbarinoo;


import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@Log4j2
public class AnbarinooApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AnbarinooApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AnbarinooApplication.class);
    }
}