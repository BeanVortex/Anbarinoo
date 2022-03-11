package ir.darkdeveloper.anbarinoo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.dto.LoginDto;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Part;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureRestDocs(outputDir = "rest_apis_docs/user")
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record UserControllerTest(UserController controller,
                                 WebApplicationContext webApplicationContext,
                                 RestDocumentationContextProvider restDocumentation,
                                 JwtUtils jwtUtils) {

    private static Long userId;
    private static MockMvc mockMvc;
    private static String signupRefreshToken;
    private static String signupAccessToken;
    private static String profileImage;
    private static String shopImage;

    @Autowired
    public UserControllerTest {
    }


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(document("{methodName}"))
                .build();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void signUpUser() throws Exception {
        var address = new MockPart("address", "address".getBytes());
        var des = new MockPart("description", "desc".getBytes());
        var username = new MockPart("userName", "user n".getBytes());
        var password = new MockPart("password", "Pass!12".getBytes());
        var passwordRepeat = new MockPart("passwordRepeat", "Pass!12".getBytes());
        var email = new MockPart("email", "email@mail.com".getBytes());
        var parts = new Part[]{email, des, username, address, password, passwordRepeat};
        var file1 = new MockMultipartFile("profileFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file2 = new MockMultipartFile("shopFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

        mockMvc.perform(multipart("/api/user/signup/")
                        .part(parts)
                        .file(file1)
                        .file(file2)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andDo(result -> {
                    signupRefreshToken = result.getResponse().getHeader("refresh_token");
                    signupAccessToken = result.getResponse().getHeader("access_token");
                    var obj = new JSONObject(result.getResponse().getContentAsString());
                    userId = obj.getLong("id");
                    profileImage = obj.getString("profileImage");
                    shopImage = obj.getString("shopImage");

                })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").isNotEmpty())
        ;
    }


    @Test
    @Order(2)
    void loginUser() throws Exception {
        var loginDto = new LoginDto("user n", "Pass!12");
        System.out.println(mapToJson(loginDto));
        mockMvc.perform(post("/api/user/login/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapToJson(loginDto)))
                .andDo(print())
                .andDo(result -> {
                    signupRefreshToken = result.getResponse().getHeader("refresh_token");
                    signupAccessToken = result.getResponse().getHeader("access_token");
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }


    @Test
    @Order(3)
    @WithMockUser(authorities = "OP_EDIT_USER")
    void updateUser() throws Exception {
        var address = new MockPart("address", "UpdatedAddress".getBytes());
        var des = new MockPart("description", "UpdatedDesc".getBytes());
        var username = new MockPart("userName", "UpdatedUser n".getBytes());
        var id = new MockPart("id", null);

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


    @Test
    @Order(4)
    @WithMockUser(authorities = "OP_EDIT_USER")
    void updateUserImagesNotAcceptable() throws Exception {
        var file1 = new MockMultipartFile("profileFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file2 = new MockMultipartFile("shopFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

        var id = new MockPart("id", null);

        mockMvc.perform(multipart("/api/user/update/images//")
                        .part(id)
                        .file(file1)
                        .file(file2)
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "OP_EDIT_USER")
    void updateUserImages() throws Exception {
        var file1 = new MockMultipartFile("profileFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file2 = new MockMultipartFile("shopFile", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

        var id = new MockPart("id", null);

        mockMvc.perform(multipart("/api/user/update/images/{id}/", userId)
                        .part(id)
                        .file(file1)
                        .file(file2)
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(result -> {
                    var obj = new JSONObject(result.getResponse().getContentAsString());
                    var newProfile = obj.getString("profileImage");
                    var newShop = obj.getString("shopImage");
                    assertThat(profileImage).isNotEqualTo(newProfile);
                    assertThat(shopImage).isNotEqualTo(newShop);
                    profileImage = newProfile;
                    shopImage = newShop;
                });
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "OP_EDIT_USER")
    void updateDeleteUserImages() throws Exception {

        var sh = new MockPart("shopImage", shopImage.getBytes());
        var pr = new MockPart("profileImage", profileImage.getBytes());
        var id = new MockPart("id", null);

        mockMvc.perform(multipart("/api/user/update/delete-images/{id}/", userId)
                        .part(sh, pr, id)
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.shopImage").value(is("noImage.png")))
                .andExpect(jsonPath("$.profileImage").value(is("noProfile.jpeg")))
                .andDo(result -> {
                    JSONObject obj = new JSONObject(result.getResponse().getContentAsString());
                    var newProfile = obj.getString("profileImage");
                    var newShop = obj.getString("shopImage");
                    assertThat(profileImage).isNotEqualTo(newProfile);
                    assertThat(shopImage).isNotEqualTo(newShop);
                    profileImage = newProfile;
                    shopImage = newShop;
                });
    }


    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getUserInfo() throws Exception {

        mockMvc.perform(get("/api/user/{id}/", userId)
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.shopImage").value(is("noImage.png")))
                .andExpect(jsonPath("$.profileImage").value(is("noProfile.jpeg")))
                .andExpect(jsonPath("$.id").value(is(userId), Long.class));
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "OP_ACCESS_USER")
    void getSimpleCurrentUserInfo() throws Exception {

        mockMvc.perform(get("/api/user/info/", userId)
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.shopImage").value(is("noImage.png")))
                .andExpect(jsonPath("$.profileImage").value(is("noProfile.jpeg")))
                .andExpect(jsonPath("$.id").value(is(userId), Long.class));
    }

    @Test
    @Order(9)
    @WithMockUser(authorities = "OP_ACCESS_ADMIN")
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/api/user/all/")
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].shopImage").value(is("noImage.png")))
                .andExpect(jsonPath("$.content[0].profileImage").value(is("noProfile.jpeg")))
                .andExpect(jsonPath("$.content[0].id").value(is(userId), Long.class));
    }

    @Test
    @Order(10)
    @WithMockUser(authorities = "OP_DELETE_USER")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/api/user/{id}/", userId)
                        .header("refresh_token", signupRefreshToken)
                        .header("access_token", signupAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(is("Deleted the user")))
                .andDo(print());
    }


    private String mapToJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

}
