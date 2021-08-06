package ir.darkdeveloper.anbarinoo.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import ir.darkdeveloper.anbarinoo.model.UserModel;

@SpringBootTest
public class UserServiceTest {

    private final UserService service;
    private UserModel user;

    @Autowired
    public UserServiceTest(UserService service) {
        this.service = service;
    }

    @BeforeAll
    static void setUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    void userSetup() {
        user = new UserModel();
        user.setEmail("email@mail.com");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
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
    void signUp() throws  Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        service.signUpUser(user, response);
    }

    @Test
    @Order(2)
    @WithMockUser(username = "email@mail.com")
    void updateUserWithNullImages() {
        user.setDescription("dex");
        user.setShopName("shop1");
        user.setProfileFile(null);
        user.setShopFile(null);
        user.setId(((UserModel) service.loadUserByUsername(user.getEmail())).getId());
        service.updateUser(user);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "email@mail.com")
    void updateUserWithImages() {
        user.setDescription("dex");
        user.setShopName("shop1");
        user.setId(((UserModel) service.loadUserByUsername(user.getEmail())).getId());
        service.updateUser(user);
    }

    @Test
    @WithMockUser(username = "email@mail.com")
    @Order(5)
    //@Disabled
    void deleteUser() {
        service.deleteUser(user);
    }

    @Test
    @Order(4)
    @WithMockUser(username = "email@mail.com", authorities = {"OP_ACCESS_USER"})
    void getUserInfo(){
        UserModel model = service.getUserInfo(user.getId());
        assertNull(model);
    }

    @Test
    @Order(5)
    void verifyUserEmail(){

    }

}
