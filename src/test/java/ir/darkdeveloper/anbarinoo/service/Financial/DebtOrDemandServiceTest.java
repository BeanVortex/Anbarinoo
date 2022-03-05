package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public record DebtOrDemandServiceTest(DebtOrDemandService demandService,
                                      UserService userService,
                                      JwtUtils jwtUtils) {

    // Other methods in DebtOrDemandService are covered in ChequeServiceTest

    private static HttpServletRequest request;
    private static Long userId;
    private static Long dodId;

    @Autowired
    public DebtOrDemandServiceTest {

    }

    @BeforeAll
    static void setUp() {
        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .enabled(false)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
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
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveDOD() {
        var dod = DebtOrDemandModel.builder()
                .amount(BigDecimal.valueOf(115.56))
                .isDebt(true)
                .issuedAt(LocalDateTime.now())
                .validTill(LocalDateTime.now().plusDays(5))
                .nameOf("Me")
                .payTo("Other")
                .build();
        demandService.saveDOD(Optional.of(dod), request);
        dodId = dod.getId();
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateDOD() {
        var dod = DebtOrDemandModel.builder()
                //should ignore id
                .id(25L)
                .amount(BigDecimal.valueOf(1564))
                .isDebt(false)
                .validTill(LocalDateTime.now().plusDays(8))
                .nameOf("Other")
                .payTo("Me")
                .build();
        demandService.updateDOD(Optional.of(dod), dodId, request);
        var fetchedDod = demandService.getDOD(dodId, request);
        assertThat(fetchedDod.getId()).isEqualTo(dodId);
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllDODRecordsOfUser() {
        var pageable = PageRequest.of(0, 8);
        var fetchedDods = demandService.getAllDODRecordsOfUser(userId, request, pageable);
        assertThat(fetchedDods.getContent().size()).isEqualTo(1);
        assertThat(fetchedDods.getContent().get(0).getId()).isEqualTo(dodId);
        assertThat(fetchedDods.getContent().get(0).getPayTo()).isEqualTo("Me");
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getDOD() {
        var fetchedDod = demandService.getDOD(dodId, request);
        assertThat(fetchedDod.getPayTo()).isEqualTo("Me");
        assertThat(fetchedDod.getNameOf()).isEqualTo("Other");
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteDOD() {
        var deleteRes = demandService.deleteDOD(dodId, request);
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    //should return the object; data is being removed
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
        for (String key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

}
