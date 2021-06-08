package ir.darkdeveloper.anbarinoo.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuth {
    
    private String username;
    private String password;
}
