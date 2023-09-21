package ir.darkdeveloper.anbarinoo.config;

import ir.darkdeveloper.anbarinoo.security.exception.RestAuthenticationEntryPoint;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtFilter;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2FailureHandler;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2RequestRepo;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2SuccessHandler;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class AppSecurityConfig {

    private final JwtFilter jwtFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2RequestRepo oAuth2RequestRepo;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    public AppSecurityConfig(@Lazy JwtFilter jwtFilter, OAuth2UserService oAuth2UserService,
                             OAuth2SuccessHandler oAuth2SuccessHandler, OAuth2RequestRepo oAuth2RequestRepo,
                             OAuth2FailureHandler oAuth2FailureHandler) {
        this.jwtFilter = jwtFilter;
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2RequestRepo = oAuth2RequestRepo;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(c -> {
        });
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorize -> authorize.requestMatchers(
                                        "/info", "/css/**",
                                        "/**",
                                        "/fonts/**",
                                        "/js/**", "/img/**",
                                        "/api/user/signup/",
                                        "/api/user/login/",
                                        "/user/profile_images/noProfile.jpeg",
                                        "/api/post/all/",
                                        "/oauth2/**",
                                        "/api/export/excel/**",
                                        "/api/user/verify/**",
                                        "/webjars/**",
                                        "/forbidden"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(new RestAuthenticationEntryPoint()));
        http.formLogin(AbstractHttpConfigurer::disable);
        http.oauth2Login(o -> {
            o.authorizationEndpoint(a ->
                    a.baseUri("/login/oauth2")
                            .authorizationRequestRepository(oAuth2RequestRepo));
            o.redirectionEndpoint(r -> r.baseUri("/login/callback"));
            o.userInfoEndpoint(u -> u.userService(oAuth2UserService));
            o.successHandler(oAuth2SuccessHandler);
            o.failureHandler(oAuth2FailureHandler);
        });
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passEncode() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.addExposedHeader("refresh_token, access_token, access_expiration, refresh_expiration");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}


