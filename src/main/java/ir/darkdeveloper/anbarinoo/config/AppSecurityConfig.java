package ir.darkdeveloper.anbarinoo.config;

import ir.darkdeveloper.anbarinoo.security.exception.RestAuthenticationEntryPoint;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtFilter;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2FailureHandler;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2RequestRepo;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2SuccessHandler;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2UserService;
import ir.darkdeveloper.anbarinoo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
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
@EnableWebSecurity
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AppSecurityConfig {

    @Lazy
    private final JwtFilter jwtFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2RequestRepo oAuth2RequestRepo;
    private final OAuth2FailureHandler oAuth2FailureHandler;


    // It is used throughout the framework as a user DAO and is the strategy used by the DaoAuthenticationProvider.
//    private final UserService userService;
//    @Bean
//    public AuthenticationProvider authenticationProvider(){
//        var daoProvider = new DaoAuthenticationProvider();
//        daoProvider.setUserDetailsService(userService);
//        daoProvider.setPasswordEncoder(passEncode());
//        return daoProvider;
//    }

    @Bean
    public PasswordEncoder passEncode() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration c) throws Exception{
        return c.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors();
        http.csrf()
                .disable()
                .authorizeHttpRequests(authorize -> authorize.requestMatchers(
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
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .formLogin()
                .disable()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/login/oauth2")
                .authorizationRequestRepository(oAuth2RequestRepo)
                .and()
                .redirectionEndpoint()
                .baseUri("/login/callback")
                .and()
                .userInfoEndpoint()
                .userService(oAuth2UserService)
                .and()
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
//                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
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


