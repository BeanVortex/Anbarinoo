package ir.darkdeveloper.anbarinoo.security;

import java.io.IOException;

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

import ir.darkdeveloper.anbarinoo.security.Crud.RefreshModel;
import ir.darkdeveloper.anbarinoo.security.Crud.RefreshService;
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
        this.refreshService = refreshService;
        this.userUtils = userUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //String token = request.getHeader("Authorization");

        String refreshToken = request.getHeader("refresh_token");
        String accessToken = request.getHeader("access_token");

        if (refreshToken != null && accessToken != null) {
            String username = jwtUtils.getUsername(refreshToken);

            Long userId = username.equals(userUtils.getAdminUsername()) ? userUtils.getAdminId()
                    : userUtils.getUserIdByUsernameOrEmail(username);

            String storedAccessToken = refreshService.getRefreshByUserId(userId).getAccessToken();
            String storedRefreshToken = refreshService.getRefreshByUserId(userId).getRefreshToken();
            if (storedAccessToken != null && storedRefreshToken != null && accessToken.equals(storedAccessToken)
                    && refreshToken.equals(storedRefreshToken) && !jwtUtils.isTokenExpired(storedRefreshToken)) {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (username != null && auth == null) {
                    UserDetails userDetails = userUtils.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(upToken);
                    String newAccessToken = jwtUtils.generateAccessToken(username);
                    RefreshModel refreshModel = new RefreshModel();
                    refreshModel.setAccessToken(newAccessToken);
                    refreshModel.setRefreshToken(storedRefreshToken);
                    refreshModel.setUserId(userId);
                    if (username.equals(userUtils.getAdminUsername())) {
                        refreshModel.setId(refreshService.getIdByUserId(userUtils.getAdminId()));
                        response.addHeader("user_id", "" + refreshModel.getId());
                    } else {
                        refreshModel
                                .setId(refreshService.getIdByUserId(userUtils.getUserIdByUsernameOrEmail(username)));
                    }
                    refreshService.saveToken(refreshModel);
                    response.addHeader("access_token", newAccessToken);
                    response.addHeader("refresh_token", refreshToken);
                }

            }
        }

        filterChain.doFilter(request, response);
    }

    /*     public boolean userAdminEndpointCheck(HttpServletRequest request, UserDetails model){
        String endPoint = request.getRequestURI();
        switch(endPoint){
            case "/api/user/role/":
            return model.getAuthorities().contains(Authority.OP_ACCESS_ROLE);
            case "/api/user/update/":
            return model.getAuthorities().contains(Authority.OP_ACCESS_ROLE);
        }
    } */

}
