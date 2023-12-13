package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.model.Authority;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "user.admin")
public record AdminUserProperties(Long id, String username, String password,
                                  List<Authority> authorities) {
}
