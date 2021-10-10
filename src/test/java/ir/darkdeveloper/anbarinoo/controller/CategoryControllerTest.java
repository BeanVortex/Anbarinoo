package ir.darkdeveloper.anbarinoo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
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
import java.util.Map;

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
    private static Long userId;
    private static HttpServletRequest request;
    private static MockMvc mockMvc;


    @Autowired
    public CategoryControllerTest {
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
    void saveCategory() throws Exception {
        CategoryModel electronics = new CategoryModel("Electronics");
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
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
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
                .andExpect(jsonPath("$.parent").value(is(catId), Long.class));
    }

    @Test
    @Order(4)
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
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getCategoriesByUserId() throws Exception {
        mockMvc.perform(get("/api/category/user/{userId}/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("refresh_token", request.getHeader("refresh_token"))
                .header("access_token", request.getHeader("access_token"))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].name").value(is("Mobiles")));
    }


    @Test
    @Order(6)
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
    @Order(7)
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