package ir.darkdeveloper.anbarinoo.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ir.darkdeveloper.anbarinoo.model.Authority;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.VerificationModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import lombok.AllArgsConstructor;

@Service("userService")
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo repo;
    private final UserUtils userUtils;
    private final AdminUserProperties adminUser;
    private final VerificationService verificationService;
    private final JwtUtils jwtUtils;
    private final IOUtils ioUtils;
    private final PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userUtils.loadUserByUsername(username);
    }


    // for image updates: only send file
    // to keep images: only send their names
    // to delete images: null files and names
    @Transactional
//    @PreAuthorize("authentication.name.equals(@userService.getAdminUser().getUsername()) || #model.getId() != null")
    public UserModel updateUser(UserModel model, HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals("anonymousUser") || auth.getAuthorities().contains(Authority.OP_ACCESS_ADMIN)
                || auth.getName().equals(model.getEmail())) {
            try {
                checkUserIsSameUserForRequest(model.getId(), req, "update");
                userUtils.resetPasswordUsingPrevious(model);
                userUtils.validateEmail(model);
                ioUtils.handleUserImage(model, userUtils, true);
                return repo.save(model);
            } catch (ForbiddenException f) {
                throw new ForbiddenException(f.getLocalizedMessage());
            } catch (IOException e) {
                throw new InternalServerException(e.getLocalizedMessage());
            }
        }
        throw new ForbiddenException("You are not allowed to update user!");
    }

    @Transactional
    @PreAuthorize("authentication.name.equals(@userService.getAdminUser().getUsername()) || #id != null")
    public ResponseEntity<?> deleteUser(Long id, HttpServletRequest req) {
        try {
            var userOpt = repo.findById(id);
            if (userOpt.isPresent()) {
                checkUserIsSameUserForRequest(userOpt.get().getId(), req, "delete");
                userUtils.deleteUser(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
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
            userUtils.authenticateUser(model, null, null, response);
        else
            userUtils.authenticateUser(model, userUtils.getUserIdByUsernameOrEmail(model.getUsername()), null,
                    response);
        return new ResponseEntity<>(repo.findByEmailOrUsername(model.getUsername()), HttpStatus.OK);

    }

    @Transactional
    public ResponseEntity<?> signUpUser(UserModel model, HttpServletResponse response) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getName().equals("anonymousUser") || auth.getAuthorities().contains(Authority.OP_ACCESS_ADMIN)
                || !auth.getName().equals(model.getEmail())) {
            try {
                userUtils.signupValidation(model, response);
                return new ResponseEntity<>(repo.findByEmailOrUsername(model.getUsername()), HttpStatus.OK);
            } catch (DataIntegrityViolationException e) {
                throw new DataExistsException("User exists!");
            }
        }
        throw new ForbiddenException("You are not allowed to signup a user!");
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

    @Transactional
    public ResponseEntity<?> verifyUserEmail(String token) {

        Optional<VerificationModel> model = verificationService.findByToken(token);

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
        Long id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }
}
