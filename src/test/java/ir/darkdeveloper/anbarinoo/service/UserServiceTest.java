package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.DataExistsException;
import ir.darkdeveloper.anbarinoo.model.Auth.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
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
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
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
        var authentication = Mockito.mock(Authentication.class);
        var securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        request = mock(HttpServletRequest.class);
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
//    @Disabled
    void signUpWithoutImage() throws Exception {
        var response = mock(HttpServletResponse.class);
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .userName("user n")
                .enabled(true)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
        service.signUpUser(user, response);
        request = setUpHeader(user);
        userId = user.getId();
    }

    @Test
    @Order(1)
//    @WithMockUser(username = "anonymousUser")
    void signUpWithImage() {
        var response = mock(HttpServletResponse.class);

        var file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!" .getBytes());
        var file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!" .getBytes());

        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .userName("user n")
                .enabled(true)
                .profileFile(file1)
                .shopFile(file2)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
        assertThrows(DataExistsException.class, () -> {
            service.signUpUser(user, response);
            request = setUpHeader(user);
            userId = user.getId();
        });
    }

    @Test
    @Order(2)
//    @WithMockUser(username = "email@mail.com", authorities = {"OP_EDIT_USER", "OP_ACCESS_USER"})
    void updateUserWithKeepImagesAndNullPasswords() {
        var fetchedUser = service.getUserInfo(userId, request);
        var user = new UserModel();
        user.setAddress("DFDF");
        service.updateUser(Optional.of(user), userId, request);
        var fetchedUser2 = service.getUserInfo(userId, request);
        assertThat(fetchedUser2.getPassword()).isNotNull();
        assertThat(fetchedUser2.getShopImage()).isEqualTo(fetchedUser.getShopImage());
        assertThat(fetchedUser2.getProfileImage()).isEqualTo(fetchedUser.getProfileImage());
        assertThat(fetchedUser2.getProvider()).isEqualTo(fetchedUser.getProvider());
    }

    @Test
    @Order(3)
    void updateUserWithKeepImagesAndNewPasswords() {
        var fetchedUser = service.getUserInfo(userId, request);
        var user = new UserModel();
        user.setPrevPassword("pass12B~");
        user.setPassword("pass4321B~");
        user.setPasswordRepeat("pass4321B~");
        service.updateUser(Optional.of(user), userId, request);
        var fetchedUser2 = service.getUserInfo(userId, request);
        assertThat(encoder.matches("pass4321B~", fetchedUser2.getPassword())).isTrue();
        assertThat(fetchedUser2.getShopImage()).isEqualTo(fetchedUser.getShopImage());
        assertThat(fetchedUser2.getProfileImage()).isEqualTo(fetchedUser.getProfileImage());
    }

    @Test
    @Order(4)
    void updateUserWithNewImages() {
        var user = new UserModel();
        user.setDescription("dex");
        user.setShopName("shop1");
        MockMultipartFile file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!" .getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!" .getBytes());
        user.setProfileFile(file1);
        user.setShopFile(file2);
        var fetchedUser = service.getUserInfo(userId, request);
        service.updateUserImages(Optional.of(user), userId, request);
        var fetchedUser2 = service.getUserInfo(userId, request);
        assertThat(fetchedUser.getShopImage()).isNotEqualTo(fetchedUser2.getShopImage());
        assertThat(fetchedUser.getProfileImage()).isNotEqualTo(fetchedUser2.getProfileImage());
        assertThat(fetchedUser.getDescription()).isNotEqualTo(user.getDescription());
    }

    @Test
    @Order(5)
    void updateDeleteUserImages() {
        var user = new UserModel();
        user.setDescription("dexfd");
        var fetchedUser = service.getUserInfo(userId, request);
        user.setProfileImage(fetchedUser.getProfileImage());
        user.setShopImage(fetchedUser.getShopImage());
        service.updateDeleteUserImages(Optional.of(user), userId, request);
        var fetchedUser2 = service.getUserInfo(userId, request);
        assertThat(fetchedUser.getShopImage()).isNotEqualTo(fetchedUser2.getShopImage());
        assertThat(fetchedUser.getProfileImage()).isNotEqualTo(fetchedUser2.getProfileImage());
        assertThat(fetchedUser2.getShopImage()).isEqualTo("noImage.png");
        assertThat(fetchedUser2.getProfileImage()).isEqualTo("noProfile.jpeg");
        assertThat(fetchedUser.getDescription()).isNotEqualTo(user.getDescription());
    }

    @Test
    @Order(6)
    void getUserInfo() {
        var model = service.getUserInfo(userId, request);
        assertThat(encoder.matches("pass4321B~", model.getPassword())).isTrue();
        assertThat(model.getEmail()).isEqualTo("email@mail.com");
        assertThat(model.getShopImage()).isEqualTo("noImage.png");
        assertThat(model.getProfileImage()).isEqualTo("noProfile.jpeg");
    }

    @Test
    @Order(7)
    void getCurrentUserInfo() {
        var model = service.getSimpleCurrentUserInfo(request);
        assertThat(model.getId()).isNotNull();
        assertThat(model.getUserName()).isEqualTo("user n");
        assertThat(model.getEmail()).isEqualTo("email@mail.com");
        assertThat(model.getShopImage()).isEqualTo("noImage.png");
        assertThat(model.getProfileImage()).isEqualTo("noProfile.jpeg");
        assertThat(model.getDescription()).isNotNull();
        assertThat(model.getShopName()).isNotNull();
        assertThat(model.getAddress()).isNotNull();
        assertThat(model.getEnabled()).isEqualTo(true);
        assertThat(model.getCreatedAt()).isNotNull();
        assertThat(model.getUpdatedAt()).isNotNull();
        assertThat(model.getProvider()).isEqualTo(AuthProvider.LOCAL);
        System.out.println(model);
    }


    @Test
    @Order(8)
    void deleteUser() {
        service.deleteUser(userId, request);
    }

    @Test
    @Order(9)
    void getRoles() {
        var roles = rolesService.getAllRoles();
        assertThat(roles.size()).isNotEqualTo(0);
    }

    @Test
    @Order(10)
    void verifyUserEmail() {
    }

    //should return the object; data is being removed
    private HttpServletRequest setUpHeader(UserModel user) {

        var headers = new HashMap<String,String>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        var refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());
        var accessToken = jwtUtils.generateAccessToken(user.getEmail());
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
