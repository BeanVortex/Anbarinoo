package ir.darkdeveloper.anbarinoo.security.oauth2;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.CookieUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2RequestRepo.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final OAuth2Properties oAuth2Properties;
    private final OAuth2RequestRepo oAuth2RequestRepo;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        var targetUrl = determineTargetUrl(request, response, authentication);

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

        // Reject redirects from unknown hosts. known hosts are defined in application.yml with prefix of 'oauth2'
        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get()))
            throw new BadRequestException(
                    "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");

        var targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        headerSetup(response, authentication);

        return targetUrl;
    }

    private void headerSetup(HttpServletResponse response, Authentication authentication) {
        var user = (UserModel) userService.loadUserByUsername(authentication.getName());
        var refreshToken = JwtUtils.generateRefreshToken(user.getEmail(), user.getId());
        var accessToken = JwtUtils.generateAccessToken(user.getEmail());

        UserAuthUtils.setupHeader(response, accessToken, refreshToken);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        oAuth2RequestRepo.removeAuthorizationRequest(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        var clientRedirectUri = URI.create(uri);

        return oAuth2Properties.getAuthorizedRedirectUris().stream().anyMatch(authorizedRedirectUri -> {
            // Only validate host and port. Let the clients use different paths if they want to
            // So I don't check the path of uri
            var authorizedURI = URI.create(authorizedRedirectUri);
            return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                    && authorizedURI.getPort() == clientRedirectUri.getPort();
        });
    }

}
