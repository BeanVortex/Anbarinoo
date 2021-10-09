package ir.darkdeveloper.anbarinoo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServerConfig {

    @Value("${server.domain_name}")
    private String domainName;

    @Bean("domainName")
    public String getDomainName() {
        return domainName;
    }
}
