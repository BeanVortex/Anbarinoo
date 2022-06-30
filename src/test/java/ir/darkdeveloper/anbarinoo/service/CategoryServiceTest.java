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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    public CategoryServiceTest {
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
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
    @WithMockUser(username = "email4@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCategory() {

        var user = new UserModel(userId);
        electronics = new CategoryModel("Electronics");
        electronics.setUser(user);
        var mobilePhones = new CategoryModel("Mobile phones", electronics);
        mobilePhones.setUser(user);
        var washingMachines = new CategoryModel("Washing machines", electronics);
        washingMachines.setUser(user);
        electronics.addChild(mobilePhones);
        electronics.addChild(washingMachines);
        var iPhone = new CategoryModel("iPhone", mobilePhones);
        iPhone.setUser(user);
        var samsung = new CategoryModel("Samsung", mobilePhones);
        samsung.setUser(user);
        mobilePhones.addChild(iPhone);
        mobilePhones.addChild(samsung);
        var galaxy = new CategoryModel("Galaxy", samsung);
        galaxy.setUser(user);
        samsung.addChild(galaxy);
        categoryService.saveCategory(Optional.of(electronics), request);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email4@mail.com", authorities = {"OP_ACCESS_USER"})
    void getCategoriesByUser() {
        var categories = categoryService.getCategoriesByUser(request);
        assertThat(categories.size()).isNotEqualTo(0);
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email4@mail.com", authorities = {"OP_ACCESS_USER"})
    void getParentCategoryById() {
        /* var parentCat =  */
        categoryService.getCategoryById(electronics.getId(), request);
        // tested in postman and was ok
        // could not test here because of lazy initialization
        //        assertThat(parentCat.getChildren().isEmpty()).isTrue();
    }

    @Test
    @Order(5)
    @WithMockUser(username = "email4@mail.com", authorities = {"OP_ACCESS_USER"})
    void deleteCategory() {
        categoryService.deleteCategory(electronics.getId(), request);
    }

    @Test
    @Order(6)
    @WithMockUser(username = "email4@mail.com", authorities = {"OP_ACCESS_USER"})
    void getUserAfterCatDelete() {
        var fetchedUser = userService.getUserInfo(userId, request);
        assertThat(fetchedUser).isNotNull();
    }

}
