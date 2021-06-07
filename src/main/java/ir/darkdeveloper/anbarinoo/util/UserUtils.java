package ir.darkdeveloper.anbarinoo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.PasswordException;
import ir.darkdeveloper.anbarinoo.model.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.jwt.JwtAuth;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;

@Component
public class UserUtils {

    private final String path = "profiles/";
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRolesService roleService;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final AdminUserProperties adminUser;
    private final IOUtils ioUtils;

    @Autowired
    public UserUtils(AuthenticationManager authManager, JwtUtils jwtUtils, UserRolesService roleService,
            RefreshService refreshService, UserRepo repo, PasswordEncoder encoder, IOUtils ioUtils,
            AdminUserProperties adminUser) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.roleService = roleService;
        this.refreshService = refreshService;
        this.repo = repo;
        this.encoder = encoder;
        this.ioUtils = ioUtils;
        this.adminUser = adminUser;
    }

    /**
     * 
     * @param model has username and password (JwtAuth)
     * @param userId for super admin, pass null
     * @param rawPass for super admin, pass null
     * @param response
     */
    public void authenticateUser(JwtAuth model, Long userId, String rawPass, HttpServletResponse response) {
        String username = model.getUsername();
        String password = model.getPassword();

        if (rawPass != null)
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPass));
        else
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");
        var refreshDate = dateFormat.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = dateFormat.format(jwtUtils.getExpirationDate(accessToken));
        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_expiration", refreshDate);
        response.addHeader("access_expiration", accessDate);
    }

    public void validateUserData(UserModel model) throws IOException {
        model.setRoles(roleService.getRole("USER"));

        if (model.getUserName() == null || model.getUserName().trim().equals(""))
            model.setUserName(model.getEmail().split("@")[0]);

        if (model.getPassword() == null || model.getPassword().isBlank())
            throw new PasswordException("Password can't be empty!");

        if (model.getPasswordRepeat() == null || !model.getPassword().equals(model.getPasswordRepeat()))
            throw new PasswordException("Passwords do not match!");

        UserModel preModel = repo.findUserById(model.getId());

        if (model.getId() != null && model.getFile() != null)
            Files.delete(Paths.get(ioUtils.getImagePath(preModel, path)));

        if (preModel != null && preModel.getProfilePicture() != null)
            model.setProfilePicture(preModel.getProfilePicture());

        String fileName = ioUtils.saveFile(model.getFile(), path);
        if (fileName != null)
            model.setProfilePicture(fileName);

        model.setPassword(encoder.encode(model.getPassword()));
        model.setProvider(AuthProvider.LOCAL);
    }

    public void signupValidation(UserModel model, HttpServletResponse response) throws IOException, Exception {

        if (model.getUserName() != null && model.getUserName().equals(adminUser.getUsername()))
            throw new BadRequestException("User exists!");

        String rawPass = model.getPassword();
        String rawPassRep = model.getPasswordRepeat();

        if (rawPass == null || rawPass.isBlank())
            throw new PasswordException("Password can't be empty");
        if (!rawPass.equals(rawPassRep))
            throw new PasswordException("Passwords do not match");

        validateUserData(model);
        repo.save(model);
        JwtAuth jwtAuth = new JwtAuth();
        jwtAuth.setUsername(model.getEmail());
        jwtAuth.setPassword(model.getPassword());
        authenticateUser(jwtAuth, model.getId(), rawPass, response);
    }

    public Long getUserIdByUsernameOrEmail(String username) {
        return repo.findUserIdByUsername(username);
    }

    public UserDetails loadUserByUsername(String username) {
        if (username.equals(adminUser.getUsername())) {
            GrantedAuthority[] authorities = (GrantedAuthority[]) adminUser.getAuthorities().toArray();
            return (UserDetails) User.builder().username(adminUser.getUsername())
                    .password(encoder.encode(adminUser.getPassword())).authorities(authorities).build();
        }
        return repo.findByEmailOrUsername(username);
    }

    public void deleteUser(UserModel model) throws IOException {
        Files.delete(Paths.get(ioUtils.getImagePath(model, path)));
        repo.deleteById(model.getId());
        refreshService.deleteTokenByUserId(model.getId());
    }

}
