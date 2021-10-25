package ir.darkdeveloper.anbarinoo.controller.Financial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
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

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/debt_or_demand")
@DirtiesContext
public record DebtOrDemandControllerTest(JwtUtils jwtUtils,
                                         UserService userService,
                                         RestDocumentationContextProvider restDocumentation,
                                         WebApplicationContext webApplicationContext) {


    private static Long userId;
    private static Long dodId;
    private static String refresh;
    private static String access;
    private static MockMvc mockMvc;

    @Autowired
    public DebtOrDemandControllerTest {
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
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
        user.setPassword("pass12P+");
        user.setPasswordRepeat("pass12P+");
        user.setEnabled(true);
        userService.signUpUser(user, response);
        userId = user.getId();
        setUpHeader(user.getEmail(), userId);
    }


    @Test
    @Order(2)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveDOD() throws Exception {
        var dod = new DebtOrDemandModel();
        dod.setAmount(BigDecimal.valueOf(115.56));
        dod.setIsDebt(true);
        dod.setIssuedAt(LocalDateTime.now());
        dod.setValidTill(LocalDateTime.now().plusDays(5));
        dod.setNameOf("Me");
        dod.setPayTo("Other");
        mockMvc.perform(post("/api/user/financial/debt-demand/save/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("refresh_token", refresh)
                .header("access_token", access)
                .content(mapToJson(dod))
        )
                .andDo(print())
                .andExpect(status().isOk())
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
        var dod = new DebtOrDemandModel();
        dod.setAmount(BigDecimal.valueOf(1564));
        dod.setIsDebt(false);
        dod.setValidTill(LocalDateTime.now().plusDays(8));
        dod.setNameOf("Other");
        dod.setPayTo("Me");
        mockMvc.perform(put("/api/user/financial/debt-demand/update/{id}/", dodId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("refresh_token", refresh)
                .header("access_token", access)
                .content(mapToJson(dod))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameOf").value(is("Other")))
                .andExpect(jsonPath("$.payTo").value(is("Me")))
                .andExpect(jsonPath("$.amount").value(is(BigDecimal.valueOf(1564)), BigDecimal.class))
                .andExpect(jsonPath("$.isDebt").value(is(false)))
        ;
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getAllDODRecordsOfUser() throws Exception {

        mockMvc.perform(get("/api/user/financial/debt-demand/get-by-user/{id}/", userId)
                .accept(MediaType.APPLICATION_JSON)
                .header("refresh_token", refresh)
                .header("access_token", access)
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
                .header("refresh_token", refresh)
                .header("access_token", access)
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
                .header("refresh_token", refresh)
                .header("access_token", access)
        )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    private String mapToJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }

    //should return the object; data is being removed
    private void setUpHeader(String email, Long userId) {
        refresh = jwtUtils.generateRefreshToken(email, userId);
        access = jwtUtils.generateAccessToken(email);
    }
}