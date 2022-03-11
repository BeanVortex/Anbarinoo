package ir.darkdeveloper.anbarinoo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.Auth.Authority;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/user_roles")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record UserRolesControllerTest(UserService userService,
                                      WebApplicationContext webApplicationContext,
                                      RestDocumentationContextProvider restDocumentation,
                                      JwtUtils jwtUtils) {

    private static MockMvc mockMvc;
    private static Long roleId;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(document("{methodName}"))
                .build();
    }

    @Autowired
    public UserRolesControllerTest {
    }


    @Test
    @Order(1)
    @WithMockUser(authorities = "OP_ADD_ROLE")
    void saveRole() throws Exception {
        var role = UserRole.builder()
                .authorities(List.of(Authority.OP_ADD_USER, Authority.OP_DELETE_USER))
                .name("AdminOfUsers")
                .build();
        mockMvc.perform(post("/api/user/role/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(role))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("Role created"));
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = "OP_ACCESS_ROLE")
    void getAllRoles() throws Exception {
        mockMvc.perform(get("/api/user/role/all/")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.userRoles").isArray())
                .andExpect(jsonPath("$.userRoles", hasSize(2)))
                .andExpect(jsonPath("$.userRoles[0].name").value("AdminOfUsers"))
                .andDo(result -> {
                    var obj = new JSONObject(result.getResponse().getContentAsString());
                    var arr = obj.getJSONArray("userRoles");
                    roleId = arr.getJSONObject(0).getLong("id");
                });
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_DELETE_ROLE")
    void deleteRole() throws Exception {
        mockMvc.perform(delete("/api/user/role/{id}/", roleId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Role deleted"));
    }


    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_ROLE")
    void getAllRolesAfterOneDeletion() throws Exception {
        mockMvc.perform(get("/api/user/role/all/")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.userRoles").isArray())
                .andExpect(jsonPath("$.userRoles", hasSize(1)))
                .andExpect(jsonPath("$.userRoles[0].name").value("USER"));
    }


    private String mapToJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}