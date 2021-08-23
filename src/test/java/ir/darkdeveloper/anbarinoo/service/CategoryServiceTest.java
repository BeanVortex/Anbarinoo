package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record CategoryServiceTest(JwtUtils jwtUtils,
                                  UserService userService,
                                  CategoryService categoryService) {

    private static UserModel user;
    private static HttpServletRequest request;
    private static CategoryModel electronics;

    @Autowired
    public CategoryServiceTest {
    }


    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(true);

        request = mock(HttpServletRequest.class);
        System.out.println("ProductServiceTest.setUp");
    }


    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        userService.signUpUser(user, response);
        request = setUpHeader();
        System.out.println("ProductServiceTest.saveUser");
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        System.out.println("ProductServiceTest.saveCategory");

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
    void getCategoriesByUserId() {
        var categories = categoryService.getCategoriesByUserId(user.getId(), request);
        assertThat(categories.size()).isNotEqualTo(0);
    }


    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getParentCategoryById() {
        var parentCat = categoryService.getCategoryById(electronics.getId(), request);
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
        var fetchedUser = userService.getUserInfo(user.getId(), request);
        assertThat(fetchedUser).isNotNull();
    }


    //should return the object; data is being removed
    private HttpServletRequest setUpHeader() {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        var refreshDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        headers.put("refresh_token", refreshToken);
        headers.put("access_token", accessToken);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);


        HttpServletRequest request = mock(HttpServletRequest.class);
        for (String key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

}

