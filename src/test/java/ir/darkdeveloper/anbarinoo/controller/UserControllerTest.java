package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record UserControllerTest(UserController controller,
                                 WebApplicationContext webApplicationContext,
                                 JwtUtils jwtUtils) {

    private static Long userId;
    private static MockMvc mockMvc;
    private static String signupRefreshToken;
    private static String signupAccessToken;

    @Autowired
    public UserControllerTest {
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
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void signUpUser() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("profileFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("shopFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        MockPart address = new MockPart("address", "address".getBytes());
        MockPart des = new MockPart("description", "desc".getBytes());
        MockPart username = new MockPart("userName", "user n".getBytes());
        MockPart password = new MockPart("password", "pass1".getBytes());
        MockPart passwordRepeat = new MockPart("passwordRepeat", "pass1".getBytes());
        MockPart email = new MockPart("email", "email@mail.com".getBytes());
        mockMvc.perform(multipart("/api/user/signup/")
                .file(file1)
                .file(file2)
                .part(email, des, username, address, passwordRepeat, password)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andDo(result -> {
                    signupRefreshToken = result.getResponse().getHeader("refresh_token");
                    signupAccessToken = result.getResponse().getHeader("access_token");
                    JSONObject obj = new JSONObject(result.getResponse().getContentAsString());
                    userId = obj.getLong("id");
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }


    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_EDIT_USER")
    void updateUser() throws Exception {
        MockPart address = new MockPart("address", "UpdatedAddress".getBytes());
        MockPart des = new MockPart("description", "UpdatedDesc".getBytes());
        MockPart username = new MockPart("userName", "UpdatedUser n".getBytes());
        MockPart id = new MockPart("id", null);

        mockMvc.perform(multipart("/api/user/update/{id}/", userId)
                .part(des, username, address, id)
                .header("refresh_token", signupRefreshToken)
                .header("access_token", signupAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(is("UpdatedAddress")))
                .andExpect(jsonPath("$.description").value(is("UpdatedDesc")))
                .andExpect(jsonPath("$.userName").value(is("UpdatedUser n")));
    }


}
