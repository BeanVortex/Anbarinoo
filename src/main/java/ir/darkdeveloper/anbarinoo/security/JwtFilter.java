package ir.darkdeveloper.anbarinoo.security;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;

@Service
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserUtils userUtils;

    @Autowired
    public JwtFilter(@Lazy JwtUtils jwtUtils, @Lazy UserUtils userUtils) {
        this.jwtUtils = jwtUtils;
        this.userUtils = userUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String refreshToken = request.getHeader("refresh_token");
        String accessToken = request.getHeader("access_token");

        if (refreshToken != null && accessToken != null && !jwtUtils.isTokenExpired(refreshToken)) {

            String username = jwtUtils.getUsername(refreshToken);
            Long userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();

            authenticateUser(username);

            String newAccessToken = accessToken;
            
            setUpRefreshToken(newAccessToken, accessToken, username, userId);

            setUpHeader(response, refreshToken, newAccessToken);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (username != null && auth == null) {
            //db query
            UserDetails userDetails = userUtils.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(upToken);

        }
    }

    private void setUpRefreshToken(String newAccessToken, String accessToken, String username, Long userId) {
        if (jwtUtils.isTokenExpired(accessToken)) {
            newAccessToken = jwtUtils.generateAccessToken(username);
            //db query
            // String storedRefreshToken = refreshService.getRefreshByUserId(userId).getRefreshToken();

            // RefreshModel refreshModel = new RefreshModel();
            // refreshModel.setAccessToken(newAccessToken);
            // refreshModel.setRefreshToken(storedRefreshToken);
            // refreshModel.setUserId(userId);
            // //db query
            // refreshModel.setId(refreshService.getIdByUserId(userId));
            // // db query
            // refreshService.saveToken(refreshModel);
        }
    }

    private void setUpHeader(HttpServletResponse response, String refreshToken, String newAccessToken) {
        // Format that js Date object understand
        var dateFormat = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");
        var refreshDate = dateFormat.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = dateFormat.format(jwtUtils.getExpirationDate(newAccessToken));
        response.addHeader("refresh_expiration", refreshDate);
        response.addHeader("access_expiration", accessDate);
        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", newAccessToken);
    }

}
