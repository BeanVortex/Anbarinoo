package ir.darkdeveloper.anbarinoo.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;

@Service
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserUtils userUtils;
    private final RefreshService refreshService;

    @Autowired
    public JwtFilter(@Lazy JwtUtils jwtUtils, @Lazy UserUtils userUtils, RefreshService refreshService) {
        this.jwtUtils = jwtUtils;
        this.userUtils = userUtils;
        this.refreshService = refreshService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String refreshToken = request.getHeader("refresh_token");
        String accessToken = request.getHeader("access_token");

        if (refreshToken != null && accessToken != null && !jwtUtils.isTokenExpired(refreshToken)) {

            String username = jwtUtils.getUsername(refreshToken);
            Long userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();

            authenticateUser(username);

            setUpHeader(response, accessToken, username, userId);
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

    private void setUpHeader(HttpServletResponse response, String accessToken, String username,
                             Long userId) {

        String newAccessToken;

        // if this if didn't execute it means the access token is still valid
        if (jwtUtils.isTokenExpired(accessToken)) {
            //db query
            String storedAccessToken = refreshService.getRefreshByUserId(userId).getAccessToken();
            if (accessToken.equals(storedAccessToken)) {
                newAccessToken = jwtUtils.generateAccessToken(username);
                RefreshModel refreshModel = new RefreshModel();
                refreshModel.setAccessToken(newAccessToken);
                refreshModel.setUserId(userId);
                //db query
                refreshModel.setId(refreshService.getIdByUserId(userId));
                // db query
                refreshService.saveToken(refreshModel);
                var accessExpiration = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(newAccessToken));

                response.addHeader("access_expiration", accessExpiration);
                response.addHeader("access_token", newAccessToken);
            } else
                //if stored token is not equal with user send token, it will return 403
                SecurityContextHolder.getContext().setAuthentication(null);

        }
    }

}
