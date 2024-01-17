package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@AutoConfigureRestDocs(outputDir = "docs/debt_or_demand")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record DebtOrDemandControllerTest(JwtUtils jwtUtils,
                                         UserService userService,
                                         RestDocumentationContextProvider restDocumentation,
                                         WebApplicationContext webApplicationContext,
                                         TestUtils testUtils) {


    private static Long userId;
    private static Long dodId;
    private static HttpHeaders authHeaders;
    private static MockMvc mockMvc;

    @Autowired
    public DebtOrDemandControllerTest {
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
        var userEmail = user.getEmail();
        userId = user.getId();
        authHeaders = testUtils.getAuthHeaders(response);
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveDOD() throws Exception {
        var dod = DebtOrDemandModel.builder()
                .amount(BigDecimal.valueOf(115.56))
                .isDebt(true)
                .issuedAt(LocalDateTime.now())
                .validTill(LocalDateTime.now().plusDays(5))
                .nameOf("Me")
                .payTo("Other")
                .build();
        mockMvc.perform(post("/api/user/financial/debt-demand/save/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(dod))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(result -> {
                    var jsonObject = new JSONObject(result.getResponse().getContentAsString());
                    dodId = jsonObject.getLong("id");
                })
        ;
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateDOD() throws Exception {
        var dod = DebtOrDemandModel.builder()
                //should ignore id
                .id(25L)
                .amount(BigDecimal.valueOf(1564))
                .isDebt(false)
                .validTill(LocalDateTime.now().plusDays(8))
                .nameOf("Other")
                .payTo("Me")
                .build();
        mockMvc.perform(put("/api/user/financial/debt-demand/update/{id}/", dodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                        .content(mapToJson(dod))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameOf").value(is("Other")))
                .andExpect(jsonPath("$.payTo").value(is("Me")))
                .andExpect(jsonPath("$.id").value(is(dodId), Long.class))
                .andExpect(jsonPath("$.amount").value(is(BigDecimal.valueOf(1564)), BigDecimal.class))
                .andExpect(jsonPath("$.isDebt").value(is(false)))
        ;
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllDODRecordsOfUser() throws Exception {

        mockMvc.perform(get("/api/user/financial/debt-demand/user/{id}/", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(is(dodId), Long.class))
                .andExpect(jsonPath("$.content[0].payTo").value(is("Me")))
        ;

    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getDOD() throws Exception {
        mockMvc.perform(get("/api/user/financial/debt-demand/{id}/", dodId)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.nameOf").value(is("Other")))
        ;
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteDOD() throws Exception {
        mockMvc.perform(delete("/api/user/financial/debt-demand/{id}/", dodId)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(authHeaders)
                )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }


}