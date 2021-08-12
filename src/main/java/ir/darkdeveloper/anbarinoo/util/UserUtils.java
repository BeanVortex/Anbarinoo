package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.VerificationModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;
import ir.darkdeveloper.anbarinoo.service.VerificationService;
import ir.darkdeveloper.anbarinoo.util.email.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class UserUtils {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRolesService roleService;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final AdminUserProperties adminUser;
    private final VerificationService verificationService;
    private final IOUtils ioUtils;
    private final EmailService emailService;
    private final Boolean userEnabled;
    private final String domainName;

    public static final SimpleDateFormat TOKEN_EXPIRATION_FORMAT = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");

    /**
     * @param authModel has username and password (JwtAuth)
     * @param userId    for super admin, pass null
     * @param rawPass   for super admin, pass null
     */
    public void authenticateUser(JwtAuth authModel, Long userId, String rawPass, HttpServletResponse response) {
        String username = authModel.getUsername();
        String password = authModel.getPassword();

        var user = repo.findByEmailOrUsername(username);

        UsernamePasswordAuthenticationToken auth;
        if (rawPass != null)
            auth = new UsernamePasswordAuthenticationToken(username, rawPass);
        else
            auth = new UsernamePasswordAuthenticationToken(username, password);

        try {
            authManager.authenticate(auth);
        } catch (DisabledException e) {
            throw new BadRequestException("Email is not verified!");
        } catch (Exception e) {
            throw new BadRequestException("Bad Credentials");
        }

        if (!user.getEnabled()) {
            sendEmail(user);
            throw new EmailNotValidException("Email is not verified! Check your emails");
        }

        RefreshModel rModel = new RefreshModel();
        if (authModel.getUsername().equals(adminUser.getUsername())) {
            rModel.setUserId(adminUser.getId());
            rModel.setId(refreshService.getIdByUserId(adminUser.getId()));
        } else {
            rModel.setId(refreshService.getIdByUserId(userId));
            rModel.setUserId(getUserIdByUsernameOrEmail(username));
        }

        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username, rModel.getUserId());

        rModel.setAccessToken(accessToken);

        refreshService.saveToken(rModel);

        setupHeader(response, accessToken, refreshToken);
    }

    public void setupHeader(HttpServletResponse response, String accessToken, String refreshToken) {
        var refreshDate = TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_expiration", refreshDate);
        response.addHeader("access_expiration", accessDate);
    }

    public void signupValidation(UserModel user, HttpServletResponse response) throws Exception {
        if (user.getId() != null)
            throw new ForbiddenException("You are not allowed to sign up! :|");

        if (user.getUserName() != null && user.getUserName().equals(adminUser.getUsername()))
            throw new BadRequestException("User exists!");

        var rawPass = validatePassword(user);
        validateEmail(user);

        user.setRoles(roleService.findAllByName("USER"));
        ioUtils.saveUserImages(user);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setPasswordRepeat("");
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(userEnabled);
        repo.save(user);
        if (!user.getEnabled())
            sendEmail(user);
        else
            authenticateUser(new JwtAuth(user.getEmail(), rawPass), user.getId(), rawPass, response);

    }

    public void validateEmail(UserModel user) {
        if (user.getEmail() != null) {
            var VALID_EMAIL_REGEX =
                    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            var matcher = VALID_EMAIL_REGEX.matcher(user.getEmail());
            if (!matcher.find())
                throw new EmailNotValidException("Email is not correct");

            if (user.getUserName() == null || user.getUserName().trim().equals(""))
                user.setUserName(user.getEmail().split("@")[0]);
        }
    }

    public Long getUserIdByUsernameOrEmail(String username) {
        return repo.findUserIdByUsername(username);
    }


    public UserDetails loadUserByUsername(String username) {
        if (username.equals(adminUser.getUsername())) {
            GrantedAuthority[] authorities = (GrantedAuthority[]) adminUser.getAuthorities().toArray();
            return User.builder().username(adminUser.getUsername())
                    .password(encoder.encode(adminUser.getPassword())).authorities(authorities).build();
        }
        return repo.findByEmailOrUsername(username);
    }

    public void deleteUser(UserModel user) throws IOException {
        if (!user.isEnabled())
            throw new EmailNotValidException("Email is not verified! Check your emails");

        ioUtils.deleteUserImages(user);
        ioUtils.deleteProductImagesOfUser(user.getProducts());
        refreshService.deleteTokenByUserId(user.getId());
        repo.deleteById(user.getId());

    }

    /**
     * updates preUser Password
     */
    private void updatePasswordUsingPrevious(UserModel user, UserModel preUser) {
        //update pass
        if (user.getPassword() != null && user.getPasswordRepeat() != null) {
            if (user.getPrevPassword() != null) {
                validatePassword(user);
                if (encoder.matches(user.getPrevPassword(), preUser.getPassword()))
                    preUser.setPassword(encoder.encode(user.getPassword()));
            } else
                throw new PasswordException("Enter previous password to change");
        }
        //else keep preUser pass

    }

    public UserModel updateUser(UserModel user, Long id) {
        //email update
        //userUtils.validateEmail(model);
        if (user.getProfileImage() != null || user.getShopImage() != null) {
            user.setProfileImage(null);
            user.setShopImage(null);
        }
        var foundUser = repo.findUserById(id);
        if (foundUser.isPresent()) {
            updatePasswordUsingPrevious(user, foundUser.get());
            foundUser.get().update(user);
            return foundUser.get();
        }
        throw new NoContentException("User not found");
    }

    public UserModel updateUserImages(UserModel user, Long id) throws IOException {
        var foundUser = repo.findUserById(id);
        if (foundUser.isPresent()) {
            ioUtils.updateUserImages(user, foundUser.get());
            foundUser.get().merge(user);
            return foundUser.get();
        }
        throw new NoContentException("User not found");
    }

    public UserModel updateDeleteUserImages(UserModel user, Long id) throws IOException {
        var foundUser = repo.findUserById(id);
        if (foundUser.isPresent()) {
            ioUtils.updateDeleteUserImages(user, foundUser.get());
            return foundUser.get();
        }
        throw new NoContentException("User not found");
    }

    public String validatePassword(UserModel user) {
        var rawPass = user.getPassword();
        var rawPassRep = user.getPasswordRepeat();
        if (rawPass != null && rawPassRep != null) {
            if (rawPass.isBlank())
                throw new PasswordException("Password can't be empty");

            if (rawPassRep.isBlank())
                throw new PasswordException("Password repeat can't be empty");

            if (!rawPass.equals(rawPassRep))
                throw new PasswordException("Passwords do not match");
        }
        return rawPass;
    }

    private void sendEmail(UserModel user) {
        String token = UUID.randomUUID().toString();
        VerificationModel emailVerify = new VerificationModel(token, user, LocalDateTime.now().plusMinutes(20));
        verificationService.saveToken(emailVerify);

        String link = domainName + "/api/user/verify/?t=" + token;
        emailService.send(user.getEmail(), emailService.buildEmail(user.getName(), link));

    }


}
