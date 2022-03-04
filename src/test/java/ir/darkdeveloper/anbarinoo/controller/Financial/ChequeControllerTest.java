package ir.darkdeveloper.anbarinoo.controller.Financial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.config.StartupConfig;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/cheque")
@DirtiesContext
public record ChequeControllerTest(JwtUtils jwtUtils,
                                   UserService userService,
                                   RestDocumentationContextProvider restDocumentation,
                                   WebApplicationContext webApplicationContext) {


    private static Long userId;
    private static HttpServletRequest request;
    private static Long chequeId;
    private static String refresh;
    private static String access;
    private static MockMvc mockMvc;

    @Autowired
    public ChequeControllerTest {
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
        userId = user.getId();
        request = setUpHeader(user.getEmail(), userId);
        refresh = request.getHeader("refresh_token");
        access = request.getHeader("access_token");
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveCheque() throws Exception {
        var cheque = ChequeModel.builder()
                .amount(BigDecimal.valueOf(750.06))
                .nameOf("Me")
                .payTo("Other")
                .issuedAt(LocalDateTime.now())
                .validTill(LocalDateTime.now().plusDays(5))
//                .isCheckedOut(false)
//                .isDebt(false)
                .build();

        mockMvc.perform(post("/api/user/financial/cheque/save/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                        .content(mapToJson(cheque))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isCheckedOut").value(false))
                .andExpect(jsonPath("$.isDebt").value(false))
                .andDo(result -> {
                    var jsonObject = new JSONObject(result.getResponse().getContentAsString());
                    chequeId = jsonObject.getLong("id");
                })
        ;
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void updateCheque() throws Exception {

        var cheque = new ChequeModel();
        cheque.setIsCheckedOut(true);
        cheque.setNameOf("Me2");
        cheque.setValidTill(LocalDateTime.now().plusDays(10));

        mockMvc.perform(post("/api/user/financial/cheque/update/{id}/", chequeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                        .content(mapToJson(cheque))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(is(750.06)))
                .andExpect(jsonPath("$.isCheckedOut").value(is(true)))
                .andExpect(jsonPath("$.nameOf").value(is("Me2")))
        ;


    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getChequeAndDod() throws Exception {
        var cheque = new AtomicReference<ChequeModel>();
        mockMvc.perform(get("/api/user/financial/cheque/{id}/", chequeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(is(750.06)))
                .andExpect(jsonPath("$.isCheckedOut").value(is(true)))
                .andExpect(jsonPath("$.nameOf").value(is("Me2")))
                .andDo(result -> {
                    var om = new ObjectMapper();
                    var m = om.readValue(result.getResponse().getContentAsString(), ChequeModel.class);
                    cheque.set(m);
                })
        ;

        mockMvc.perform(get("/api/user/financial/debt-demand/get-by-user/{id}/", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(is(cheque.get().getAmount()), BigDecimal.class))
                .andExpect(jsonPath("$.content[0].isDebt").value(is(cheque.get().getIsDebt())))
                .andExpect(jsonPath("$.content[0].isCheckedOut").value(is(cheque.get().getIsCheckedOut())))
                .andExpect(jsonPath("$.content[0].nameOf").value(is(cheque.get().getNameOf())))
                .andExpect(jsonPath("$.content[0].payTo").value(is(cheque.get().getPayTo())))
                .andExpect(jsonPath("$.content[0].userId").value(is(cheque.get().getUser().getId()), Long.class))
                .andExpect(jsonPath("$.content[0].issuedAt").value(cheque.get().getIssuedAt()
                        .format(StartupConfig.DATE_FORMATTER)))
                .andExpect(jsonPath("$.content[0].validTill").value(cheque.get().getValidTill()
                        .format(StartupConfig.DATE_FORMATTER)))
        ;

    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getChequesByUserId() throws Exception {

        mockMvc.perform(get("/api/user/financial/cheque/user/{id}/", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheques[0].amount").value(is(750.06)))
                .andExpect(jsonPath("$.cheques[0].isCheckedOut").value(is(true)))
                .andExpect(jsonPath("$.cheques[0].nameOf").value(is("Me2")))
        ;

    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void findByPayToContains() throws Exception {
        var payTo = "th";
        mockMvc.perform(get("/api/user/financial/cheque/search/")
                        .param("payTo", payTo)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheques[0].amount").value(is(750.06)))
                .andExpect(jsonPath("$.cheques[0].isCheckedOut").value(is(true)))
                .andExpect(jsonPath("$.cheques[0].nameOf").value(is("Me2")))
        ;
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void deleteCheque() throws Exception {
        mockMvc.perform(delete("/api/user/financial/cheque/{id}/", chequeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                )
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getDODAfterChequeDelete() throws Exception {
        mockMvc.perform(get("/api/user/financial/debt-demand/get-by-user/{id}/", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("refresh_token", refresh)
                        .header("access_token", access)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
        ;
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