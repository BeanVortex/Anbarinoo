package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.TestUtils;
import ir.darkdeveloper.anbarinoo.extentions.DatabaseSetup;
import ir.darkdeveloper.anbarinoo.model.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(DatabaseSetup.class)
public record UserServiceTest(UserService service,
                              JwtUtils jwtUtils,
                              PasswordEncoder encoder,
                              UserRolesService rolesService,
                              TestUtils testUtils) {

    private static HttpServletRequest request;
    private static Long userId;


    @Autowired
    public UserServiceTest {
    }

    @Test
    @Order(1)
    @WithMockUser(username = "anonymousUser")
//    @Disabled
    void signUpWithoutImage() {
        var response = new MockHttpServletResponse();
        var user = UserModel.builder()
                .email("email@mail.com")
                .address("address")
                .description("desc")
                .userName("user n")
                .enabled(true)
                .password("pass12B~")
                .passwordRepeat("pass12B~")
                .build();
        service.signUpUser(Optional.of(user), response);
        userId = user.getId();
        request = testUtils.setUpHeaderAndGetReqWithRes(response);
    }

    @Test
    @Order(1)
//    @WithMockUser(username = "anonymousUser")
    void signUpWithImage() {
        var response = new MockHttpServletResponse();

        var file1 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        var file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());

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
        assertThrows(DataIntegrityViolationException.class, () -> {
            service.signUpUser(Optional.of(user), response);
            userId = user.getId();
            request = testUtils.setUpHeaderAndGetReqWithRes(response);
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
                "Hello, World!".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "hello.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
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

}
