package ir.darkdeveloper.anbarinoo.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import ir.darkdeveloper.anbarinoo.security.JwtFilter;
import ir.darkdeveloper.anbarinoo.service.UserService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AppSecurityConfig extends WebSecurityConfigurerAdapter{
    

    private final UserService userService;
    private final JwtFilter jwtFilter;

    @Autowired
    public AppSecurityConfig(@Lazy UserService userService, JwtFilter jwtFilter) {
        this.userService = userService;
        this.jwtFilter = jwtFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors();
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/", "/info", "/css/**",
                 "/fonts/**", 
                 "/js/**", "/img/**", 
                 "/api/user/signup/",
                  "/api/user/login/",
                   "/api/post/all/",
                   "/oauth2/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .disable()
                .oauth2Login()
                    .authorizationEndpoint()
                        .baseUri("/oauth2/authorize")
                        .and()
                    .redirectionEndpoint()
                        .baseUri("/oauth2/callback")
                    .and()
                    .userInfoEndpoint()
                        .userService(null)
                .and()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passEncode());
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }



    @Bean
    public PasswordEncoder passEncode() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.addExposedHeader("refresh_token, access_token, expiration, user_id");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}



