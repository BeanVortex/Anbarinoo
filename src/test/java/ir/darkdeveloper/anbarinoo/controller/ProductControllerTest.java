package ir.darkdeveloper.anbarinoo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/product")
@DirtiesContext
public record ProductControllerTest(WebApplicationContext webApplicationContext,
                                    CategoryService categoryService,
                                    UserService userService,
                                    RestDocumentationContextProvider restDocumentation,
                                    JwtUtils jwtUtils) {


    private static Long userId;
    private static String user1Refresh;
    private static String user1Access;
    private static String user2Refresh;
    private static String user2Access;
    private static Long productId;
    private static Long catId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;

    @Autowired
    public ProductControllerTest {
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
        userService.signUpUser(user, response);
        userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
        user1Refresh = request.getHeader("refresh_token");
        user1Access = request.getHeader("access_token");
        System.out.println(jwtUtils.getUserId(user1Refresh));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "anonymousUser")
    void saveUser2() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        var user = UserModel.builder()
                .email("email2@mail.com")
                .address("address")
                .description("desc")
                .userName("user n2")
                .password("pass12P+")
                .passwordRepeat("pass12P+")
                .build();
        userService.signUpUser(user, response);
        var userId2 = user.getId();
        request = setUpHeader(user.getEmail(), userId2);
        user2Refresh = request.getHeader("refresh_token");
        user2Access = request.getHeader("access_token");
        System.out.println(jwtUtils.getUserId(user2Refresh));
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveCategory() {
        var electronics = new CategoryModel("Electronics");
        request = setUpHeader("email@mail.com", userId);
        categoryService.saveCategory(electronics, request);
        catId = electronics.getId();
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveProductWithImages() throws Exception {

        var category = new MockPart("category", catId.toString().getBytes());
        var price = new MockPart("price", "10".getBytes());
        var tax = new MockPart("tax", "9".getBytes());
        var name = new MockPart("name", "product1".getBytes());
        var description = new MockPart("description", "desc1".getBytes());
        var totalCount = new MockPart("totalCount", "500".getBytes());

        var file1 = new MockMultipartFile("files", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file2 = new MockMultipartFile("files", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

        mockMvc.perform(multipart("/api/category/products/save/")
                        .part(name, price, tax, description, category, totalCount)
                        .file(file1)
                        .file(file2)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.categoryId").value(is(catId), Long.class))
                .andExpect(jsonPath("$.images", hasSize(2)))
                .andDo(result -> {
                    JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
                    productId = jsonObject.getLong("id");
                });

    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveProductWithoutImages() throws Exception {
        var category = new MockPart("category", catId.toString().getBytes());
        var price = new MockPart("price", "10".getBytes());
        var tax = new MockPart("tax", "9".getBytes());
        var name = new MockPart("name", "product2".getBytes());
        var description = new MockPart("description", "desc2".getBytes());
        var totalCount = new MockPart("totalCount", "500".getBytes());
        mockMvc.perform(multipart("/api/category/products/save/")
                        .part(name, price, tax, description, category, totalCount)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(is(catId), Long.class));
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void findByNameContainsWithAnotherUser() throws Exception {
        var name = "";
        mockMvc.perform(get("/api/category/products/search/")
                        .param("name", name)
                        .header("refresh_token", user2Refresh)
                        .header("access_token", user2Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void findByNameContains() throws Exception {
        var name = "";
        mockMvc.perform(get("/api/category/products/search/")
                        .param("name", name)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(is(productId), Long.class));
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateProduct() throws Exception {

        var product = new ProductModel();
        product.setPrice(BigDecimal.valueOf(50.05));
        product.setName("product1Updated");
        product.setDescription("desc1Updated");
        product.setTotalCount(BigDecimal.valueOf(50500));


        mockMvc.perform(put("/api/category/products/update/{id}/", productId)
                        .content(mapToJson(product))
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(is(catId), Long.class))
                .andExpect(jsonPath("$.price").value(is(50.05)))
                .andExpect(jsonPath("$.totalCount").value(is(50500), Integer.class))
                .andExpect(jsonPath("$.name").value(is("product1Updated")))
                .andExpect(jsonPath("$.description").value(is("desc1Updated")));

    }

    @Test
    @Order(9)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateProduct2() throws Exception {

        var product = new ProductModel();
        product.setPrice(BigDecimal.valueOf(16.5));
        product.setName("product1Updated");
        product.setDescription("desc1Updated");
        product.setTotalCount(BigDecimal.valueOf(6953546));


        mockMvc.perform(put("/api/category/products/update/{id}/", productId)
                        .content(mapToJson(product))
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @Order(10)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateProduct3() throws Exception {
        var product = new ProductModel();
        product.setName("product1Updated2");
        product.setDescription("desc1Updated2");

        mockMvc.perform(put("/api/category/products/update/{id}/", productId)
                        .content(mapToJson(product))
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(is("product1Updated2")))
                .andExpect(jsonPath("$.description").value(is("desc1Updated2")))
        ;

    }


    @Test
    @Order(11)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateProductImages() throws Exception {

        // these should be ignored to save
        var id = new MockPart("id", null);
        var price = new MockPart("price", "65.56".getBytes());
        var name = new MockPart("name", "ignoredNameUpdate".getBytes());
        var description = new MockPart("description", "ignoredDescriptionUpdate".getBytes());
        var totalCount = new MockPart("totalCount", "854684".getBytes());

        // only pictures will update

        var file1 = new MockMultipartFile("files", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file2 = new MockMultipartFile("files", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file3 = new MockMultipartFile("files", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

        // limit of 5 pics for a product

        mockMvc.perform(multipart("/api/category/products/update/images/{id}/", productId)
                        .part(id, name, price, description, totalCount)
                        .file(file1).file(file2).file(file3)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        })
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(is(catId), Long.class))
                .andExpect(jsonPath("$.price").value(is(50.05)))
                .andExpect(jsonPath("$.totalCount").value(is(50500), Integer.class))
                .andExpect(jsonPath("$.name").value(is("product1Updated2")))
                .andExpect(jsonPath("$.description").value(is("desc1Updated2")));

    }

    @Test
    @Order(12)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateDelete2ProductImages() throws Exception {
        var product = new ProductModel();
        List<String> images = new ArrayList<>();

        //get product
        mockMvc.perform(get("/api/category/products/{id}/", productId)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    var jsonObject = new JSONObject(result.getResponse().getContentAsString());
                    images.add(jsonObject.getJSONArray("images").getString(0));
                    images.add(jsonObject.getJSONArray("images").getString(1));
                });


        // delete

        product.setImages(images);

        mockMvc.perform(put("/api/category/products/update/delete-images/{id}/", productId)
                        .content(mapToJson(product))
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @Order(13)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getProduct() throws Exception {

        mockMvc.perform(get("/api/category/products/{id}/", productId)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images", hasSize(3)));


    }

    @Test
    @Order(14)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteProductWithNonMatchedUserIdAndRefreshToken() throws Exception {
        mockMvc.perform(delete("/api/category/products/{id}/", productId)
                        .header("refresh_token", user2Refresh)
                        .header("access_token", user2Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(15)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteProduct() throws Exception {
        mockMvc.perform(delete("/api/category/products/{id}/", productId)
                        .header("refresh_token", user1Refresh)
                        .header("access_token", user1Access)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    private String mapToJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(String email, Long userId) {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        String refreshToken = jwtUtils.generateRefreshToken(email, userId);
        String accessToken = jwtUtils.generateAccessToken(email);
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