package ir.darkdeveloper.anbarinoo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import ir.darkdeveloper.anbarinoo.model.UserModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record UserServiceTest(UserService service,
                              JwtUtils jwtUtils,
                              PasswordEncoder encoder,
                              UserRolesService rolesService) {

    private static HttpServletRequest request;
    private static Long userId;


    @Autowired
    public UserServiceTest {
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    @Disabled
    void signUpWithoutImage() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        var user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setEnabled(true);
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        service.signUpUser(user, response);
        request = setUpHeader(user);
        userId = user.getId();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void signUpWithImage() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        var user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setEnabled(true);
        MockMultipartFile file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        user.setProfileFile(file1);
        user.setShopFile(file2);
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        service.signUpUser(user, response);
        request = setUpHeader(user);
        userId = user.getId();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_EDIT_USER", "OP_ACCESS_USER"})
    void updateUserWithKeepImagesAndNullPasswords() {
        var user = new UserModel();
        user.setAddress("DFDF");
        service.updateUser(user, userId, request);
        var fetchedUser = service.getUserInfo(userId, request);
        assertThat(fetchedUser.getPassword()).isNotNull();
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_EDIT_USER", "OP_ACCESS_USER"})
    void updateUserWithKeepImagesAndNewPasswords() {
        var user = new UserModel();
        user.setPrevPassword("pass1");
        user.setPassword("pass4321");
        user.setPasswordRepeat("pass4321");
        service.updateUser(user, userId, request);
        var fetchedUser = service.getUserInfo(userId, request);
        assertThat(encoder.matches("pass4321", fetchedUser.getPassword())).isTrue();
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_EDIT_USER", "OP_ACCESS_USER"})
    void updateUserWithNewImages() {
        var user = new UserModel();
        user.setDescription("dex");
        user.setShopName("shop1");
        MockMultipartFile file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        user.setProfileFile(file1);
        user.setShopFile(file2);
        var fetchedUser = service.getUserInfo(userId, request);
        service.updateUser(user, userId, request);
        var fetchedUser2 = service.getUserInfo(userId, request);
        assertThat(fetchedUser.getShopImage()).isNotEqualTo(fetchedUser2.getShopImage());
        assertThat(fetchedUser.getProfileImage()).isNotEqualTo(fetchedUser2.getProfileImage());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_EDIT_USER", "OP_ACCESS_USER"})
    void updateUserWithDefaultImages() {
        var user = new UserModel();
        user.setDescription("dex");
        user.setShopName("shop1");
        user.setShopImage("default");
        user.setProfileImage("default");
        var fetchedUser = service.getUserInfo(userId, request);
        service.updateUser(user, userId, request);
        var fetchedUser2 = service.getUserInfo(userId, request);
        assertThat(fetchedUser.getShopImage()).isNotEqualTo(fetchedUser2.getShopImage());
        assertThat(fetchedUser.getProfileImage()).isNotEqualTo(fetchedUser2.getProfileImage());
        assertThat(fetchedUser2.getShopImage()).isEqualTo("noImage.png");
        assertThat(fetchedUser2.getProfileImage()).isEqualTo("noProfile.jpeg");
    }

    @Test
    @Order(6)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getUserInfo() {
        var model = service.getUserInfo(userId, request);
        assertThat(encoder.matches("pass4321", model.getPassword())).isTrue();
        assertThat(model.getEmail()).isEqualTo("email@mail.com");
        assertThat(model.getShopImage()).isEqualTo("noImage.png");
        assertThat(model.getProfileImage()).isEqualTo("noProfile.jpeg");
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "OP_DELETE_USER")
    void deleteUser() {
        service.deleteUser(userId, request);
    }

    @Test
    @WithMockUser(authorities = "OP_ACCESS_ROLE")
    @Order(8)
    void getRoles() {
        var roles = rolesService.getAllRoles();
        assertThat(roles.size()).isNotEqualTo(0);
    }

    @Test
    @Order(8)
    void verifyUserEmail() {
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(UserModel user) {

        Map<String, String> headers = new HashMap<>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
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
