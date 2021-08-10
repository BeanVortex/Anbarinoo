package ir.darkdeveloper.anbarinoo.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup/")
    public ResponseEntity<?> signUpUser(@ModelAttribute UserModel model, HttpServletResponse response)
            throws Exception {
        return userService.signUpUser(model, response);
    }

    @PostMapping("/login/")
    public ResponseEntity<?> loginUser(@RequestBody JwtAuth model, HttpServletResponse response) {
        return userService.loginUser(model, response);
    }

    @GetMapping("/verify/")
    public ResponseEntity<?> verifyUserEmail(@RequestParam("t") String token) {
        return userService.verifyUserEmail(token);
    }

    @PutMapping("/update/{id}/")
    public UserModel updateUser(@ModelAttribute UserModel model, @PathVariable Long id, HttpServletRequest req) {
        return userService.updateUser(model, id, req);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        return userService.deleteUser(id, req);
    }

    @GetMapping("/all/")
    public Page<UserModel> allUsers(Pageable pageable) {
        return userService.allUsers(pageable);
    }

    @GetMapping("/{id}/")
    public UserModel getUserInfo(@PathVariable Long id, HttpServletRequest req) {
        return userService.getUserInfo(id, req);
    }
}
