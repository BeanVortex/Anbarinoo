package ir.darkdeveloper.anbarinoo.security.jwt;

import lombok.Data;

@Data
public class JwtAuth {
    
    private String username;
    private String password;
}
