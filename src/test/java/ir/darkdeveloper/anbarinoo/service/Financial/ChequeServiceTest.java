package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

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
@DirtiesContext
public record ChequeServiceTest(ChequeService chequeService,
                                UserService userService,
                                JwtUtils jwtUtils) {

    private static HttpServletRequest request;
    private static Long userId;
    private static Long chequeId;

    @Autowired
    public ChequeServiceTest {
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }


    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        var user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(false);
        var response = mock(HttpServletResponse.class);
        userService.signUpUser(user, response);
        var fetchedModel = (UserModel) userService.loadUserByUsername(user.getEmail());
        assertThat(fetchedModel.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedModel.getEnabled()).isEqualTo(true);
        assertThat(fetchedModel.getUserName()).isEqualTo("email");
        userId = fetchedModel.getId();
        request = setUpHeader(user.getEmail(), userId);
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCheque() {
        var cheque = new ChequeModel();
        cheque.setAmount(new BigDecimal("554.55"));
        cheque.setIsDebt(true);
        cheque.setNameOf("DD");
        cheque.setPayTo("GG");
        cheque.setIssuedAt(LocalDateTime.now());
        cheque.setValidTill(LocalDateTime.now().plusDays(5));
        cheque.setUser(new UserModel(userId));
        chequeService.saveCheque(cheque, request);
        chequeId = cheque.getId();
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getCheque() {
        var fetchedCheque = chequeService.getCheque(chequeId, request);
        assertThat(fetchedCheque).isNotNull();
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getChequesByUserId() {
        List<ChequeModel> cheques = chequeService.getChequesByUserId(userId, request);
        assertThat(cheques.size()).isNotEqualTo(0);
        for (ChequeModel cheque : cheques)
            assertThat(cheque.getUser().getId()).isEqualTo(userId);

    }

    @Test
    @Order(5)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateCheque() {
        var cheque = new ChequeModel();
        cheque.setIsCheckedOut(true);
        cheque = chequeService.updateCheque(cheque, chequeId, request);
        assertThat(cheque.getIsCheckedOut()).isTrue();
    }

    @Test
    @Order(6)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void findByPayToContains() {
        var fetchedCheque = chequeService.getCheque(chequeId, request);
        var cheques = chequeService.findByPayToContains(fetchedCheque.getPayTo(), request);
        assertThat(cheques.get(0)).isNotNull();
    }

    @Test
    @Order(7)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void deleteCheque() {
        chequeService.deleteCheque(chequeId, request);
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