package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.dto.LoginDto;
import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.Operations;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;


@Service("userService")
public class UserService implements UserDetailsService {

    private final UserRepo repo;
    private final UserAuthUtils userAuthUtils;
    private final Operations userOP;
    private final VerificationService verificationService;
    private final AdminUserProperties adminUser;
    private final JwtUtils jwtUtils;

    public UserService(UserRepo repo, @Lazy UserAuthUtils userAuthUtils, Operations userOP,
                       VerificationService verificationService, AdminUserProperties adminUser,
                       JwtUtils jwtUtils) {
        this.repo = repo;
        this.userAuthUtils = userAuthUtils;
        this.userOP = userOP;
        this.verificationService = verificationService;
        this.adminUser = adminUser;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAuthUtils.loadUserByUsername(username)
                .orElseThrow(() -> new NoContentException("User does not exist"));
    }


    @Transactional
    public UserModel updateUser(Optional<UserModel> user, Long id, HttpServletRequest req) {
        user.map(UserModel::getId).ifPresent(i -> user.get().setId(null));
        userAuthUtils.checkUserIsSameUserForRequest(id, req, "update");
        var updatedUser = userOP.updateUser(user, id);
        return repo.save(updatedUser);
    }

    /**
     * updates user images, other values will be ignored
     *
     * @param user other values will be ignored expect files
     * @param id   should not to be null
     * @return updated user images
     */
    @Transactional
    public UserModel updateUserImages(Optional<UserModel> user, Long id, HttpServletRequest req) {
        user.map(UserModel::getId).ifPresent(i -> user.get().setId(null));
        userAuthUtils.checkUserIsSameUserForRequest(id, req, "update images");
        var updatedUser = userOP.updateUserImages(user, id);
        return repo.save(updatedUser);
    }

    /**
     * delete user images
     *
     * @param user other values will be ignored expect file names
     * @param id   should not to be null
     * @return user with deleted image(s) (default images)
     */
    @Transactional
    public UserModel updateDeleteUserImages(Optional<UserModel> user, Long id, HttpServletRequest req) {
        user.map(UserModel::getId).ifPresent(i -> user.get().setId(null));
        userAuthUtils.checkUserIsSameUserForRequest(id, req, "delete images");
        var updatedUser = userOP.updateDeleteUserImages(user, id);
        return repo.save(updatedUser);
    }

    @Transactional
    public String deleteUser(Long id, HttpServletRequest req) {
        var user = repo.findById(id).orElseThrow(() -> new NoContentException("User does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(id, req, "delete");
        userOP.deleteUser(user);
        return "Deleted the user";
    }

    public Page<UserModel> allUsers(Pageable pageable) {
        return repo.getAll(pageable);
    }

    @Transactional
    public UserModel loginUser(LoginDto loginDto, HttpServletResponse response) {
        return userAuthUtils.authenticateUser(loginDto, response);
    }

    @Transactional
    public UserModel signUpUser(Optional<UserModel> model, HttpServletResponse response) {
        return userAuthUtils.signup(model, response);
    }

    public UserModel getUserInfo(Long id, HttpServletRequest req) {
        userAuthUtils.checkUserIsSameUserForRequest(id, req, "fetch");
        return repo.findById(id).orElseThrow(() -> new NoContentException("User does not exist"));
    }


    /**
     * @return a simple user model that won't query for other data like categories, products ...
     */
    public UserModel getSimpleCurrentUserInfo(HttpServletRequest req) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        return repo.getSimpleUserInfo(id).orElseThrow(() -> new NoContentException("User does not exist"));
    }


    @Transactional
    public String verifyUserEmail(String token) {
        var model = verificationService.findByToken(token)
                .orElseThrow(() -> new NoContentException("Link does not exists"));
        if (model.getExpiresAt().isAfter(LocalDateTime.now())) {
            model.setVerifiedAt(LocalDateTime.now());
            repo.updateEnabledById(model.getUser().getId(), true);
            verificationService.saveToken(model);
            return "Email Successfully verified";
        } else
            throw new BadRequestException("Link is expired. try logging in again");
    }


    public AdminUserProperties getAdminUser() {
        return adminUser;
    }
}
