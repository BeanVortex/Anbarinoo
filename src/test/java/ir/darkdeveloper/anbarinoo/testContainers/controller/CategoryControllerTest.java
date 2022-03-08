package ir.darkdeveloper.anbarinoo.testContainers.controller;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.testContainers.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@ExtendWith({DatabaseSetup.class})
public class CategoryControllerTest {



    private final UserService userService;
    private final JwtUtils jwtUtils;


    private static HttpServletRequest request;
    private static Long userId;
    private static MockMvc mockMvc;

    @Autowired
    public CategoryControllerTest(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }


    @BeforeAll
    public static void setUp() {
        var authentication = Mockito.mock(Authentication.class);
        var securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }


    @Test
    @Order(1)
    void saveUser() {
        var response = mock(HttpServletResponse.class);
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .userName("user n")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .build();
        userService.signUpUser(user, response);
        userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
    }

    @Test
    @Order(2)
    void getUser() {
        var fetched = userService.getUserInfo(userId, request);
        assertThat(fetched.getId()).isEqualTo(userId);
    }


    private HttpServletRequest setUpHeader(String email, Long userId) {

        var headers = new HashMap<String, String>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        var refreshToken = jwtUtils.generateRefreshToken(email, userId);
        var accessToken = jwtUtils.generateAccessToken(email);
        var refreshDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        headers.put("refresh_token", refreshToken);
        headers.put("access_token", accessToken);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);


        var request = mock(HttpServletRequest.class);
        for (var key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }


}
