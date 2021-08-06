package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@SpringBootTest
class ChequeServiceTest {

    private static UserModel user;
    private static HttpServletRequest request;
    private ChequeModel cheque;

    private final ChequeService chequeService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    ChequeServiceTest(ChequeService chequeService, UserService userService, JwtUtils jwtUtils) {
        this.chequeService = chequeService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(false);
        request = mock(HttpServletRequest.class);
    }

    @BeforeEach
    void chequeSetup() {
        cheque = new ChequeModel();
        cheque.setAmount(new BigDecimal("554.55"));
        cheque.setIsDebt(true);
        cheque.setNameOf("DD");
        cheque.setPayTo("GG");
        cheque.setIssuedAt(LocalDateTime.now());
        cheque.setValidTill(LocalDateTime.now().plusDays(5));


    }

    @Test
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        userService.signUpUser(user, response);
        UserModel fetchedModel = (UserModel) userService.loadUserByUsername("email");

        request = setUpHeader();

        assertThat(fetchedModel.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedModel.getEnabled()).isEqualTo(true);
        assertThat(fetchedModel.getUserName()).isEqualTo("email");
    }

    @Test
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCheque() {
        UserModel userModel = new UserModel();
        userModel.setId(user.getId());
        cheque.setUser(userModel);
        request = setUpHeader();
        chequeService.saveCheque(cheque, request);
        ChequeModel fetchedCheque = chequeService.getCheque(cheque.getId(), request);
        assertThat(fetchedCheque).isNotNull();
    }


    //should return the object; data is being removed
    HttpServletRequest setUpHeader() {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        var refreshDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        headers.put("refresh_token", refreshToken);
        headers.put("access_token", accessToken);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);


        HttpServletRequest request = mock(HttpServletRequest.class);
        for (String key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        for (String key : headers.keySet())
            System.out.println(key + ": " + request.getHeader(key));

        return request;
    }


}