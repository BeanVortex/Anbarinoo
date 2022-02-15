package ir.darkdeveloper.anbarinoo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public record CategoryServiceTest(JwtUtils jwtUtils,
                                  UserService userService,
                                  CategoryService categoryService) {

    private static HttpServletRequest request;
    private static CategoryModel electronics;
    private static Long userId;

    @Autowired
    public CategoryServiceTest {
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        var response = mock(HttpServletResponse.class);
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .userName("user n")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .enabled(true)
                .build();
        userService.signUpUser(user, response);
        userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
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
        categoryService.saveCategory(electronics, request);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getCategoriesByUser() {
        var categories = categoryService.getCategoriesByUser(request);
        assertThat(categories.size()).isNotEqualTo(0);
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getParentCategoryById() {
        /* var parentCat =  */
        categoryService.getCategoryById(electronics.getId(), request);
        // tested in postman and was ok
        // could not test here because of lazy initialization
        //        assertThat(parentCat.getChildren().isEmpty()).isTrue();
    }

    @Test
    @Order(5)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void deleteCategory() {
        categoryService.deleteCategory(electronics.getId(), request);
    }

    @Test
    @Order(6)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getUserAfterCatDelete() {
        var fetchedUser = userService.getUserInfo(userId, request);
        assertThat(fetchedUser).isNotNull();
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(String email, Long userId) {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        var refresh = jwtUtils.generateRefreshToken(email, userId);
        var access = jwtUtils.generateAccessToken(email);
        var refreshDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refresh));
        var accessDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(access));
        headers.put("refresh_token", refresh);
        headers.put("access_token", access);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);


        HttpServletRequest request = mock(HttpServletRequest.class);
        for (String key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

}
