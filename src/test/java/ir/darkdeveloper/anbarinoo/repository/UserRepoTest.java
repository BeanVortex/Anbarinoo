package ir.darkdeveloper.anbarinoo.repository;

import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
class UserRepoTest {

    private final UserRepo repo;
    private static Long userId;
    private static String email;

    @Autowired
    UserRepoTest(UserRepo repo) {
        this.repo = repo;
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
        repo.save(user);
        assertThat(user.getId()).isNotNull();
        userId = user.getId();
        email = user.getEmail();
    }

    @Test
    @Order(2)
    void findByEmailOrUsername() {
        var fetched = repo.findByEmailOrUsername(email);
        assertThat(fetched.isPresent()).isTrue();
        System.out.println(fetched.get());
        assertThat(fetched.get().getId()).isEqualTo(userId);
        assertThat(fetched.get().getEmail()).isEqualTo(email);
    }

    @Test
    @Order(3)
    void getAll() {
        var pageable = PageRequest.of(0, 8);
        var fetched = repo.getAll(pageable);
        assertThat(fetched.getContent()).isNotEmpty();
        System.out.println(fetched.getContent());
        assertThat(fetched.getContent().get(0).getId()).isEqualTo(userId);
    }

    @Test
    @Order(4)
    void updateEnabledById() {
        repo.updateEnabledById(userId, false);
        var fetched = repo.findById(userId);
        assertThat(fetched.isPresent()).isTrue();
        System.out.println(fetched.get());
        assertThat(fetched.get().getEnabled()).isFalse();
    }

    @Test
    @Order(5)
    void getSimpleUserInfo() {
        var fetched = repo.getSimpleUserInfo(userId);
        assertThat(fetched.isPresent()).isTrue();
        System.out.println(fetched.get());
        assertThat(fetched.get().getCategories()).isNull();
        assertThat(fetched.get().getRoles()).isNull();

    }
}