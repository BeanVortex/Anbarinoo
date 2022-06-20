package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record ProductServiceTest(ProductService productService,
                                 JwtUtils jwtUtils,
                                 UserService userService,
                                 CategoryService categoryService,
                                 TestUtils testUtils) {


    private static HttpServletRequest request;
    private static Long catId;
    private static Long userId;
    //    private static Long userId2;
    private static Long productId;

    @Autowired
    public ProductServiceTest {
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
        userService.signUpUser(Optional.of(user), response);
        userId = user.getId();
        request = testUtils.setUpHeaderAndGetReq(user.getEmail(), userId);
        var user2 = UserModel.builder()
                .email("email2@mail.com")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .build();
        userService.signUpUser(Optional.of(user2), response);
//        userId2 = user2.getId();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        var electronics = new CategoryModel("Electronics");
        categoryService.saveCategory(Optional.of(electronics), request);
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

        // user is deleted so can't access to system
        assertThrows(NoContentException.class,
                () -> categoryService.getCategoryById(catId, request),
                "Category is not found");
        assertThrows(NoContentException.class,
                () -> productService.getProduct(productId, request),
                "This product does not exist");

    }

}
