package ir.darkdeveloper.anbarinoo.service;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    void userSetup(){
        user = new UserModel();
        user.setEmail("email");
        user.setAddress("address");
        user.setDescription("desc");
        user.setUserName("user n");
        user.setPassword("pass1");
        user.setPasswordRepeat("pass1");
        user.setEnabled(true);
        user.setFile(null);
    }

    @Test
    @WithMockUser(username = "anonymous")
    void signUp() throws IOException, Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        service.signUpUser(user, response);
    }

    @Test
    @WithMockUser(username = "email", password = "pass1")
    void deleteUser(){
        service.deleteUser(user);
      /*   Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName()); */
    }

    @Test
    void updateUser(){
        
    }

}
