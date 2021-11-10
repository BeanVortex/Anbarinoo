package ir.darkdeveloper.anbarinoo.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.service.UserService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup/")
    public ResponseEntity<?> signUpUser(@ModelAttribute @Valid UserModel user, BindingResult bindingResult,
                                        HttpServletResponse response) throws Exception {
        if (!bindingResult.hasErrors())
            return userService.signUpUser(user, response);
        var errors = bindingResult.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        throw new BadRequestException(errors.toString());
    }

    @PostMapping("/login/")
    public ResponseEntity<?> loginUser(@RequestBody JwtAuth authModel, HttpServletResponse response) {
        return userService.loginUser(authModel, response);
    }

    @GetMapping("/verify/")
    public ResponseEntity<?> verifyUserEmail(@RequestParam("t") String token) {
        return userService.verifyUserEmail(token);
    }

    @PutMapping("/update/{id}/")
    public UserModel updateUser(@ModelAttribute UserModel user, @PathVariable Long id, HttpServletRequest req) {
        return userService.updateUser(user, id, req);
    }

    @PutMapping("/update/images/{id}/")
    public UserModel updateUserImages(@ModelAttribute UserModel user, @PathVariable Long id, HttpServletRequest req) {
        return userService.updateUserImages(user, id, req);
    }

    @PutMapping("/update/delete-images/{id}/")
    public UserModel updateDeleteUserImages(@ModelAttribute UserModel user, @PathVariable Long id,
                                            HttpServletRequest req) {
        return userService.updateDeleteUserImages(user, id, req);
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        return userService.deleteUser(id, req);
    }

    @GetMapping("/all/")
    public Page<UserModel> getAllUsers(Pageable pageable) {
        return userService.allUsers(pageable);
    }

    @GetMapping("/{id}/")
    public UserModel getUserInfo(@PathVariable Long id, HttpServletRequest req) {
        return userService.getUserInfo(id, req);
    }

    @GetMapping("/info/")
    public UserModel getSimpleCurrentUserInfo(HttpServletRequest req) {
        return userService.getSimpleCurrentUserInfo(req);
    }
}
