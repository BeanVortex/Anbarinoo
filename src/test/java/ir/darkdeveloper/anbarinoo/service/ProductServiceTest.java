package ir.darkdeveloper.anbarinoo.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;

@SpringBootTest
public class ProductServiceTest {

    private final ProductService productService;
    private ProductModel product;
    private UserModel user;

    @Autowired
    public ProductServiceTest(ProductService productService) {
        this.productService = productService;
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    void productSetup(){
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
        product.setUser(user);
        CategoryModel cat =  new CategoryModel();
        cat.setName("cat");
        product.setCategory(cat);
        
    }

    @Test
    void saveProduct() {

    }

}
