package ir.darkdeveloper.anbarinoo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class AnbarinooApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(AnbarinooApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(AnbarinooApplication.class);
	}
}
