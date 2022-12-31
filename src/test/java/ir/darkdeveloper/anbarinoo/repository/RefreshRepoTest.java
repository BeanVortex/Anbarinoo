package ir.darkdeveloper.anbarinoo.repository;

import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(DatabaseSetup.class)
class RefreshRepoTest {

    private final UserRepo userRepo;
    private final RefreshRepo refreshRepo;
    private final JwtUtils jwtUtils;
    private static Long userId;
    private static String username;
    private static Long refreshId;

    @Autowired
    RefreshRepoTest(UserRepo userRepo, RefreshRepo refreshRepo, JwtUtils jwtUtils) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.jwtUtils = jwtUtils;
    }

    @Test
    @Order(1)
    void saveUser() {
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .userName("user n")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .build();
        System.out.println(user);
        userRepo.save(user);
        assertThat(user.getId()).isNotNull();
        userId = user.getId();
        username = user.getUsername();
    }

    @Test
    @Order(2)
    void saveRefresh() {
        var refresh = RefreshModel.builder()
                .userId(userId)
                .accessToken(jwtUtils.generateAccessToken(username))
                .build();
        refreshRepo.save(refresh);
        System.out.println(refresh);
        assertThat(refresh.getId()).isNotNull();
        refreshId = refresh.getId();
    }




    @Test
    @Order(3)
    void getRefreshByUserId() {
        var refresh = refreshRepo.getRefreshByUserId(userId);
        assertThat(refresh.getUserId()).isEqualTo(userId);
        assertThat(refresh.getId()).isEqualTo(refreshId);
    }

    @Test
    @Order(4)
    void getIdByUserId() {
        var id = refreshRepo.getIdByUserId(userId);
        assertThat(id).isEqualTo(refreshId);
    }

    @Test
    @Order(5)
    void deleteTokenByUserId() {
        refreshRepo.deleteTokenByUserId(userId);
        var refresh = refreshRepo.findById(refreshId);
        assertThat(refresh.isEmpty()).isTrue();
    }
}