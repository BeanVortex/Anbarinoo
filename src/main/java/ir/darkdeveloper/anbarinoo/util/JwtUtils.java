package ir.darkdeveloper.anbarinoo.util;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtUtils {

    @Value("${jwt.secretkey}")
    private String secret;

    // private final PasswordEncoder encoder;

    // @Autowired
    // public JwtUtils(PasswordEncoder encoder) {
    //     this.encoder = encoder;
    // }

    @PostConstruct
    public void initSecret(){
       // secret = encoder.encode(secret);
    }

    //refresh token is used to generate access token
    public String generateRefreshToken(String username) {
        Date date  = new Date(System.currentTimeMillis() + 60 * 60 * 24 * 7 * 3 * 1000);
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secret)
                .setSubject(username)
                .setExpiration(date)
                .compact();
    }

    public String generateAccessToken(String username) {
        Date date = new Date(System.currentTimeMillis() + 60 * 1 * 1000);
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secret)
                .setSubject(username)
                .setExpiration(date)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDate(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDate(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration();
    }

}
