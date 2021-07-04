package ir.darkdeveloper.anbarinoo.security.oauth2;

import static ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2RequestRepo.REDIRECT_URI_PARAM_COOKIE_NAME;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.CookieUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final OAuth2RequestRepo oAuth2RequestRepo;
    private final UserService userService;
    private final OAuth2Properties oAuth2Properties;

    @Autowired
    public OAuth2SuccessHandler(@Lazy JwtUtils jwtUtils, OAuth2RequestRepo oAuth2RequestRepo,
            @Lazy UserService userService, OAuth2Properties oAuth2Properties) {
        this.jwtUtils = jwtUtils;
        this.oAuth2RequestRepo = oAuth2RequestRepo;
        this.userService = userService;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {

        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        // rejects redirects from unknown hosts. known hosts are defined in application.yml with prefix of 'oauth2'
        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get()))
            throw new BadRequestException(
                    "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        headerSetup(response, authentication);

        return targetUrl;
    }

    private void headerSetup(HttpServletResponse response, Authentication authentication) {
        UserModel user = (UserModel) userService.loadUserByUsername(authentication.getName());

        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());

        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_expiration", jwtUtils.getExpirationDate(refreshToken).toString());
        response.addHeader("access_expiration", jwtUtils.getExpirationDate(accessToken).toString());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        oAuth2RequestRepo.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return oAuth2Properties.getAuthorizedRedirectUris().stream().anyMatch(authorizedRedirectUri -> {
            // Only validate host and port. Let the clients use different paths if they want to
            // So I dont check the path of uri 
            URI authorizedURI = URI.create(authorizedRedirectUri);
            if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                    && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                return true;
            }
            return false;
        });
    }

}
