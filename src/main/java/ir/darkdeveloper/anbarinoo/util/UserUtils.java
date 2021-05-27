package ir.darkdeveloper.anbarinoo.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ir.darkdeveloper.anbarinoo.model.Authority;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.security.JwtAuth;
import ir.darkdeveloper.anbarinoo.security.Crud.RefreshModel;
import ir.darkdeveloper.anbarinoo.security.Crud.RefreshService;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;

@Component
public class UserUtils {

    @Value("${spring.security.user.name}")
    private String adminUsername;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    private final Long adminId = -1L;
    private final String path = "profiles/";

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRolesService roleService;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final IOUtils ioUtils;

    @Autowired
    public UserUtils(AuthenticationManager authManager, JwtUtils jwtUtils, UserRolesService roleService,
            RefreshService refreshService, UserRepo repo, PasswordEncoder encoder, IOUtils ioUtils) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.roleService = roleService;
        this.refreshService = refreshService;
        this.repo = repo;
        this.encoder = encoder;
        this.ioUtils = ioUtils;
    }

    public void authenticateUser(JwtAuth model, Long userId, String rawPass, HttpServletResponse response) {
        String username = model.getUsername();
        String password = model.getPassword();

        if (rawPass != null) {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, rawPass));
        } else {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        }

        RefreshModel rModel = new RefreshModel();
        if (model.getUsername().equals(getAdminUsername())) {
            rModel.setUserId(getAdminId());
            rModel.setId(refreshService.getIdByUserId(getAdminId()));
        } else {
            rModel.setId(refreshService.getIdByUserId(userId));
            rModel.setUserId(getUserIdByUsernameOrEmail(username));
        }

        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username, rModel.getUserId());

        rModel.setRefreshToken(refreshToken);
        rModel.setAccessToken(accessToken);

        refreshService.saveToken(rModel);

        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_token", refreshToken);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss");
        var refreshDate = dateFormat.format(jwtUtils.getExpirationDate(refreshToken));
        var accessDate = dateFormat.format(jwtUtils.getExpirationDate(accessToken));
        response.addHeader("refresh_expiration", refreshDate);
        response.addHeader("access_expiration", accessDate);
    }

    public void validateUserData(UserModel model) throws FileNotFoundException, IOException, Exception {
        model.setRoles(roleService.getRole("USER"));

        if (model.getUserName() == null || model.getUserName().trim().equals("")) {
            model.setUserName(model.getEmail().split("@")[0]);
        }

        UserModel preModel = repo.findUserById(model.getId());

        if (model.getId() != null && model.getFile() != null) {
            Files.delete(Paths.get(ioUtils.getImagePath(preModel, path)));
        }

        if (preModel != null && preModel.getProfilePicture() != null) {
            model.setProfilePicture(preModel.getProfilePicture());
        }

        String fileName = ioUtils.saveFile(model.getFile(), path);
        if (fileName != null) {
            model.setProfilePicture(fileName);
        }
        model.setPassword(encoder.encode(model.getPassword()));
    }

    public Long getUserIdByUsernameOrEmail(String username) {
        return repo.findUserIdByUsername(username);
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public UserDetails loadUserByUsername(String username) {
        if (username.equals(getAdminUsername())) {
            GrantedAuthority[] authorities = { Authority.OP_ACCESS_ADMIN, Authority.OP_EDIT_ADMIN,
                    Authority.OP_ADD_ADMIN, Authority.OP_DELETE_ADMIN, Authority.OP_ACCESS_USER, Authority.OP_EDIT_USER,
                    Authority.OP_DELETE_USER, Authority.OP_ADD_USER, Authority.OP_ADD_ROLE, Authority.OP_DELETE_ROLE,
                    Authority.OP_ACCESS_ROLE, Authority.OP_DELETE_POST, Authority.OP_DELETE_COMMENT };
            return (UserDetails) User.builder().username(getAdminUsername())
                    .password(encoder.encode(getAdminPassword())).authorities(authorities).build();
        }
        return repo.findByEmailOrUsername(username);
    }

    public void deleteUser(UserModel model) throws Exception {
        Files.delete(Paths.get(ioUtils.getImagePath(model, path)));
        repo.deleteById(model.getId());
        refreshService.deleteTokenByUserId(model.getId());
    }

}
