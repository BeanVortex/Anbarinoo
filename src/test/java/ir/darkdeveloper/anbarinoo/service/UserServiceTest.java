package ir.darkdeveloper.anbarinoo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public record UserServiceTest(UserService service,
                              JwtUtils jwtUtils,
                              PasswordEncoder encoder) {

    private static HttpServletRequest request;
    private static UserModel user;


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
        user = new UserModel();
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
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
    void signUp() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        service.signUpUser(user, response);
        request = setUpHeader();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateUserWithDeleteImagesAndNullEmail() {
        user.setDescription("dex");
        user.setShopName("shop1");
        user.setEmail(null);
        user.setProfileFile(null);
        user.setShopFile(null);
        user.setShopImage(null);
        user.setProfileImage(null);
        service.updateUser(user, request);
        var fetchedUser = service.getUserInfo(user.getId(), request);
        assertThat(fetchedUser.getProfileImage()).isNull();
        assertThat(fetchedUser.getShopImage()).isNull();
    }


    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com")
    void updateUserWithNewImages() {
        user.setDescription("dex");
        user.setShopName("shop1");
        MockMultipartFile file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        user.setProfileFile(file1);
        user.setShopFile(file2);
        service.updateUser(user, request);
        var fetchedUser = service.getUserInfo(user.getId(), request);
        assertThat(encoder.matches(user.getPassword(), fetchedUser.getPassword())).isTrue();
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateUserWithKeepImagesAndNullPasswords() {
        assertThrows(Exception.class, () -> {
        });
        user.setPassword(null);
        user.setPasswordRepeat(null);
        user.setShopFile(null);
        user.setProfileFile(null);
        service.updateUser(user, request);
        var fetchedUser = service.getUserInfo(user.getId(), request);
        assertThat(fetchedUser.getPassword()).isNotNull();
    }

    @Test
    @Order(5)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateUserWithKeepImagesAndNewPasswords() {
        user.setPrevPassword("pass1234");
        user.setPassword("pass4321");
        user.setPasswordRepeat("pass4321");
        user.setShopFile(null);
        user.setProfileFile(null);
        service.updateUser(user, request);
        var fetchedUser = service.getUserInfo(user.getId(), request);
        assertThat(encoder.matches("pass4321", fetchedUser.getPassword())).isTrue();
    }

    @Test
    @Order(6)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void updateUserWithKeepImagesAndWithNewEmail() {
        user.setEmail("email2@mail.com");
        user.setPassword(null);
        user.setPasswordRepeat(null);
        user.setShopFile(null);
        user.setProfileFile(null);
        service.updateUser(user, request);
        var fetchedUser = service.getUserInfo(user.getId(), request);
        assertThat(fetchedUser.getEmail()).isEqualTo("email2@mail.com");
    }


    @Test
    @Order(7)
    @WithMockUser(username = "email2@mail.com", authorities = {"OP_ACCESS_USER"})
    void getUserInfo() {
        UserModel model = service.getUserInfo(user.getId(), request);
        assertThat(model.getEmail()).isEqualTo("email2@mail.com");
    }

    @Test
    @WithMockUser(username = "email2@mail.com")
    @Order(8)
    void deleteUser() {
        service.deleteUser(user.getId(), request);
    }

    @Test
    @Order(9)
    void verifyUserEmail() {
    }


    //should return the object; data is being removed
    private HttpServletRequest setUpHeader() {

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
