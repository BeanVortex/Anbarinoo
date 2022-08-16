package ir.darkdeveloper.anbarinoo.repository;

import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
class ProductRepositoryTest {

    private final UserRepo userRepo;
    private final CategoryRepo categoryRepo;
    private final ProductRepository productRepo;
    private static Long catId;
    private static Long userId;

    @Autowired
    ProductRepositoryTest(UserRepo userRepo, CategoryRepo categoryRepo, ProductRepository productRepo) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
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
    void saveProduct() {
        var product = ProductModel.builder()
                .category(new CategoryModel(catId))
                .price(BigDecimal.TEN)
                .tax(9)
                .name("abc_Product_efg")
                .description("abc_description_efg")
                .totalCount(BigDecimal.TEN)
                .build();
        productRepo.save(product);
        assertThat(product.getId()).isNotNull();
        System.out.println(product);
    }

    @Test
    @Order(4)
    void findByNameContainsAndUserId() {
        var pageable = PageRequest.of(0, 8);
        var fetched = productRepo.findByNameContainsAndUserId("DUCT",
                userId, pageable);
        assertThat(fetched.getContent()).hasSizeGreaterThan(0);
        assertThat(fetched.getContent().get(0).getName()).containsIgnoringCase("DUCT");
        System.out.println(fetched.getContent());
    }

    @Test
    @Order(5)
    void findAllByUserId() {
        var pageable = PageRequest.of(0, 8);
        var fetched = productRepo.findAllByUserId(userId, pageable);
        assertThat(fetched.getContent()).hasSizeGreaterThan(0);
        System.out.println(fetched.getContent());
    }
}