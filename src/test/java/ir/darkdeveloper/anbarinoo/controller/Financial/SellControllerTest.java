package ir.darkdeveloper.anbarinoo.controller.Financial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record SellControllerTest(UserService userService,
                                 ProductService productService,
                                 JwtUtils jwtUtils,
                                 WebApplicationContext webApplicationContext,
                                 CategoryService categoryService) {

    private static Long userId;
    private static Long productId;
    private static Long sellId;
    private static Long sellId2;
    private static String refresh;
    private static String access;
    private static LocalDateTime from, to;
    private static Long catId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;

    @Autowired
    public SellControllerTest {
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @BeforeEach
    void setUp2() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void saveUser() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        var user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(true);
        userService.signUpUser(user, response);
        userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        var electronics = new CategoryModel("Electronics");
        categoryService.saveCategory(electronics, request);
        catId = electronics.getId();
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveProduct() {
        var product = new ProductModel();
        product.setName("name");
        product.setDescription("description");
        product.setTotalCount(BigDecimal.valueOf(50));
        product.setPrice(BigDecimal.valueOf(500));
        product.setCategory(new CategoryModel(catId));
        productService.saveProduct(product, request);
        productId = product.getId();
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveSell() throws Exception {
        var sell = new SellModel();
        sell.setProduct(new ProductModel(productId));
        sell.setPrice(BigDecimal.valueOf(5000));
        sell.setCount(BigDecimal.valueOf(8));
        from = LocalDateTime.now();
        mockMvc.perform(post("/api/category/products/sell/save/")
                .header("refresh_token", refresh)
                .header("access_token", access)
                .content(mapToJson(sell))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value(is(productId), Long.class))
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
        var sell = new SellModel();
        sell.setProduct(new ProductModel(productId));
        sell.setPrice(BigDecimal.valueOf(500));
        sell.setCount(BigDecimal.valueOf(20));
        System.out.println(mapToJson(sell));
        mockMvc.perform(post("/api/category/products/sell/save/")
                .header("refresh_token", refresh)
                .header("access_token", access)
                .content(mapToJson(sell))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value(is(productId), Long.class))
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

        var sell = new SellModel();
        sell.setProduct(new ProductModel(productId));
        sell.setPrice(BigDecimal.valueOf(9000.568));
        sell.setCount(BigDecimal.valueOf(15));
        mockMvc.perform(put("/api/category/products/sell/update/{id}/", sellId)
                .header("refresh_token", refresh)
                .header("access_token", access)
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
                .header("refresh_token", refresh)
                .header("access_token", access)
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
                .header("refresh_token", refresh)
                .header("access_token", access)
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
        to = LocalDateTime.now();
        var financial = new FinancialModel();
        financial.setFromDate(from);
        financial.setToDate(to);
        mockMvc.perform(post("/api/category/products/sell/get-by-product/date/{id}/",
                productId)
                .header("refresh_token", refresh)
                .header("access_token", access)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapToJson(financial))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].product").value(is(productId), Long.class))
                .andExpect(jsonPath("$.totalElements").value(is(2)))
                .andDo(print())
        ;
    }

    @Test
    @Order(10)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfUserFromDateTo() throws Exception {
        to = LocalDateTime.now();
        var financial = new FinancialModel();
        financial.setFromDate(from);
        financial.setToDate(to);
        mockMvc.perform(post("/api/category/products/sell/get-by-user/date/{id}/",
                userId)
                .header("refresh_token", refresh)
                .header("access_token", access)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapToJson(financial))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].product").value(is(productId), Long.class))
                .andExpect(jsonPath("$.totalElements").value(is(2)))
                .andDo(print())
        ;

    }

    @Test
    @Order(11)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getSell() throws Exception {
        mockMvc.perform(get("/api/category/products/sell/{id}/?page={page}&size={size}",
                sellId, 0, 2)
                .header("refresh_token", refresh)
                .header("access_token", access)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.product").value(is(productId), Long.class))
                .andExpect(jsonPath("$.id").value(is(sellId), Long.class))
                .andExpect(jsonPath("$.count").value(is(BigDecimal.valueOf(15.0)), BigDecimal.class))
                .andDo(print());
    }

    @Test
    @Order(12)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteSell() throws Exception {

        mockMvc.perform(delete("/api/category/products/sell/{id}/", sellId)
                .header("refresh_token", refresh)
                .header("access_token", access)
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
                .header("refresh_token", refresh)
                .header("access_token", access)
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
                .header("refresh_token", refresh)
                .header("access_token", access)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    private String mapToJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(String email, Long userId) {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        refresh = jwtUtils.generateRefreshToken(email, userId);
        access = jwtUtils.generateAccessToken(email);
        var refreshDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refresh));
        var accessDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(access));
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