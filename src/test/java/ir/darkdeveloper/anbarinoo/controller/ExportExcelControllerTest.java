package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "docs/product-excel")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
record ExportExcelControllerTest(WebApplicationContext webApplicationContext,
                                 CategoryService categoryService,
                                 UserService userService,
                                 ProductService productService,
                                 RestDocumentationContextProvider restDocumentation,
                                 JwtUtils jwtUtils, TestUtils testUtils) {


    private static Long userId;
    private static HttpServletRequest request;
    private static HttpHeaders authHeaders;
    private static Long productId;
    private static Long catId;
    private static MockMvc mockMvc;


    @Autowired
    public ExportExcelControllerTest {
    }


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(document("{method-name}"))
                .build();
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
                .build();
        userService.signUpUser(Optional.of(user), response);
        userId = user.getId();
        var userEmail = user.getEmail();
        request = testUtils.setUpHeaderAndGetReq(userEmail, userId);
        authHeaders = testUtils.getAuthHeaders(userEmail, userId);
    }


    @Test
    @Order(2)
    void saveCategory() {
        var electronics = new CategoryModel("Electronics");
        request = testUtils.setUpHeaderAndGetReq("email@mail.com", userId);
        categoryService.saveCategory(Optional.of(electronics), request);
        catId = electronics.getId();
    }


    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
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
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getProductsExcel() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/export/excel/products")
                        .headers(authHeaders)
                        .accept(MediaType.APPLICATION_OCTET_STREAM)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/octet-stream");
    }
}