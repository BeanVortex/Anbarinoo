package ir.darkdeveloper.anbarinoo.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import ir.darkdeveloper.anbarinoo.security.jwt.JwtFilter;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2FailureHandler;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2RequestRepo;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2SuccessHandler;
import ir.darkdeveloper.anbarinoo.security.oauth2.OAuth2UserService;
import ir.darkdeveloper.anbarinoo.service.UserService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AppSecurityConfig extends WebSecurityConfigurerAdapter{
    

    private final UserService userService;
    private final JwtFilter jwtFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2RequestRepo  oAuth2RequestRepo;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    @Value("${user.email-verification-disabled}")
    private Boolean userEnabled;

    @Autowired
    public AppSecurityConfig(@Lazy UserService userService, JwtFilter jwtFilter,
                         OAuth2UserService oAuth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler,
                         OAuth2RequestRepo  oAuth2RequestRepo, OAuth2FailureHandler oAuth2FailureHandler) {
        this.userService = userService;
        this.jwtFilter = jwtFilter;
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2RequestRepo = oAuth2RequestRepo;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
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
                                "/oauth2/**",
                                "/api/user/verify/**") 
                        .permitAll()
                    .anyRequest()
                        .authenticated()
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
    @Bean("userEnabled")
    public Boolean userEnabled(){
        return userEnabled;
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



