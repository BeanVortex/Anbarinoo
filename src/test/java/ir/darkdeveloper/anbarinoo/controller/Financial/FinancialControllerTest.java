package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import ir.darkdeveloper.anbarinoo.service.Financial.DebtOrDemandService;
import ir.darkdeveloper.anbarinoo.service.Financial.SellService;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static ir.darkdeveloper.anbarinoo.TestUtils.mapToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/financial")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record FinancialControllerTest(UserService userService,
                                      ProductService productService,
                                      JwtUtils jwtUtils,
                                      WebApplicationContext webApplicationContext,
                                      CategoryService categoryService,
                                      BuyService buyService,
                                      SellService sellService,
                                      RestDocumentationContextProvider restDocumentation,
                                      DebtOrDemandService dodService,
                                      TestUtils testUtils) {

    private static HttpHeaders authHeaders;
    private static Long productId;
    private static Long buyId;
    private static Long sellId;
    private static Long catId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;
    private static LocalDateTime fromDate = null;
    private static LocalDateTime toDate = null;

    @Autowired
    public FinancialControllerTest {
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
                .enabled(true)
                .build();
        userService.signUpUser(user, response);
        var userEmail = user.getEmail();
        var userId = user.getId();
        request = testUtils.setUpHeaderAndGetReq(userEmail, userId);
        authHeaders = testUtils.getAuthHeaders(userEmail, userId);
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
        var product = ProductModel.builder()
                .name("name")
                .description("description")
                .totalCount(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(500))
                .category(new CategoryModel(catId))
                .tax(9)
                .build();
        fromDate = LocalDateTime.now().minusHours(1);
        productService.saveProduct(Optional.of(product), request);
        productId = product.getId();
    }

    @RepeatedTest(5)
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveBuy() {
        var buy = BuyModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(5000))
                .count(BigDecimal.valueOf(8))
                .tax(9)
                .build();
        buyService.saveBuy(Optional.of(buy), false, request);
        assertThat(buy.getId()).isNotNull();
        buyId = buy.getId();
    }

    @RepeatedTest(5)
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveSell() {
        var sell = SellModel.builder()
                .product(new ProductModel(productId))
                .price(BigDecimal.valueOf(6000))
                .count(BigDecimal.valueOf(4))
                .tax(9)
                .build();
        sellService.saveSell(Optional.of(sell), request);
        sellId = sell.getId();
        assertThat(sell.getId()).isNotNull();
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void badProductUpdate() {
        var product = new ProductModel();
        product.setTotalCount(BigDecimal.valueOf(9850));
        product.setPrice(BigDecimal.valueOf(564));
        assertThrows(NoContentException.class,
                () -> productService.updateProduct(Optional.of(product), 13L, request));
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getProductsAfterSells() {
        var product = productService.getProduct(productId, request);
        assertThat(product.getTotalCount()).isEqualTo(BigDecimal.valueOf(700000, 4));
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getCosts() throws Exception {
        toDate = LocalDateTime.now().plusMinutes(1);
        var financial = new FinancialDto(fromDate, toDate);

        var cost1 = BigDecimal.valueOf(50).multiply(BigDecimal.valueOf(500));
        var cost2 = BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(8));
        var tax1 = cost1.multiply(BigDecimal.valueOf(9, 2));
        var tax2 = cost2.multiply(BigDecimal.valueOf(9, 2));
        var finalCost1 = cost1.add(tax1);
        var finalCost2 = cost2.add(tax2).multiply(BigDecimal.valueOf(5));
        var finalCost = finalCost1.add(finalCost2).setScale(1, RoundingMode.CEILING);

        mockMvc.perform(post("/api/user/financial/costs/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costs").value(is(finalCost), BigDecimal.class))
        ;
    }

    @Test
    @Order(9)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getIncomes() throws Exception {
        var financial = new FinancialDto(fromDate, toDate);
        var income = BigDecimal.valueOf(6000).multiply(BigDecimal.valueOf(4));

        var tax = income.multiply(BigDecimal.valueOf(9, 2));

        var finalIncome = income.subtract(tax).multiply(BigDecimal.valueOf(5))
                .setScale(1, RoundingMode.CEILING);

        mockMvc.perform(post("/api/user/financial/incomes/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomes").value(is(finalIncome), BigDecimal.class))
        ;
    }

    @Test
    @Order(10)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateABuyWithBiggerCountThanPrevious() {
        var buy = BuyModel.builder()
                .price(BigDecimal.valueOf(6000))
                .count(BigDecimal.valueOf(20))
                .product(new ProductModel(productId))
                .tax(9)
                .build();
        buyService.updateBuy(Optional.of(buy), buyId, request);
    }

    @Test
    @Order(11)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateASellWithBiggerCountThanPrevious() {
        var sell = SellModel.builder()
                .price(BigDecimal.valueOf(9000))
                .count(BigDecimal.valueOf(6))
                .product(new ProductModel(productId))
                .tax(9)
                .build();
        sellService.updateSell(Optional.of(sell), sellId, request);
    }

    @Test
    @Order(12)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateABuyWithLessCountThanPrevious() {
        var buy = BuyModel.builder()
                .price(BigDecimal.valueOf(6000))
                .count(BigDecimal.valueOf(2))
                .product(new ProductModel(productId))
                .tax(9)
                .build();
        buyService.updateBuy(Optional.of(buy), buyId, request);
    }

    @Test
    @Order(13)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateASellWithLessCountThanPrevious() {
        var sell = SellModel.builder()
                .price(BigDecimal.valueOf(9000))
                .count(BigDecimal.valueOf(3))
                .product(new ProductModel(productId))
                .tax(9)
                .build();
        sellService.updateSell(Optional.of(sell), sellId, request);
    }

    @Test
    @Order(14)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getProductsAfterSellAndBuyUpdate() {
        var product = productService.getProduct(productId, request);
        assertThat(product.getTotalCount()).isEqualTo(BigDecimal.valueOf(650000, 4));
    }

    @Test
    @Order(15)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getCostsAfterBuyAndSellUpdates() throws Exception {
        toDate = LocalDateTime.now().plusMinutes(1);
        var financial = new FinancialDto(fromDate, toDate);

        var cost1 = BigDecimal.valueOf(50).multiply(BigDecimal.valueOf(500));
        var cost2 = BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(8));
        var cost3 = BigDecimal.valueOf(6000).multiply(BigDecimal.valueOf(2));
        var tax1 = cost1.multiply(BigDecimal.valueOf(9, 2));
        var tax2 = cost2.multiply(BigDecimal.valueOf(9, 2));
        var tax3 = cost3.multiply(BigDecimal.valueOf(9, 2));
        var finalCost1 = cost1.add(tax1);
        var finalCost2 = cost2.add(tax2).multiply(BigDecimal.valueOf(4));
        var finalCost3 = cost3.add(tax3);
        var finalCost = finalCost1.add(finalCost2).add(finalCost3)
                .setScale(1, RoundingMode.CEILING);

        mockMvc.perform(post("/api/user/financial/costs/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costs").value(is(finalCost), BigDecimal.class))
        ;
    }

    @Test
    @Order(16)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getIncomesAfterBuyAndSellUpdates() throws Exception {
        var financial = new FinancialDto(fromDate, toDate);

        var income1 = BigDecimal.valueOf(6000).multiply(BigDecimal.valueOf(4));
        var income2 = BigDecimal.valueOf(9000).multiply(BigDecimal.valueOf(3));

        var tax1 = income1.multiply(BigDecimal.valueOf(9, 2));
        var tax2 = income2.multiply(BigDecimal.valueOf(9, 2));
        var finalIncome1 = income1.subtract(tax1).multiply(BigDecimal.valueOf(4));
        var finalIncome2 = income2.subtract(tax2);
        var finalIncome = finalIncome1.add(finalIncome2).setScale(1, RoundingMode.CEILING);

        mockMvc.perform(post("/api/user/financial/incomes/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomes").value(is(finalIncome), BigDecimal.class))
        ;
    }


    @Test
    @Order(17)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getProfitAndLoss() throws Exception {
        var financial = new FinancialDto(fromDate, toDate);


        var income1 = BigDecimal.valueOf(6000).multiply(BigDecimal.valueOf(4));
        var income2 = BigDecimal.valueOf(9000).multiply(BigDecimal.valueOf(3));

        var tax1 = income1.multiply(BigDecimal.valueOf(9, 2));
        var tax2 = income2.multiply(BigDecimal.valueOf(9, 2));
        var finalIncome1 = income1.subtract(tax1).multiply(BigDecimal.valueOf(4));
        var finalIncome2 = income2.subtract(tax2);
        var finalIncome = finalIncome1.add(finalIncome2).setScale(1, RoundingMode.CEILING);

        var cost1 = BigDecimal.valueOf(50).multiply(BigDecimal.valueOf(500));
        var cost2 = BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(8));
        var cost3 = BigDecimal.valueOf(6000).multiply(BigDecimal.valueOf(2));
        var tax1c = cost1.multiply(BigDecimal.valueOf(9, 2));
        var tax2c = cost2.multiply(BigDecimal.valueOf(9, 2));
        var tax3c = cost3.multiply(BigDecimal.valueOf(9, 2));
        var finalCost1 = cost1.add(tax1c);
        var finalCost2 = cost2.add(tax2c).multiply(BigDecimal.valueOf(4));
        var finalCost3 = cost3.add(tax3c);
        var finalCost = finalCost1.add(finalCost2).add(finalCost3)
                .setScale(1, RoundingMode.CEILING);

        var profitOrLoss = finalIncome.multiply(BigDecimal.valueOf(100)).divide(finalCost, 2, RoundingMode.CEILING);

        var finalProfitOrLoss = (BigDecimal) null;

        if (profitOrLoss.compareTo(BigDecimal.valueOf(100)) > 0)
            finalProfitOrLoss = profitOrLoss.subtract(BigDecimal.valueOf(100));
        else
            finalProfitOrLoss = BigDecimal.valueOf(100).subtract(profitOrLoss);


        var finalProfitOrLoss1 = finalProfitOrLoss;
        mockMvc.perform(post("/api/user/financial/profit-loss/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(financial))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var jObject = new JSONObject(result.getResponse().getContentAsString());
                    var fetchedProfitOrLoss = (BigDecimal) null;
                    if (jObject.get("loss") != null)
                        fetchedProfitOrLoss = BigDecimal.valueOf(jObject.getDouble("loss"))
                                .setScale(2, RoundingMode.HALF_DOWN);

                    assertThat(fetchedProfitOrLoss).isEqualTo(finalProfitOrLoss1);
                })
        ;
    }

}