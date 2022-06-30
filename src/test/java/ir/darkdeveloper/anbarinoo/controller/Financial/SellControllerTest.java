package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.SellModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static ir.darkdeveloper.anbarinoo.TestUtils.mapToJson;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "docs/sell_product")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record SellControllerTest(UserService userService,
                                 ProductService productService,
                                 JwtUtils jwtUtils,
                                 WebApplicationContext webApplicationContext,
                                 RestDocumentationContextProvider restDocumentation,
                                 CategoryService categoryService,
                                 TestUtils testUtils) {

    private static Long userId;
    private static Long productId;
    private static Long sellId;
    private static Long sellId2;
    private static HttpHeaders authHeaders;
    private static LocalDateTime from, to;
    private static Long catId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;

    @Autowired
    public SellControllerTest {
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
        var response = new MockHttpServletResponse();
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
        var userEmail = user.getEmail();
        userId = user.getId();
        request = testUtils.setUpHeaderAndGetReqWithRes(response);
        authHeaders = testUtils.getAuthHeaders(response);
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        var electronics = new CategoryModel("Electronics");
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
        productService.saveProduct(Optional.of(product), request);
        productId = product.getId();
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveSell() throws Exception {
        var sell = SellModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(5000))
                .count(BigDecimal.valueOf(8))
                .tax(9)
                .build();
        from = LocalDateTime.now().minusHours(1);
        mockMvc.perform(post("/api/category/products/sell/save/")
                        .headers(authHeaders)
                        .content(mapToJson(sell))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(is(productId), Long.class))
                .andExpect(jsonPath("$").isMap())
                .andDo(result -> {
                    var obj = new JSONObject(result.getResponse().getContentAsString());
                    sellId = obj.getLong("id");
                });
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveSell2() throws Exception {
        var sell = SellModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(500))
                .count(BigDecimal.valueOf(20))
                .tax(9)
                .build();
        mockMvc.perform(post("/api/category/products/sell/save/")
                        .headers(authHeaders)
                        .content(mapToJson(sell))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(is(productId), Long.class))
                .andExpect(jsonPath("$").isMap())
                .andDo(result -> {
                    var obj = new JSONObject(result.getResponse().getContentAsString());
                    sellId2 = obj.getLong("id");
                });
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateSell() throws Exception {
        var sell = SellModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(9000.568))
                .count(BigDecimal.valueOf(15))
                .build();
        mockMvc.perform(put("/api/category/products/sell/update/{id}/", sellId)
                        .headers(authHeaders)
                        .content(mapToJson(sell))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.price").value(is(BigDecimal.valueOf(9000.568)), BigDecimal.class))
                .andExpect(jsonPath("$.count").value(is(BigDecimal.valueOf(15)), BigDecimal.class))
        ;

    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfProduct() throws Exception {

        mockMvc.perform(get("/api/category/products/sell/get-by-product/{id}/?page={page}&size={size}",
                        productId, 0, 1)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageSize").value(is(1)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(is(0)))
                .andExpect(jsonPath("$.totalPages").value(is(2)))
                .andDo(print());
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfUser() throws Exception {

        mockMvc.perform(get("/api/category/products/sell/get-by-user/{id}/?page={page}&size={size}",
                        userId, 0, 2)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.pageable.pageSize").value(is(2)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(is(0)))
                .andExpect(jsonPath("$.totalPages").value(is(1)))
                .andDo(print());

    }

    @Test
    @Order(9)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfProductFromDateTo() throws Exception {
        to = LocalDateTime.now().plusMinutes(1);
        var financial = new FinancialDto(from, to);
        mockMvc.perform(post("/api/category/products/sell/get-by-product/date/{id}/",
                        productId)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].productId").value(is(productId), Long.class))
                .andExpect(jsonPath("$.totalElements").value(is(2)))
        ;
    }

    @Test
    @Order(10)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfUserFromDateTo() throws Exception {
        to = LocalDateTime.now().plusMinutes(1);
        var financial = new FinancialDto(from, to);

        mockMvc.perform(post("/api/category/products/sell/get-by-user/date/{id}/",
                        userId)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].productId").value(is(productId), Long.class))
                .andExpect(jsonPath("$.totalElements").value(is(2)))
        ;

    }

    @Test
    @Order(11)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getSell() throws Exception {
        mockMvc.perform(get("/api/category/products/sell/{id}/?page={page}&size={size}",
                        sellId, 0, 2)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.productId").value(is(productId), Long.class))
                .andExpect(jsonPath("$.id").value(is(sellId), Long.class))
                .andExpect(jsonPath("$.count").value(is(BigDecimal.valueOf(15.0)), BigDecimal.class))
                .andDo(print());
    }

    @Test
    @Order(12)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteSell() throws Exception {

        mockMvc.perform(delete("/api/category/products/sell/{id}/", sellId)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Order(13)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfProductAfterSellDelete() throws Exception {

        mockMvc.perform(get("/api/category/products/sell/get-by-product/{id}/?page={page}&size={size}",
                        productId, 0, 2)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageSize").value(is(2)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(is(0)))
                .andExpect(jsonPath("$.totalElements").value(is(1)))
        ;
    }

    @Test
    @Order(14)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getSellRecordOfAProductAfterProductDelete() throws Exception {

        productService.deleteProduct(productId, request);

        mockMvc.perform(get("/api/category/products/sell/{id}/",
                        sellId2)
                        .headers(authHeaders)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}