package ir.darkdeveloper.anbarinoo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import ir.darkdeveloper.anbarinoo.model.Auth.Authority;
import ir.darkdeveloper.anbarinoo.model.UserRoles;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;

@Configuration
public class UserConfig {

    private final UserRolesService service;

    @Value("${user.email-verification-disabled}")
    private Boolean userEnabled;

    @Autowired
    public UserConfig(UserRolesService service) {
        this.service = service;
    }

    @Bean
    public Boolean userEnabled(){
        return userEnabled;
    }

    private void createDefaultRole() {
        if (!service.exists("USER")) {
            List<Authority> authorities = new ArrayList<>(List.of(Authority.OP_EDIT_USER, Authority.OP_ACCESS_USER, Authority.OP_DELETE_USER));
            service.saveRole(new UserRoles(1L, "USER", authorities));
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        createDefaultRole();
    }
}
