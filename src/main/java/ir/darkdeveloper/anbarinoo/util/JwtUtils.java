package ir.darkdeveloper.anbarinoo.util;

import java.util.Date;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class JwtUtils {

    @Value("${jwt.secretkey}")
    private String secret;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final PasswordEncoder encoder;

    @Autowired
    public JwtUtils(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @PostConstruct
    public void initSecret(){
        secret = encoder.encode(secret);
        System.out.println("SECRET : " + secret);
    }

    public String generateRefreshToken(String username, Long userId) {
        Date date = new Date(System.currentTimeMillis() + 60 * 60 * 24 * 7 * 3 * 1000);
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secret)
                .setIssuedAt(new Date())
                .setSubject(username)
                .claim("user_id", userId).setExpiration(date).compact();
    }

    public String generateAccessToken(String username) {
        Date date = new Date(System.currentTimeMillis() + 60 * 1 * 1000);
        return Jwts.builder().signWith(SignatureAlgorithm.HS256, secret).setSubject(username).setIssuedAt(new Date())
                .setExpiration(date).compact();
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public Claims getAllClaimsFromToken(String token) throws JwtException {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {

        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return false;
          } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
          } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
          } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
          } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
          } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
          }
          return true;      
    }

    public Date getExpirationDate(String token) throws JwtException {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
}
