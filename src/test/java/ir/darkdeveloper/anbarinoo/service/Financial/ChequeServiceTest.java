package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record ChequeServiceTest(ChequeService chequeService,
                                UserService userService,
                                JwtUtils jwtUtils,
                                TestUtils testUtils) {

    private static HttpServletRequest request;
    private static Long userId;
    private static Long chequeId;

    @Autowired
    public ChequeServiceTest {
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() {
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .enabled(false)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
        var response = mock(HttpServletResponse.class);
        userService.signUpUser(Optional.of(user), response);
        var fetchedModel = (UserModel) userService.loadUserByUsername(user.getEmail());
        assertThat(fetchedModel.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedModel.getEnabled()).isEqualTo(true);
        assertThat(fetchedModel.getUserName()).isEqualTo("email");
        userId = fetchedModel.getId();
        request = testUtils.setUpHeaderAndGetReq(user.getEmail(), userId);
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCheque() {
        var cheque = ChequeModel.builder()
                .amount(new BigDecimal("554.55"))
                .nameOf("DD")
                .payTo("GG")
                .issuedAt(LocalDateTime.now())
                .validTill(LocalDateTime.now().plusDays(5))
//                .isCheckedOut(true)
                .isDebt(true)
                .build();
        cheque.setUser(new UserModel(userId));
        chequeService.saveCheque(Optional.of(cheque), request);
        assertThat(cheque.getIsDebt()).isTrue();
        assertThat(cheque.getIsCheckedOut()).isFalse();
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
        var cheques = chequeService.getChequesByUserId(userId, request);
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
        cheque = chequeService.updateCheque(Optional.of(cheque), chequeId, request);
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


}