package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.springframework.security.test.context.support.WithMockUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record ProductServiceTest(ProductService productService,
                                 JwtUtils jwtUtils,
                                 UserService userService) {


    private static ProductModel product;
    private static UserModel user;
    private static HttpServletRequest request;


    @Autowired
    public ProductServiceTest {
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        user = new UserModel();
        user.setEmail("email");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(true);
        MockMultipartFile file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        user.setProfileFile(file1);
        user.setShopFile(file2);
        product = new ProductModel();
        product.setName("name");
        product.setDescription("description");
        product.setBoughtCount(25);
        product.setBuyPrice(156d);
        product.setSellPrice(180d);
        product.setSoldCount(13);
        product.setTotalCount(50);
        CategoryModel cat = new CategoryModel();
        cat.setName("cat");
        product.setCategory(cat);
        request = mock(HttpServletRequest.class);
    }


    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        userService.signUpUser(user, response);
        request = setUpHeader();
    }


    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveProduct() {
        productService.saveProduct(product, request);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getProduct() {
        Pageable pageable = mock(Pageable.class);
        productService.findByNameContains(product.getName().substring(0, 2), pageable, request);
    }

    @Test
    @Order(4)
    void updateProduct() {
        Pageable pageable = mock(Pageable.class);
        productService.findByNameContains(product.getName().substring(0, 2), pageable, request);
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
