package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record DebtOrDemandServiceTest(DebtOrDemandService demandService,
                                      UserService userService,
                                      JwtUtils jwtUtils, TestUtils testUtils) {

    // Other methods in DebtOrDemandService are covered in ChequeServiceTest

    private static HttpServletRequest request;
    private static Long userId;
    private static Long dodId;

    @Autowired
    public DebtOrDemandServiceTest {

    }

    @Test
    @Order(1)
    void saveUser() {
        var response = new MockHttpServletResponse();
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .enabled(false)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
        userService.signUpUser(Optional.of(user), response);
        var fetchedModel = (UserModel) userService.loadUserByUsername(user.getEmail());
        assertThat(fetchedModel.getEmail()).isEqualTo(user.getEmail());
        assertThat(fetchedModel.getEnabled()).isEqualTo(true);
        assertThat(fetchedModel.getUserName()).isEqualTo("email");
        userId = fetchedModel.getId();
        request = testUtils.setUpHeaderAndGetReqWithRes(response);
    }


    @Test
    @Order(2)
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
        demandService.updateDOD(Optional.of(dod), dodId, false, request);
        var fetchedDod = demandService.getDOD(dodId, request);
        assertThat(fetchedDod.getId()).isEqualTo(dodId);
    }

    @Test
    @Order(4)
    void getAllDODRecordsOfUser() {
        var pageable = PageRequest.of(0, 8);
        var fetchedDods = demandService.getAllDODRecordsOfUser(userId, request, pageable);
        assertThat(fetchedDods.getContent().size()).isEqualTo(1);
        assertThat(fetchedDods.getContent().get(0).getId()).isEqualTo(dodId);
        assertThat(fetchedDods.getContent().get(0).getPayTo()).isEqualTo("Me");
    }

    @Test
    @Order(5)
    void getDOD() {
        var fetchedDod = demandService.getDOD(dodId, request);
        assertThat(fetchedDod.getPayTo()).isEqualTo("Me");
        assertThat(fetchedDod.getNameOf()).isEqualTo("Other");
    }

    @Test
    @Order(6)
    void deleteDOD() {
        var deleteRes = demandService.deleteDOD(dodId, request);
        assertThat(deleteRes).isEqualTo("Debt or Demand deleted");
    }

}
