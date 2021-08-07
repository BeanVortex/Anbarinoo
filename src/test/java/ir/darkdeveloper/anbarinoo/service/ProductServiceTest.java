package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record ProductServiceTest(ProductService productService,
                                 JwtUtils jwtUtils,
                                 UserService userService,
                                 CategoryService categoryService) {


    private static ProductModel product;
    private static UserModel user;
    private static HttpServletRequest request;
    private static CategoryModel cat1;
    private static CategoryModel electronics;


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
        user.setEmail("email@mail.com");
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

    //sub cat should save before the top cat
    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        System.out.println("ProductServiceTest.saveCategory");
        cat1 = new CategoryModel("Other");
        categoryService.saveCategory(cat1);
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

        categoryService.saveCategory(electronics);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveProduct() {
        System.out.println("ProductServiceTest.saveProduct");
        product.setCategory(cat1);
        productService.saveProduct(product, request);
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getProduct() {
        System.out.println("ProductServiceTest.getProduct");
        var fetchedProduct = productService.getProduct(product.getId(), request);
        assertThat(fetchedProduct.getUser().getId()).isEqualTo(user.getId());
        assertThat(fetchedProduct.getBoughtCount()).isEqualTo(product.getBoughtCount());
        assertThat(fetchedProduct.getSoldCount()).isEqualTo(product.getSoldCount());
        assertThat(fetchedProduct.getTotalCount()).isEqualTo(product.getTotalCount());
        assertThat(fetchedProduct.getId()).isEqualTo(product.getId());
        assertThat(fetchedProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateProduct() {
        System.out.println("ProductServiceTest.updateProduct");
        product.setName("updatedName");
        product.setDescription("updatedDescription");
        product.setBoughtCount(15);
        product.setBuyPrice(25d);
        product.setSellPrice(5d);
        product.setSoldCount(6);
        product.setTotalCount(10);
        product.setCategory(electronics);
        productService.updateProduct(product, request);
        getProduct();
        var fetchedProduct = productService.getProduct(product.getId(), request);
        assertThat(fetchedProduct.getCategory().getName())
                .isEqualTo(product.getCategory().getName());

    }

    @Test
    @Order(6)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void findByNameContains() {
        System.out.println("ProductServiceTest. findByNameContains");
        var pageable = PageRequest.of(0, 8);
        var foundProducts = productService.findByNameContains(product.getName().substring(0, 2), pageable, request);
        foundProducts.getContent().forEach(p -> {
            assertThat(p.getName()).isEqualTo(product.getName());
            assertThat(p.getBoughtCount()).isEqualTo(product.getBoughtCount());
        });
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
