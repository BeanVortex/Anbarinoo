package ir.darkdeveloper.anbarinoo.security;

import ir.darkdeveloper.anbarinoo.security.jwt.JwtFilter;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.assertj.core.data.Percentage;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Part;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public record JwtTest(WebApplicationContext webApplicationContext, JwtUtils jwtUtils, JwtFilter filter) {

    // a simple test class based on user controller test


    private static Long userId;
    private static MockMvc mockMvc;
    private static String refreshToken;
    private static String accessToken;
    private static LocalDateTime dateRefresh;
    private static LocalDateTime dateAccess;
    private static final Long accessTime = (long) (2 * 1000);
    private static final Long refreshTime = (long) (5 * 1000);


    @Autowired
    public JwtTest {
    }

    @BeforeAll
    static void setUp() {
        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    void setUp2() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(filter).build();
        jwtUtils.setAccessExpire(accessTime);
        jwtUtils.setRefreshExpire(refreshTime);
        dateAccess = LocalDateTime.now().plusSeconds(accessTime / 1000);
        dateRefresh = LocalDateTime.now().plusSeconds(refreshTime / 1000);
    }


    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void signUpUser() throws Exception {
        var address = new MockPart("address", "address".getBytes());
        var des = new MockPart("description", "desc".getBytes());
        var username = new MockPart("userName", "user n".getBytes());
        var password = new MockPart("password", "Pass!12".getBytes());
        var passwordRepeat = new MockPart("passwordRepeat", "Pass!12".getBytes());
        var email = new MockPart("email", "email@mail.com".getBytes());
        var parts = new Part[]{email, des, username, address, password, passwordRepeat};

        mockMvc.perform(multipart("/api/user/signup/")
                        .part(parts)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andDo(result -> {
                    refreshToken = result.getResponse().getHeader("refresh_token");
                    accessToken = result.getResponse().getHeader("access_token");
                    var obj = new JSONObject(result.getResponse().getContentAsString());
                    userId = obj.getLong("id");
                })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").isNotEmpty());

        // based on system performance
        assertThat(jwtUtils.getExpirationDate(refreshToken).getSecond())
                .isCloseTo(dateRefresh.getSecond(), Percentage.withPercentage(20));
        assertThat(jwtUtils.getExpirationDate(accessToken).getSecond())
                .isCloseTo(dateAccess.getSecond(), Percentage.withPercentage(20));
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void waitAndGetUserInfo() throws Exception {
        Thread.sleep(refreshTime - 1000);
        assertThat(jwtUtils.isTokenExpired(accessToken)).isTrue();
        mockMvc.perform(get("/api/user/{id}/", userId)
                        .header("refresh_token", refreshToken)
                        .header("access_token", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("access_token"))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.shopImage").value(is("noImage.png")))
                .andExpect(jsonPath("$.profileImage").value(is("noProfile.jpeg")))
                .andExpect(jsonPath("$.id").value(is(userId), Long.class));
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void waitAndGetUserInfoFailing() throws Exception {
        Thread.sleep(refreshTime + 1000);
        assertThat(jwtUtils.isTokenExpired(accessToken)).isTrue();
        mockMvc.perform(get("/api/user/{id}/", userId)
                        .header("refresh_token", refreshToken)
                        .header("access_token", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(is("You are logged out. Try logging in again")));
    }


}
