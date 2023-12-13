package ir.darkdeveloper.anbarinoo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtils {

    public static Long refreshExpire;
    public static Long accessExpire;
    public static SecretKey key;
    public static JwtParser parser;

    public JwtUtils() {
        refreshExpire = (long) (60 * 60 * 24 * 7 * 3 * 1000);
        accessExpire = (long) (60 * 1000);
    }

    @PostConstruct
    public void initSecret() {
        // note that previous token after restarting application won't work
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    // generates a unique jwt token
    public static String generateRefreshToken(String username, Long userId) {
        // expires in 21 days
        var date = new Date(System.currentTimeMillis() + refreshExpire);
        return Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setSubject(username)
                .claim("user_id", userId).setExpiration(date).compact();
    }

    // Generates access token
    public static String generateAccessToken(String username) {
        var date = new Date(System.currentTimeMillis() + accessExpire);
        return Jwts.builder()
                .setSubject(username)
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(date).compact();
    }

    public static String getUsername(String token) {
        return parser.parseClaimsJws(token).getBody().getSubject();
    }

    public static Long getUserId(String refreshToken) {
        return getAllClaimsFromToken(refreshToken).get("user_id", Double.class).longValue();
    }

    public static Claims getAllClaimsFromToken(String token) throws JwtException {
        return parser.parseClaimsJws(token).getBody();
    }

    // Todo: handle token exceptions
    public static Boolean isTokenExpired(String token) {
        try {
            parser.parseClaimsJws(token);
            return false;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature : {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return true;
    }

    public static LocalDateTime getExpirationDate(String token) throws JwtException {
        return getClaimFromToken(token, Claims::getExpiration)
                .toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    // for testing purposes
    public void changeExpirationDate(String token, long till) {
        Jwts.claims(getAllClaimsFromToken(token)).setExpiration(new Date(till));
    }

    public static  <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        var claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
}
