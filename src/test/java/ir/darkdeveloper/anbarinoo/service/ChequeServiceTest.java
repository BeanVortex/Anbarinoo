package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.*;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChequeServiceTest {

    private static UserModel user;
    private static HttpServletRequest request;
    private static ChequeModel cheque;

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
        Authentication authentication = mock(Authentication.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
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
        System.out.println("BeforeAll");
    }


    @Test
    @WithMockUser(username = "anonymousUser")
    @Order(1)
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        userService.signUpUser(user, response);
        UserModel fetchedModel = (UserModel) userService.loadUserByUsername(user.getEmail());
        assertThat(fetchedModel.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedModel.getEnabled()).isEqualTo(true);
        assertThat(fetchedModel.getUserName()).isEqualTo("email");
        System.out.println("saveUser");
    }

    @Test
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Order(2)
    void saveCheque() {
        System.out.println("saveCheque");
        UserModel userModel = new UserModel();
        userModel.setId(user.getId());
        cheque.setUser(userModel);
        request = setUpHeader();
        chequeService.saveCheque(cheque, request);
    }

    @Test
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Order(3)
    void getCheque() {
        System.out.println("getCheque");
        request = setUpHeader();
        ChequeModel fetchedCheque = chequeService.getCheque(cheque.getId(), request);
        assertThat(fetchedCheque).isNotNull();
    }

    @Test
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Order(4)
    void getChequesByUserId() {
        System.out.println("getChequesByUserId");
        List<ChequeModel> cheques = chequeService.getChequesByUserId(user.getId(), request);
        assertThat(cheques.size()).isNotEqualTo(0);
        for (ChequeModel cheque : cheques)
            assertThat(cheque.getUser().getId()).isEqualTo(user.getId());

    }

    @Test
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Order(5)
    void updateCheque() {
        System.out.println("updateCheque");
        request = setUpHeader();
        cheque.setIsCheckedOut(true);
        request = setUpHeader();
        chequeService.updateCheque(cheque, request);
    }

    @Test
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Order(6)
    void deleteCheque() {
        System.out.println("deleteCheque");
        request = setUpHeader();
        chequeService.deleteCheque(cheque.getId(), request);
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

        return request;
    }


}