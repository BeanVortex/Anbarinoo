package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        CategoryModel mobilePhones = new CategoryModel("Mobile phones", electronics);
        CategoryModel washingMachines = new CategoryModel("Washing machines", electronics);
        electronics.addChild(mobilePhones);
        electronics.addChild(washingMachines);
        CategoryModel iPhone = new CategoryModel("iPhone", mobilePhones);
        CategoryModel samsung = new CategoryModel("Samsung", mobilePhones);
        mobilePhones.addChild(iPhone);
        mobilePhones.addChild(samsung);
        CategoryModel galaxy = new CategoryModel("Galaxy", samsung);
        samsung.addChild(galaxy);

        categoryService.saveCategory(electronics, request);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getCategoriesByUserId() {
        List<CategoryModel> categories = categoryService.getCategoriesByUserId(user.getId(), request);
        assertThat(categories.size()).isNotEqualTo(0);
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
