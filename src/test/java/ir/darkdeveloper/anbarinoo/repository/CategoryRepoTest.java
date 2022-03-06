package ir.darkdeveloper.anbarinoo.repository;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ActiveProfiles("h2")
@DirtiesContext
class CategoryRepoTest {

    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private static Long catId;
    private static Long userId;

    @Autowired
    CategoryRepoTest(UserRepo userRepo, CategoryRepo categoryRepo) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
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
        userRepo.save(user);
        assertThat(user.getId()).isNotNull();
        userId = user.getId();
    }

    @Test
    @Order(2)
    void saveCategory() {
        var electronics = CategoryModel.builder()
                .user(new UserModel(userId))
                .name("Electronics")
                .build();
        categoryRepo.save(electronics);
        assertThat(electronics.getId()).isNotNull();
        catId = electronics.getId();
    }

    @Test
    @Order(3)
    void findAllByUserId() {
        var categories = categoryRepo.findAllByUserId(userId);
        assertThat(categories.isEmpty()).isFalse();
        System.out.println(categories);
        var ids = categories.stream()
                .map(CategoryModel::getId)
                .toList();
        assertThat(ids).contains(catId);
    }
}