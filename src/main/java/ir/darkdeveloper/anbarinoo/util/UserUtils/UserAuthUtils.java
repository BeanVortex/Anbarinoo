package ir.darkdeveloper.anbarinoo.util.UserUtils;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.PasswordException;
import ir.darkdeveloper.anbarinoo.model.Auth.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
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

@Component
@AllArgsConstructor
public class UserAuthUtils {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final AdminUserProperties adminUser;
    private final IOUtils ioUtils;
    private final UserRolesService roleService;
    private final Boolean userEnabled;
    private final Operations operations;
    private PasswordUtils passwordUtils;

    public static final SimpleDateFormat TOKEN_EXPIRATION_FORMAT = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");


    public void signup(UserModel user, HttpServletResponse response) throws IOException {
        if (user.getId() != null)
            throw new ForbiddenException("You are not allowed to sign up! :|");

        var rawPass = user.getPassword();

        if (user.getEmail() != null)
            if (user.getUserName() == null || user.getUserName().trim().equals(""))
                user.setUserName(user.getEmail().split("@")[0]);

        passwordUtils.passEqualityChecker(user);

        user.setRoles(roleService.findAllByName("USER"));
        ioUtils.saveUserImages(user);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(userEnabled);
        repo.save(user);
        if (!user.getEnabled())
            operations.sendEmail(user);
        else
            authenticateUser(new JwtAuth(user.getEmail(), rawPass), user.getId(), rawPass, response);

    }

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
            operations.sendEmail(user);
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
//        var accessDate = TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(accessToken));
        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_expiration", refreshDate);
//        response.addHeader("access_expiration", accessDate);
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

}
