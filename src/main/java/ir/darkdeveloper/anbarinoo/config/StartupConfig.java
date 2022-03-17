package ir.darkdeveloper.anbarinoo.config;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import ir.darkdeveloper.anbarinoo.model.Authority;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminUserProperties.class)
public class StartupConfig {

    private final UserRolesService rolesService;
    public static final String DATE_FORMAT = "EE MMM dd yyyy HH:mm:ss";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Bean
    public String dateFormat() {
        return DATE_FORMAT;
    }

    @Bean
    public DateTimeFormatter dateFormatter() {
        return DATE_FORMATTER;
    }

    @Value("${user.email-verification-disabled}")
    private Boolean userEnabled;

    @Bean
    public Boolean userEnabled() {
        return userEnabled;
    }

    private void createDefaultRole() {
        if (!rolesService.exists("USER")) {
            List<Authority> authorities = new ArrayList<>(List.of(Authority.OP_EDIT_USER, Authority.OP_ACCESS_USER, Authority.OP_DELETE_USER));
            rolesService.saveRole(new UserRole(1L, "USER", authorities));
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        createDefaultRole();
    }
}
