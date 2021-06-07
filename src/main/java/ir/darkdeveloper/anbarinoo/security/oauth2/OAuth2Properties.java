package ir.darkdeveloper.anbarinoo.security.oauth2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    
    private List<String> authorizedRedirectUris = new ArrayList<>();

    public List<String> getAuthorizedRedirectUris() {
        return authorizedRedirectUris;
    }

    public OAuth2Properties authorizedRedirectUris(List<String> authorizedRedirectUris) {
        this.authorizedRedirectUris = authorizedRedirectUris;
        return this;
    }
}
