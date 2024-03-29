package ir.darkdeveloper.anbarinoo.config;

import ir.darkdeveloper.anbarinoo.model.Authority;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
            var authorities = List.of(
                    Authority.OP_EDIT_USER,
                    Authority.OP_ACCESS_USER,
                    Authority.OP_DELETE_USER,
                    Authority.OP_ADD_PRODUCT,
                    Authority.OP_ACCESS_PRODUCT,
                    Authority.OP_EDIT_PRODUCT,
                    Authority.OP_DELETE_PRODUCT
            );
            rolesService.saveRole(new UserRole(1L, "USER", authorities));
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        createDefaultRole();
    }
}
