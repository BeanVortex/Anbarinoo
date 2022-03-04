package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public record BuyServiceTest(UserService userService,
                             JwtUtils jwtUtils,
                             ProductService productService,
                             BuyService buyService,
                             CategoryService categoryService) {

    private static HttpServletRequest request;
    private static Long userId;
    private static Long buyId;
    private static Long catId;
    private static Long productId;
    private static Pageable pageable;

    @Autowired
    public BuyServiceTest {
    }

    @BeforeAll
    static void setUp() {
        var authentication = Mockito.mock(Authentication.class);
        var securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
        pageable = PageRequest.of(0, 8);
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
                .enabled(true)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
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
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveProduct() {
        var product = ProductModel.builder()
                .name("name")
                .description("description")
                .totalCount(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(56))
                .category(new CategoryModel(catId))
                .tax(9)
                .build();
        productService.saveProduct(Optional.of(product), request);
        productId = product.getId();
    }


    @Test
    @Order(4)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveBuy() {
        var buyRecord = BuyModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(50))
                .count(BigDecimal.valueOf(20))
                .tax(9)
                .build();
        buyService.saveBuy(Optional.of(buyRecord), false, request);
        buyId = buyRecord.getId();
        var fetchedBuy = buyService.getBuy(buyId, request);
        assertThat(fetchedBuy.getProduct()).isNotNull();
        assertThat(fetchedBuy.getProduct().getCategory().getUser().getId()).isEqualTo(userId);
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void updateBuyWithNullUpdatableValues() {
        var buyRecord = BuyModel.builder()
                .product(new ProductModel(productId))
                .price(null)
                .count(null)
                .build();
        assertThrows(BadRequestException.class, () -> {
            buyService.updateBuy(Optional.of(buyRecord), buyId, request);
            var fetchedBuy = buyService.getBuy(buyId, request);
            assertThat(fetchedBuy.getCount()).isEqualTo(BigDecimal.valueOf(200000, 4));
            assertThat(fetchedBuy.getPrice()).isEqualTo(BigDecimal.valueOf(500000, 4));
        });
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void updateBuy() {
        var buyRecord = BuyModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(60.505))
                .count(BigDecimal.valueOf(26.502))
                .build();
        buyService.updateBuy(Optional.of(buyRecord), buyId, request);
        var fetchedBuy = buyService.getBuy(buyId, request);
        assertThat(fetchedBuy.getCount()).isEqualTo(BigDecimal.valueOf(265020, 4));
        assertThat(fetchedBuy.getPrice()).isEqualTo(BigDecimal.valueOf(605050, 4));
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void getAllBuyRecordsOfProduct() {
        var fetchedRecords = buyService.getAllBuyRecordsOfProduct(productId, request, pageable);
        assertThat(fetchedRecords.getContent().get(0).getId()).isNotEqualTo(buyId);
        assertThat(fetchedRecords.getContent().get(0).getPrice()).isNotEqualTo(BigDecimal.valueOf(605050, 4));
        assertThat(fetchedRecords.getContent().get(1).getId()).isEqualTo(buyId);
        assertThat(fetchedRecords.getContent().get(1).getPrice()).isEqualTo(BigDecimal.valueOf(605050, 4));
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void getAllBuyRecordsOfUser() {
        var fetchedRecords = buyService.getAllBuyRecordsOfUser(userId, request, pageable);
        assertThat(fetchedRecords.getContent().get(0).getId()).isNotEqualTo(buyId);
        assertThat(fetchedRecords.getContent().get(0).getPrice()).isNotEqualTo(BigDecimal.valueOf(605050, 4));
        assertThat(fetchedRecords.getContent().get(1).getId()).isEqualTo(buyId);
        assertThat(fetchedRecords.getContent().get(1).getPrice()).isEqualTo(BigDecimal.valueOf(605050, 4));
    }

    @Test
    @Order(9)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void deleteBuy() {
        buyService.deleteBuy(buyId, request);
        assertThrows(NoContentException.class, () -> buyService.getBuy(buyId, request));
        var product = productService.getProduct(productId, request);
        assertThat(product.getId()).isEqualTo(productId);
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


        HttpServletRequest request = mock(HttpServletRequest.class);
        for (String key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

}
