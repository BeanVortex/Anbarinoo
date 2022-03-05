package ir.darkdeveloper.anbarinoo.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public record ProductServiceTest(ProductService productService,
                                 JwtUtils jwtUtils,
                                 UserService userService,
                                 CategoryService categoryService) {


    private static HttpServletRequest request;
    private static Long catId;
    private static Long userId;
    //    private static Long userId2;
    private static Long productId;

    @Autowired
    public ProductServiceTest {
    }

    @BeforeAll
    static void setUp() {
        var authentication = Mockito.mock(Authentication.class);
        var securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }


    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() {
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
        var user2 = UserModel.builder()
                .email("email2@mail.com")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .build();
        userService.signUpUser(user2, response);
//        userId2 = user2.getId();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        var electronics = new CategoryModel("Electronics");
        categoryService.saveCategory(electronics, request);
        catId = electronics.getId();
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveProduct() {
        var product = ProductModel.builder()
                .name("name")
                .description("description")
                .totalCount(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(500))
                .category(new CategoryModel(catId))
                .tax(9)
                .build();
        var file3 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file4 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        product.setFiles(Arrays.asList(file3, file4));
        product.setCategory(new CategoryModel(catId));
        productService.saveProduct(Optional.of(product), request);
        productId = product.getId();
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getProduct() {
        var fetchedProduct = productService.getProduct(productId, request);
        assertThat(fetchedProduct.getCategory().getUser().getId()).isEqualTo(userId);
    }

    @Test
    @Order(5)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateProduct() {
        var product = ProductModel.builder()
                .name("updatedName")
                .description("updatedDescription")
                .totalCount(BigDecimal.valueOf(10))
                .category(new CategoryModel(catId))
                .build();

        productService.updateProduct(Optional.of(product), productId, request);
        var fetchedProduct = productService.getProduct(productId, request);
        assertThat(fetchedProduct.getCategory().getName())
                .isEqualTo("Electronics");
        assertThat(fetchedProduct.getTotalCount()).isEqualTo(BigDecimal.valueOf(10_0000, 4));
        assertThat(fetchedProduct.getPrice()).isEqualTo(BigDecimal.valueOf(500_0000, 4));
    }

    @Test
    @Order(6)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateProductImages() {
        var product = new ProductModel();

        var file3 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file4 = new MockMultipartFile("file", "hole.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file5 = new MockMultipartFile("file", "halo.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

        product.setFiles(Arrays.asList(file3, file4, file5));

        productService.updateProductImages(Optional.of(product), productId, request);

        var fetchedProduct = productService.getProduct(productId, request);
        assertThat(fetchedProduct.getImages().size()).isNotEqualTo(0);
        for (var image : fetchedProduct.getImages())
            assertThat(image).isNotEqualTo("noImage.png");
    }

    @Test
    @Order(7)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void delete3UpdateProductImages() {
        var product = new ProductModel();
        var fetchedProduct = productService.getProduct(productId, request);
        var fileNames = fetchedProduct.getImages();
        fileNames.remove(0);
        fileNames.remove(1);
        product.setImages(fileNames);

        productService.updateDeleteProductImages(Optional.of(product), productId, request);

        var fetchedProduct2 = productService.getProduct(productId, request);
        assertThat(fetchedProduct2.getImages().size()).isEqualTo(2);
        for (var image : fetchedProduct2.getImages())
            assertThat(image).isNotEqualTo("noImage.png");
    }

    @Test
    @Order(8)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Disabled
    void deleteAllUpdateProductImages() {
        var product = new ProductModel();
        var fetchedProduct = productService.getProduct(productId, request);
        product.setImages(fetchedProduct.getImages());

        productService.updateDeleteProductImages(Optional.of(product), productId, request);

        var fetchedProduct2 = productService.getProduct(productId, request);
        assertThat(fetchedProduct2.getImages().size()).isNotEqualTo(0);
        for (var image : fetchedProduct2.getImages())
            assertThat(image).isEqualTo("noImage.png");
    }


    @Test
    @Order(9)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void findByNameContains() {
        var pageable = PageRequest.of(0, 8);
        var product = new ProductModel();
        product.setName("updatedName");
        var foundProducts = productService.findByNameContains(product.getName().substring(0, 2), pageable, request);
        foundProducts.getContent().forEach(p -> assertThat(p.getName()).isEqualTo(product.getName()));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    @Disabled
    void deleteProduct() {
        productService.deleteProduct(productId, request);
    }

    @Test
    @Order(11)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER", "OP_DELETE_USER"})
    void deleteUser() {
        // should delete all products and product images of this user
        // for images check build/resources/test/static/user/product_images/
        userService.deleteUser(userId, request);
        assertThrows(NoContentException.class, () -> categoryService.getCategoryById(catId, request));
        assertThrows(NoContentException.class, () -> productService.getProduct(productId, request));
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(String email, Long userId) {

        var headers = new HashMap<String, String>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        var refreshToken = jwtUtils.generateRefreshToken(email, userId);
        var accessToken = jwtUtils.generateAccessToken(email);
        var refreshDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        headers.put("refresh_token", refreshToken);
        headers.put("access_token", accessToken);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);


        var request = mock(HttpServletRequest.class);
        for (var key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

}
