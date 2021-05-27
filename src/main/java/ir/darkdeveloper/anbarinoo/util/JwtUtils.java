package ir.darkdeveloper.anbarinoo.util;

import java.util.Date;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    // @PostConstruct
    // public void initSecret(){
    //     secret = encoder.encode(secret);
    //     System.out.println("SECRET : " + secret);
    // }

    public String generateRefreshToken(String username, Long userId) {
        Date date  = new Date(System.currentTimeMillis() + 60 * 60 * 24 * 7 * 3 * 1000);
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secret)
                .setSubject(username)
                .claim("user_id", userId)
                .setExpiration(date)
                .compact();
    }

    public String generateAccessToken(String username) {
        Date date = new Date(System.currentTimeMillis() + 60 * 2 * 1000);
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secret)
                .setSubject(username)
                .setExpiration(date)
                .compact();
    }

    public String getUsername(String token) {
        Jwts.claims().get("exp");
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDate(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Date getExpirationDate(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}
}
