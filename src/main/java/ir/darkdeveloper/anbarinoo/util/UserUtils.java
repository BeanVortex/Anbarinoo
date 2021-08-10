package ir.darkdeveloper.anbarinoo.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.PasswordException;
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

    public static final SimpleDateFormat TOKEN_EXPIRATION_FORMAT = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");

    /**
     * @param model   has username and password (JwtAuth)
     * @param userId  for super admin, pass null
     * @param rawPass for super admin, pass null
     */
    public void authenticateUser(JwtAuth model, Long userId, String rawPass, HttpServletResponse response) {
        String username = model.getUsername();
        String password = model.getPassword();

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
        if (model.getUsername().equals(adminUser.getUsername())) {
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

    public void signupValidation(UserModel model, HttpServletResponse response) throws Exception {
        if (model.getId() != null)
            throw new ForbiddenException("You are not allowed to sign up! :|");

        if (model.getUserName() != null && model.getUserName().equals(adminUser.getUsername()))
            throw new BadRequestException("User exists!");

        var rawPass = validatePassword(model);
        validateEmail(model);

        model.setRoles(roleService.findAllByName("USER"));
        ioUtils.handleUserImage(model, this, false, null);
        model.setPassword(encoder.encode(model.getPassword()));
        model.setPasswordRepeat("");
        model.setProvider(AuthProvider.LOCAL);
        model.setEnabled(userEnabled);
        repo.save(model);
        if (!model.getEnabled())
            sendEmail(model);
        else
            authenticateUser(new JwtAuth(model.getEmail(), rawPass), model.getId(), rawPass, response);

    }

    public void validateEmail(UserModel model) {
        if (model.getEmail() != null) {
            var VALID_EMAIL_REGEX =
                    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            var matcher = VALID_EMAIL_REGEX.matcher(model.getEmail());
            if (!matcher.find())
                throw new EmailNotValidException("Email is not correct");

            if (model.getUserName() == null || model.getUserName().trim().equals(""))
                model.setUserName(model.getEmail().split("@")[0]);
        }
    }

    public Long getUserIdByUsernameOrEmail(String username) {
        return repo.findUserIdByUsername(username);
    }

    public UserModel getUserById(Long id) {
        return repo.findUserById(id).orElse(null);
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
        ioUtils.deleteUserProductImages(user.getProducts());
        refreshService.deleteTokenByUserId(user.getId());
        repo.deleteById(user.getId());

    }

    private void resetPasswordUsingPrevious(UserModel user, Long userId) {
        //update pass
        if (user.getPassword() != null && user.getPasswordRepeat() != null) {
            if (user.getPrevPassword() != null) {
                validatePassword(user);

                var foundUser = repo.findUserById(userId);
                foundUser.ifPresent(userModel -> {
                    if (encoder.matches(user.getPrevPassword(), foundUser.get().getPassword()))
                        user.setPassword(encoder.encode(user.getPassword()));

                });
            } else
                throw new PasswordException("Enter previous password to change");
        } else {
            //keep pass
            var foundUser = repo.findUserById(userId);
            foundUser.ifPresent(userModel -> user.setPassword(userModel.getPassword()));
        }
    }

    public UserModel updateUser(UserModel user, Long id) throws IOException {
        //email update
        //userUtils.validateEmail(model);
        resetPasswordUsingPrevious(user, id);
        ioUtils.handleUserImage(user, this, true, id);
        var fetchedUser = repo.findUserById(id);
        if (fetchedUser.isPresent()) {
            fetchedUser.get().merge(user);
            return fetchedUser.get();
        }
        return null;
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

    private void sendEmail(UserModel model) {
        String token = UUID.randomUUID().toString();
        VerificationModel emailVerify = new VerificationModel(token, model, LocalDateTime.now().plusMinutes(20));
        verificationService.saveToken(emailVerify);

        String link = "http://localhost:8080/api/user/verify/?t=" + token;
        emailService.send(model.getEmail(), emailService.buildEmail(model.getName(), link));

    }


}
