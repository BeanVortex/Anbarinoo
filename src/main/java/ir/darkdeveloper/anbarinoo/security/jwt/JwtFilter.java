package ir.darkdeveloper.anbarinoo.security.jwt;

import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.NotFoundException;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserAuthUtils userAuthUtils;
    private final RefreshService refreshService;
    private final AdminUserProperties adminUser;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var refreshToken = Optional.ofNullable(request.getHeader("refresh_token"));
        var accessToken = Optional.ofNullable(request.getHeader("access_token"));
        if (refreshToken.isPresent() && accessToken.isPresent()
                && !JwtUtils.isTokenExpired(refreshToken.get())) {

            var username = JwtUtils.getUsername(refreshToken.get());
            var userId = JwtUtils.getAllClaimsFromToken(refreshToken.get())
                    .get("user_id", Double.class);
            authenticateUser(username, userId.longValue());
            setUpHeader(response, refreshToken.get(), accessToken.get(), username, userId.longValue());
        }
        chain.doFilter(request, response);
    }

    private void authenticateUser(String username, Long userId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (username != null && auth == null) {
            //db query
            var userDetails = userAuthUtils.loadUserByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User does not exist"));
            if (!userDetails.getUsername().equals(adminUser.username())) {
                var userModel = (UserModel) userDetails;
                if (!userModel.getId().equals(userId))
                    throw new ForbiddenException("Do not change token. I'm watching you");
            }

            var upToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(upToken);
        }
    }

    private void setUpHeader(HttpServletResponse response, String refreshToken,
                             String accessToken, String username, Long userId) {
        // if this didn't execute, it means the access token is still valid
        if (JwtUtils.isTokenExpired(accessToken)) {
            //db query
            var storedRefreshModel = refreshService.getRefreshByUserId(userId);
            var storedAccessToken = storedRefreshModel.getAccessToken();
            var storedRefreshToken = storedRefreshModel.getRefreshToken();
            if (accessToken.equals(storedAccessToken) && storedRefreshToken.equals(refreshToken)) {
                var newAccessToken = JwtUtils.generateAccessToken(username);
                var refreshModel = new RefreshModel();
                refreshModel.setAccessToken(newAccessToken);
                refreshModel.setRefreshToken(storedRefreshToken);
                refreshModel.setUserId(userId);
                refreshModel.setId(storedRefreshModel.getId());
                // db query
                refreshService.saveToken(refreshModel);
                response.addHeader("access_token", newAccessToken);
            } else
                //if stored token is not equal with user send token, it will return 401
                SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

}
