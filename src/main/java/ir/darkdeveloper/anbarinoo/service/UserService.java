package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.Operations;
import javassist.NotFoundException;
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
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER') && #id != null")
    public UserModel updateUser(UserModel model, Long id, HttpServletRequest req) {
        try {
            if (model.getId() != null) throw new BadRequestException("User id should null, can't update");
            checkUserIsSameUserForRequest(id, req, "update");
            var updatedUser = userOP.updateUser(model, id);
            return repo.save(updatedUser);
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        }
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
    public UserModel updateUserImages(UserModel user, Long id, HttpServletRequest req) {
        try {
            if (user.getId() != null) throw new BadRequestException("User id should null, can't update");
            checkUserIsSameUserForRequest(id, req, "update images");
            var updatedUser = userOP.updateUserImages(user, id);
            return repo.save(updatedUser);
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * delete user images
     *
     * @param user other values will be ignored expect file names
     * @param id   should not to be null
     * @return user with deleted image(s) (default images)
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER') && #id != null")
    public UserModel updateDeleteUserImages(UserModel user, Long id, HttpServletRequest req) {
        try {
            if (user.getId() != null) throw new BadRequestException("User id should null, can't update");
            checkUserIsSameUserForRequest(id, req, "delete images");
            var updatedUser = userOP.updateDeleteUserImages(user, id);
            return repo.save(updatedUser);
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_DELETE_USER')")
    public ResponseEntity<?> deleteUser(Long id, HttpServletRequest req) {
        try {
            if (id == null) throw new NotFoundException("User id is null, can't update");
            var userOpt = repo.findById(id);
            if (userOpt.isPresent()) {
                checkUserIsSameUserForRequest(userOpt.get().getId(), req, "delete");
                userOP.deleteUser(userOpt.get());
                return new ResponseEntity<>("Successfully deleted user", HttpStatus.OK);
            }
        } catch (NotFoundException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (IOException e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("User does not exist");
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
        try {
            userAuthUtils.signup(model, response);
            return new ResponseEntity<>(repo.findByEmailOrUsername(model.getUsername()), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            throw new DataExistsException("User exists!");
        } catch (EmailNotValidException e) {
            throw new EmailNotValidException(e.getLocalizedMessage());
        } catch (PasswordException e) {
            throw new PasswordException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public UserModel getUserInfo(Long id, HttpServletRequest req) {
        try {
            var userOpt = repo.findUserById(id);

            if (userOpt.isPresent()) {
                checkUserIsSameUserForRequest(id, req, "fetch");
                return userOpt.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        }
        throw new NoContentException("User does not exist");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public UserModel getSimpleCurrentUserInfo(HttpServletRequest req) {
        try {
            var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
            var userOpt = repo.getSimpleUserInfo(id);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        }
        throw new NoContentException("User does not exist");
    }


    @Transactional
    public ResponseEntity<?> verifyUserEmail(String token) {

        var model = verificationService.findByToken(token);

        try {
            if (model.isPresent())
                if (model.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                    model.get().setVerifiedAt(LocalDateTime.now());
                    repo.trueEnabledById(model.get().getUser().getId());
                    verificationService.saveToken(model.get());
                    return new ResponseEntity<>("Email Successfully verified", HttpStatus.OK);
                } else
                    throw new BadRequestException("Link is expired. try logging in again");
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new InternalServerException("Link does not exists");
    }

    public AdminUserProperties getAdminUser() {
        return adminUser;
    }

    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }
}
