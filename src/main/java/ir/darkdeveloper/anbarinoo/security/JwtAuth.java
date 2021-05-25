package ir.darkdeveloper.anbarinoo.security;

import lombok.Data;

@Data
public class JwtAuth {
    
    private String username;
    private String password;
}
