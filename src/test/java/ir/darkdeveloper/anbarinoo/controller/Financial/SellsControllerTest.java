package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
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
public record SellsControllerTest(SellsController sellController,
                           UserService userService,
                           ProductService productService,
                           JwtUtils jwtUtils,
                           WebApplicationContext webApplicationContext) {

    private static Long userId;
    private static Long productId;
    private static Long sellId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;

    @Autowired
    public SellsControllerTest {
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
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveProduct() {
        var product = new ProductModel();
        product.setName("name");
        product.setDescription("description");
        product.setTotalCount(50);
        product.setPrice(BigDecimal.valueOf(500));
        productService.saveProduct(product, request);
        productId = product.getId();
    }


    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveSell() throws Exception {
        var sell = new SellsModel();
        sell.setProduct(new ProductModel(productId));
        sell.setPrice(BigDecimal.valueOf(5000));
        sell.setCount(BigDecimal.valueOf(8));
        mockMvc.perform(post("/api/product/sell/save/")
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
                .content(mapToJson(sell))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value(is(productId), Long.class))
                .andExpect(jsonPath("$").isMap())
                .andDo(print())
                .andDo(result -> {
                    JSONObject obj = new JSONObject(result.getResponse().getContentAsString());
                    sellId = obj.getLong("id");
                });
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateSell() throws Exception {

        var sell = new SellsModel();
        sell.setProduct(new ProductModel(productId));
        sell.setPrice(BigDecimal.valueOf(9000.568));
        sell.setCount(BigDecimal.valueOf(60.2));
        mockMvc.perform(put("/api/product/sell/update/{id}/", sellId)
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
                .content(mapToJson(sell))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.price").value(is(BigDecimal.valueOf(9000.568)), BigDecimal.class))
                .andExpect(jsonPath("$.count").value(is(BigDecimal.valueOf(60.2)), BigDecimal.class))
                .andDo(print());

    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfProduct() throws Exception {

        mockMvc.perform(get("/api/product/sell/get-by-product/{id}/?page={page}&size={size}",
                productId, 5, 6)
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageSize").value(is(6)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(is(5)))
                .andExpect(jsonPath("$.totalPages").value(is(1)))
                .andDo(print());
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllSellRecordsOfUser() throws Exception {

        mockMvc.perform(get("/api/product/sell/get-by-user/{id}/?page={page}&size={size}",
                userId, 0, 2)
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
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
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getSell() throws Exception {
        mockMvc.perform(get("/api/product/sell/{id}/?page={page}&size={size}",
                sellId, 0, 2)
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").value(is(5)))
                .andExpect(jsonPath("$.count").value(is(BigDecimal.valueOf(60.2)), BigDecimal.class))
                .andDo(print());
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteSell() throws Exception {

        mockMvc.perform(delete("/api/product/sell/{id}/", sellId)
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private String mapToJson(SellsModel sell) {
        return """
                {
                    "id":%d,
                    "count":%f,
                    "price":%f,
                    "tax":%d,
                    "product":
                         {
                            "id":%d
                         }
                    }
                """.formatted(sell.getId(), sell.getCount(), sell.getPrice(),
                sell.getTax(), sell.getProduct().getId());
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(String email, Long userId) {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        String refreshToken = jwtUtils.generateRefreshToken(email, userId);
        String accessToken = jwtUtils.generateAccessToken(email);
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