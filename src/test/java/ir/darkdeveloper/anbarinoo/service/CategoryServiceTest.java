package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record CategoryServiceTest(JwtUtils jwtUtils,
                                  UserService userService,
                                  CategoryService categoryService,
                                  TestUtils testUtils) {

    private static HttpServletRequest request;
    private static CategoryModel electronics;
    private static Long userId;
    private static Long catId;

    @Autowired
    public CategoryServiceTest {
    }

    @Test
    @Order(1)
    void saveUser() {
        var response = new MockHttpServletResponse();
        var user = UserModel.builder()
                .email("email4@mail.com")
                .address("address")
                .description("desc")
                .userName("user n4")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .enabled(true)
                .build();
        userService.signUpUser(Optional.of(user), response);
        userId = user.getId();
        request = testUtils.setUpHeaderAndGetReqWithRes(response);
    }

    @Test
    @Order(2)
    void saveCategory() {
        var user = new UserModel(userId);
        electronics = new CategoryModel("Electronics");
        electronics.setUser(user);
        categoryService.saveCategory(Optional.of(electronics), request);
        catId = electronics.getId();
    }

    @Test
    @Order(3)
    void getCategoriesByUser() {
        var categories = categoryService.getCategoriesByUser(request, userId);
        assertThat(categories.size()).isNotEqualTo(0);
    }

    @Test
    @Order(4)
    void getParentCategoryById() {
        var categoryById = categoryService.getCategoryById(electronics.getId(), request);
        assertThat(categoryById.getId()).isEqualTo(catId);
        assertThat(categoryById.getUser().getId()).isEqualTo(userId);
    }

    @Test
    @Order(5)
    void addChildren() {
        var galaxy = new CategoryModel("Galaxy");
        var subCategory = categoryService.saveSubCategory(Optional.of(galaxy), catId, request);

        assertThat(subCategory.getName()).isEqualTo("Galaxy");
        assertThat(subCategory.getParent().getId()).isEqualTo(catId);
    }

    @Test
    @Order(6)
    void deleteCategory() {
        categoryService.deleteCategory(electronics.getId(), request);
    }

    @Test
    @Order(7)
    void getUserAfterCatDelete() {
        var fetchedUser = userService.getUserInfo(userId, request);
        assertThat(fetchedUser).isNotNull();
    }

}
