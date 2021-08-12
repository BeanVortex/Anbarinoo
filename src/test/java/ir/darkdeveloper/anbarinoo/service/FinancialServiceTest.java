package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.FinancialModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record FinancialServiceTest(FinancialService financialService,
                                   UserService userService,
                                   JwtUtils jwtUtils) {

    private static HttpServletRequest request;
    private static Long financialId;

    @Autowired
    public FinancialServiceTest {
    }


    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        var user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(true);
        userService.signUpUser(user, response);
        var userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = {"OP_EDIT_USER"})
    void saveFinancial() {
        var financial = new FinancialModel();
        financialService.saveFinancial(financial, request);
        financialId = financial.getId();
        assertThat(financial.getId()).isNotNull();
        assertThat(financial.getUser()).isNotNull();
        assertThat(financial.getCosts()).isEqualTo("0");
        assertThat(financial.getEarnings()).isEqualTo("0");
        assertThat(financial.getProfit()).isEqualTo("0");
        assertThat(financial.getTax()).isEqualTo(9);
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = {"OP_EDIT_USER"})
    void updateFinancial() {
        var financial = new FinancialModel();
        financial.setId(financialId);
        financial.setCosts(new BigDecimal("5"));
        financial.setProfit(new BigDecimal("5"));
        financialService.saveFinancial(financial, request);
        assertThat(financial.getUser()).isNotNull();
        assertThat(financial.getId()).isNotNull();
        assertThat(financial.getUser()).isNotNull();
        assertThat(financial.getCosts()).isEqualTo("5");
        assertThat(financial.getEarnings()).isEqualTo("0");
        assertThat(financial.getProfit()).isEqualTo("5");
        assertThat(financial.getTax()).isEqualTo(9);
    }


    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(String email, Long userId) {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        String refreshToken = jwtUtils.generateRefreshToken(email, userId);
        String accessToken = jwtUtils.generateAccessToken(email);
        var refreshDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        headers.put("refresh_token", refreshToken);
        headers.put("access_token", accessToken);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);


        HttpServletRequest request = mock(HttpServletRequest.class);
        for (String key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

}