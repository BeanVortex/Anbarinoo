package ir.darkdeveloper.anbarinoo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
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
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/category")
@DirtiesContext
public record CategoryControllerTest(UserService userService,
                                     CategoryController categoryController,
                                     JwtUtils jwtUtils,
                                     RestDocumentationContextProvider restDocumentation,
                                     WebApplicationContext webApplicationContext) {

    private static Long catId;
    private static Long subCatId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;


    @Autowired
    public CategoryControllerTest {
    }

    @BeforeAll
    static void setUp() {
        var authentication = Mockito.mock(Authentication.class);
        var securityContext = Mockito.mock(SecurityContext.class);
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
    void saveUser()  {
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
        var userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveCategory() throws Exception {
        var electronics = new CategoryModel("Electronics");
        System.out.println(mapToJson(electronics));
        mockMvc.perform(post("/api/category/save/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(electronics))
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andDo(result -> {
                    JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
                    catId = jsonObject.getLong("id");
                })
                .andExpect(status().isCreated());
    }

    // should not save product
    @Test
    @Order(3)
    @WithMockUser(authorities = {"OP_ACCESS_USER"})
    void saveCategoryWithProduct() throws Exception {
        var electronics = new CategoryModel("Electronics");
        electronics.setProducts(List.of(new ProductModel(5L)));
        System.out.println(mapToJson(electronics));
        mockMvc.perform(post("/api/category/save/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(electronics))
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andExpect(jsonPath("$.products").isEmpty())
                .andExpect(status().isCreated());
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveASubCategory() throws Exception {
        var subCat = new CategoryModel("Mobiles");
        System.out.println(mapToJson(subCat));
        mockMvc.perform(post("/api/category/sub-category/save/{parentId}/", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(subCat))
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andDo(result -> {
                    JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
                    subCatId = jsonObject.getLong("id");
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(is(catId), Long.class));
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getParentCategoryById() throws Exception {
        mockMvc.perform(get("/api/category/{id}/", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0]").value(is(subCatId), Long.class));
    }


    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getCategoriesByUser() throws Exception {
        mockMvc.perform(get("/api/category/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(3)))
                .andExpect(jsonPath("$.categories[2].name").value(is("Mobiles")));
    }


    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteCategoryById() throws Exception {
        mockMvc.perform(delete("/api/category/{id}/", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getSubCategoryByIdAfterParentDelete() throws Exception {
        mockMvc.perform(get("/api/category/{id}/", subCatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", request.getHeader("refresh_token"))
                        .header("access_token", request.getHeader("access_token"))
                )
                .andDo(print())
                .andExpect(status().isNoContent());
    }


    private String mapToJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
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