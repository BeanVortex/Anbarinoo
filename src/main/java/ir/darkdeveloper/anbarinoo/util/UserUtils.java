package ir.darkdeveloper.anbarinoo.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final String path = "profile_images/";
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

        //SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");
        // var refreshDate = dateFormat.format();
        // var accessDate = dateFormat.format();
        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_expiration", jwtUtils.getExpirationDate(refreshToken).toString());
        response.addHeader("access_expiration", jwtUtils.getExpirationDate(accessToken).toString());
    }

    public void validateUserData(UserModel model) throws IOException {
        model.setRoles(roleService.getRole("USER"));

        var VALID_EMAIL_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        var matcher = VALID_EMAIL_REGEX.matcher(model.getEmail());
        if (!matcher.find())
            throw new EmailNotValidException("Email is not correct");

        if (model.getUserName() == null || model.getUserName().trim().equals(""))
            model.setUserName(model.getEmail().split("@")[0]);

        if (model.getPassword() == null || model.getPassword().isBlank())
            throw new PasswordException("Password can't be empty!");

        if (model.getPasswordRepeat() == null || !model.getPassword().equals(model.getPasswordRepeat()))
            throw new PasswordException("Passwords do not match!");


        ioUtils.handleUserImages(model, path, this);

        model.setPassword(encoder.encode(model.getPassword()));
        model.setPasswordRepeat("");
        model.setProvider(AuthProvider.LOCAL);
    }

    public void signupValidation(UserModel model, HttpServletResponse response) throws Exception {
        if (model.getId() != null)
            throw new ForbiddenException("You are not allowed to sign up! :|");

        if (model.getUserName() != null && model.getUserName().equals(adminUser.getUsername()))
            throw new BadRequestException("User exists!");

        String rawPass = model.getPassword();
        String rawPassRep = model.getPasswordRepeat();

        if (rawPass == null || rawPass.isBlank())
            throw new PasswordException("Password can't be empty");
        if (!rawPass.equals(rawPassRep))
            throw new PasswordException("Passwords do not match");

        validateUserData(model);
        model.setEnabled(userEnabled);
        repo.save(model);
        if (!model.getEnabled())
            sendEmail(model);
        else
            authenticateUser(new JwtAuth(model.getEmail(), rawPass), model.getId(), rawPass, response);

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

    public void deleteUser(UserModel model) throws IOException {
        UserModel model2 = (UserModel) loadUserByUsername(model.getEmail());
        if (!model2.isEnabled())
            throw new EmailNotValidException("Email is not verified! Check your emails");

        ioUtils.deleteUserImages(model2, path);
        //verificationService

        repo.deleteById(model2.getId());
        refreshService.deleteTokenByUserId(model2.getId());
    }

    public void checkCurrentUserIsTheSameAuthed(HttpServletRequest req) {
        Long userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!repo.findUserById(userId).getUserName().equals(auth.getName()))
            throw new ForbiddenException("You can't delete this cheque. It does not belong to you");
    }

    private void sendEmail(UserModel model) {
        String token = UUID.randomUUID().toString();
        VerificationModel emailVerify = new VerificationModel(token, model, LocalDateTime.now().plusMinutes(20));
        verificationService.saveToken(emailVerify);

        String link = "http://localhost:8080/api/user/verify/?t=" + token;
        emailService.send(model.getEmail(), emailService.buildEmail(model.getName(), link));

    }


}
