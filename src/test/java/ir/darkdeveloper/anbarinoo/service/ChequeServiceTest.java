package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
class ChequeServiceTest {

    private UserModel user;
    private ChequeModel cheque;
    private HttpServletRequest request;

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
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    void chequeSetup() {
        user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(false);

        cheque = new ChequeModel();
        cheque.setAmount(new BigDecimal("554.55"));
        cheque.setIsDebt(true);
        cheque.setNameOf("DD");
        cheque.setPayTo("GG");
        cheque.setIssuedAt(LocalDateTime.now());
        cheque.setValidTill(LocalDateTime.now().plusDays(5));

        request = mock(HttpServletRequest.class);


    }

    @Test
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        userService.signUpUser(user, response);
        UserModel fetchedModel = (UserModel) userService.loadUserByUsername("email");
        jwtUtils.generateRefreshToken(user.getEmail(), user.getId());

        assertThat(response.containsHeader("refresh_token")).isTrue();

        assertThat(fetchedModel.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedModel.getEnabled()).isEqualTo(true);
        assertThat(fetchedModel.getUserName()).isEqualTo("email");
    }

    @Test
    @WithMockUser(username = "email@mail.com")
    void saveCheque() {
        cheque.setUser(user);
        chequeService.saveCheque(cheque);
//        chequeService.getCheque()
    }


    void setUpHeader(){

    }


}