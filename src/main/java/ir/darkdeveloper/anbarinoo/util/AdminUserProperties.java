package ir.darkdeveloper.anbarinoo.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ir.darkdeveloper.anbarinoo.model.Authority;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "user.admin")
@Data
public class AdminUserProperties {

    private Long id;
    private String username;
    private String password;
    private List<Authority> authorities = new ArrayList<>();

}
