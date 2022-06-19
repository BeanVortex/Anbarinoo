package ir.darkdeveloper.anbarinoo.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import ir.darkdeveloper.anbarinoo.dto.UserDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.UserMapper;
import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.dto.LoginDto;
import ir.darkdeveloper.anbarinoo.service.UserService;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/signup/")
    @PreAuthorize("authentication.name.equals(@userService.getAdminUser().username()) " +
            "|| authentication.name.equals('anonymousUser')")
    public ResponseEntity<UserDto> signUpUser(@ModelAttribute @Valid UserModel user, BindingResult bindingResult,
                                              HttpServletResponse response) throws Exception {
        if (!bindingResult.hasErrors())
            return new ResponseEntity<>(
                    userMapper.userToDto(userService.signUpUser(Optional.ofNullable(user), response)),
                    HttpStatus.CREATED);

        var errors = bindingResult.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        throw new BadRequestException(errors.toString());
    }

    @PostMapping("/login/")
    public ResponseEntity<UserDto> loginUser(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        return ResponseEntity.ok(userMapper.userToDto(userService.loginUser(loginDto, response)));
    }

    @GetMapping("/verify/")
    public ResponseEntity<?> verifyUserEmail(@RequestParam("t") String token) {
        return userService.verifyUserEmail(token);
    }

    @PutMapping("/update/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')")
    public ResponseEntity<UserDto> updateUser(@ModelAttribute UserModel user, @PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(
                userMapper.userToDto(userService.updateUser(Optional.ofNullable(user), id, req)));
    }

    @PutMapping("/update/images/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')")
    public ResponseEntity<UserDto> updateUserImages(@ModelAttribute UserModel user, @PathVariable Long id,
                                                    HttpServletRequest req) {
        return ResponseEntity.ok(
                userMapper.userToDto(userService.updateUserImages(Optional.ofNullable(user), id, req)));
    }

    @PutMapping("/update/delete-images/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')")
    public ResponseEntity<UserDto> updateDeleteUserImages(@ModelAttribute UserModel user, @PathVariable Long id,
                                                          HttpServletRequest req) {
        return ResponseEntity.ok(
                userMapper.userToDto(userService.updateDeleteUserImages(Optional.ofNullable(user), id, req)));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_DELETE_USER')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(userService.deleteUser(id, req));
    }

    @GetMapping("/all/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok((userService.allUsers(pageable).map(userMapper::userToDto)));
    }

    @GetMapping("/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public ResponseEntity<UserDto> getUserInfo(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(userMapper.userToDto(userService.getUserInfo(id, req)));
    }

    @GetMapping("/info/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public ResponseEntity<UserDto> getSimpleCurrentUserInfo(HttpServletRequest req) {
        return ResponseEntity.ok(userMapper.userToDto(userService.getSimpleCurrentUserInfo(req)));
    }
}
