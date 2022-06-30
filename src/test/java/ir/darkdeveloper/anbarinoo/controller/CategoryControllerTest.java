package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
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

import java.util.List;
import java.util.Optional;

import static ir.darkdeveloper.anbarinoo.TestUtils.mapToJson;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "docs/category")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record CategoryControllerTest(UserService userService,
                                     CategoryController categoryController,
                                     JwtUtils jwtUtils,
                                     RestDocumentationContextProvider restDocumentation,
                                     WebApplicationContext webApplicationContext,
                                     TestUtils testUtils) {

    private static Long catId;
    private static HttpHeaders authHeaders;
    private static Long subCatId;
    private static MockMvc mockMvc;


    @Autowired
    public CategoryControllerTest {
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
                .build();
        userService.signUpUser(Optional.of(user), response);
        authHeaders = testUtils.getAuthHeaders(response);
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
                        .headers(authHeaders)
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
                        .headers(authHeaders)
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
                        .headers(authHeaders)
                )
                .andDo(print())
                .andDo(result -> {
                    var jsonObject = new JSONObject(result.getResponse().getContentAsString());
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
                        .headers(authHeaders)
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
                        .headers(authHeaders)
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
                        .headers(authHeaders)
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
                        .headers(authHeaders)
                )
                .andDo(print())
                .andExpect(status().isNoContent());
    }

}