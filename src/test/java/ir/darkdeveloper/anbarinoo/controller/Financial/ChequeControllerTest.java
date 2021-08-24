package ir.darkdeveloper.anbarinoo.controller.Financial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.service.UserService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record ChequeControllerTest(JwtUtils jwtUtils,
                                   UserService userService,
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
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @BeforeEach
    void setUp2() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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
        refresh = request.getHeader("refresh_token");
        access = request.getHeader("access_token");
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void saveCheque() throws Exception {
        var cheque = new ChequeModel();
        cheque.setAmount(BigDecimal.valueOf(750.06));
        cheque.setIsCheckedOut(false);
        cheque.setNameOf("Me");
        cheque.setPayTo("Other");
        cheque.setIssuedAt(LocalDateTime.now());
        cheque.setValidTill(LocalDateTime.now().plusDays(5));
        cheque.setIsDebt(false);

        mockMvc.perform(post("/api/user/financial/cheque/save/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("refresh_token", refresh)
                .header("access_token", access)
                .content(mapToJson(cheque))
        )
                .andDo(print())
                .andExpect(status().isOk())
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
    void getCheque() throws Exception {
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
                .andExpect(jsonPath("$[0].amount").value(is(750.06)))
                .andExpect(jsonPath("$[0].isCheckedOut").value(is(true)))
                .andExpect(jsonPath("$[0].nameOf").value(is("Me2")))
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
                .andExpect(jsonPath("$[0].amount").value(is(750.06)))
                .andExpect(jsonPath("$[0].isCheckedOut").value(is(true)))
                .andExpect(jsonPath("$[0].nameOf").value(is("Me2")))
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
        var refreshDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = UserUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
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