package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.Operations;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service("userService")
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo repo;
    private final UserAuthUtils userAuthUtils;
    private final Operations userOP;
    private final AdminUserProperties adminUser;
    private final VerificationService verificationService;
    private final JwtUtils jwtUtils;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAuthUtils.loadUserByUsername(username);
    }


    /**
     * #model.getId() == null should be null. if wasn't other users can change other users data due to
     * implementation of this method!!
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')")
    public UserModel updateUser(Optional<UserModel> user, Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            user.map(UserModel::getId).ifPresent(i -> user.get().setId(null));
            checkUserIsSameUserForRequest(id, req, "update");
            var updatedUser = userOP.updateUser(user, id);
            return repo.save(updatedUser);
        });
    }

    /**
     * updates user images, other values will be ignored
     *
     * @param user other values will be ignored expect files
     * @param id   should not to be null
     * @return updated user images
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')&& #id != null")
    public UserModel updateUserImages(Optional<UserModel> user, Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            user.map(UserModel::getId).ifPresent(i -> user.get().setId(null));
            try {
                checkUserIsSameUserForRequest(id, req, "update images");
                var updatedUser = userOP.updateUserImages(user, id);
                return repo.save(updatedUser);
            } catch (IOException e) {
                throw new InternalServerException(e.getLocalizedMessage());
            }
        });
    }

    /**
     * delete user images
     *
     * @param user other values will be ignored expect file names
     * @param id   should not to be null
     * @return user with deleted image(s) (default images)
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')")
    public UserModel updateDeleteUserImages(Optional<UserModel> user, Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            user.map(UserModel::getId).ifPresent(i -> user.get().setId(null));
            try {
                checkUserIsSameUserForRequest(id, req, "delete images");
                var updatedUser = userOP.updateDeleteUserImages(user, id);
                return repo.save(updatedUser);
            } catch (IOException e) {
                throw new InternalServerException(e.getLocalizedMessage());
            }
        });
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_DELETE_USER')")
    public ResponseEntity<?> deleteUser(Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var user = repo.findById(id).orElseThrow(() -> new NoContentException("User does not exist"));
            checkUserIsSameUserForRequest(user.getId(), req, "delete");
            try {
                userOP.deleteUser(user);
            } catch (IOException e) {
                throw new InternalServerException(e.getLocalizedMessage());
            }
            return new ResponseEntity<>("Successfully deleted user", HttpStatus.OK);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN')")
    public Page<UserModel> allUsers(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional
    public ResponseEntity<?> loginUser(JwtAuth model, HttpServletResponse response) {

        if (model.getUsername().equals(adminUser.getUsername()))
            userAuthUtils.authenticateUser(model, null, null, response);
        else
            userAuthUtils.authenticateUser(model, userAuthUtils.getUserIdByUsernameOrEmail(model.getUsername()), null,
                    response);
        return new ResponseEntity<>(repo.findByEmailOrUsername(model.getUsername()), HttpStatus.OK);

    }

    @Transactional
    @PreAuthorize("authentication.name.equals(@userService.getAdminUser().getUsername()) " +
            "|| authentication.name.equals('anonymousUser')")
    public ResponseEntity<?> signUpUser(UserModel model, HttpServletResponse response) throws Exception {
        return exceptionHandlers(() -> {
            try {
                userAuthUtils.signup(model, response);
            } catch (IOException e) {
                throw new InternalServerException(e.getLocalizedMessage());
            }
            return new ResponseEntity<>(repo.findByEmailOrUsername(model.getUsername()), HttpStatus.OK);
        });

    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public UserModel getUserInfo(Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var user = repo.findUserById(id).orElseThrow(() -> new NoContentException("User does not exist"));
            checkUserIsSameUserForRequest(id, req, "fetch");
            return user;
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public UserModel getSimpleCurrentUserInfo(HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.getSimpleUserInfo(id).orElseThrow(() -> new NoContentException("User does not exist"));
        });
    }


    @Transactional
    public ResponseEntity<?> verifyUserEmail(String token) {
        var model = verificationService.findByToken(token).orElseThrow(() -> new InternalServerException("Link does not exists"));
        if (model.getExpiresAt().isAfter(LocalDateTime.now())) {
            model.setVerifiedAt(LocalDateTime.now());
            repo.trueEnabledById(model.getUser().getId());
            verificationService.saveToken(model);
            return new ResponseEntity<>("Email Successfully verified", HttpStatus.OK);
        } else
            throw new BadRequestException("Link is expired. try logging in again");
    }

    public AdminUserProperties getAdminUser() {
        return adminUser;
    }

    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }

    private <T> T exceptionHandlers(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (DataIntegrityViolationException e) {
            throw new DataExistsException("User exists!");
        } catch (EmailNotValidException e) {
            throw new EmailNotValidException(e.getLocalizedMessage());
        } catch (PasswordException e) {
            throw new PasswordException(e.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }
}
