package ir.darkdeveloper.anbarinoo.util.UserUtils;

import ir.darkdeveloper.anbarinoo.dto.LoginDto;
import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAuthUtils {

    private final AuthenticationManager authManager;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final AdminUserProperties adminUser;
    private final IOUtils ioUtils;
    private final UserRolesService roleService;
    private final Boolean userEnabled;
    private final Operations operations;
    private final PasswordUtils passwordUtils;

    public static final DateTimeFormatter TOKEN_EXPIRATION_FORMAT =
            DateTimeFormatter.ofPattern("EE MMM dd yyyy HH:mm:ss");


    @Transactional
    public UserModel signup(Optional<UserModel> user, HttpServletResponse response) {
        var rawPass = user.map(UserModel::getPassword)
                .orElseThrow(() -> new PasswordException("Password is required!"));
        validateUserData(user);
        var role = roleService.findAllByName("USER");
        user.get().setRoles(role);
        ioUtils.saveUserImages(user);
        user.get().setPassword(encoder.encode(user.get().getPassword()));
        user.get().setProvider(AuthProvider.LOCAL);
        user.get().setEnabled(userEnabled);
        repo.save(user.get());
        if (userEnabled)
            return authenticateUser(new LoginDto(user.get().getEmail(), rawPass), response);
        else
            return user.get();
    }

    /**
     * @param loginDto has username and password (LoginDto)
     */
    public UserModel authenticateUser(LoginDto loginDto, HttpServletResponse response) {
        var username = loginDto.username();
        var password = loginDto.password();

        var user = new UserModel();

        var rModel = new RefreshModel();
        if (loginDto.username().equals(adminUser.username())) {
            rModel.setUserId(adminUser.id());
            rModel.setId(refreshService.getIdByUserId(adminUser.id()));
            user.setEnabled(true);
        } else {
            user = repo.findByEmailOrUsername(username)
                    .orElseThrow(() -> new NoContentException("User does not exist"));
            //TODO extra query if user is signing up
            var id = refreshService.getIdByUserId(user.getId());
            rModel.setId(id);
            rModel.setUserId(user.getId());
        }

        var auth = new UsernamePasswordAuthenticationToken(username, password);

        if (!user.getEnabled()) {
            operations.sendEmail(user);
            throw new EmailNotValidException("Email verification sent, Check your emails");
        }

        try {
            authManager.authenticate(auth);
        } catch (DisabledException e) {
            throw new EmailNotValidException("Email is not verified!");
        } catch (Exception e) {
            throw new BadRequestException("Bad Credentials");
        }

        var accessToken = JwtUtils.generateAccessToken(username);
        var refreshToken = JwtUtils.generateRefreshToken(username, rModel.getUserId());

        rModel.setAccessToken(accessToken);
        rModel.setRefreshToken(refreshToken);
        refreshService.saveToken(rModel);

        setupHeader(response, accessToken, refreshToken);
        return user;
    }

    public static void setupHeader(HttpServletResponse response, String accessToken, String refreshToken) {
        var date = JwtUtils.getExpirationDate(refreshToken);
        var refreshDate = TOKEN_EXPIRATION_FORMAT.format(date);
        response.addHeader("refresh_token", refreshToken);
        response.addHeader("access_token", accessToken);
        response.addHeader("refresh_expiration", refreshDate);
    }

    public Optional<? extends UserDetails> loadUserByUsername(String username) {
        if (username.equals(adminUser.username())) {
            var authorities = adminUser.authorities();
            var superAdmin = User.builder()
                    .username(adminUser.username())
                    .password(encoder.encode(adminUser.password()))
                    .authorities(authorities)
                    .build();
            return Optional.of(superAdmin);
        }
        return repo.findByEmailOrUsername(username);
    }

    private void validateUserData(Optional<UserModel> user) {
        user.map(UserModel::getId).ifPresent(id -> {
            throw new ForbiddenException("You are not allowed to sign up! :|");
        });

        user.map(UserModel::getEmail)
                .ifPresent(email -> {
                    var username = user.map(UserModel::getUserName);
                    if (username.isEmpty() || username.get().isBlank() || username.get().equals(adminUser.username()))
                        user.get().setUserName(email.split("@")[0]);
                });
        passwordUtils.passEqualityChecker(user);
    }

    /**
     * @param userId if it is null, then it compares id in jwt token and id mapped to this token in database
     */
    public void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var token = req.getHeader("refresh_token");
        if (!JwtUtils.isTokenExpired(token)) {

            var id = JwtUtils.getUserId(token);
            if (userId != null && !id.equals(userId))
                throw new ForbiddenException("You don't have permission to " + operation);
            else {
                // in case when attacker tried to change the userId in refreshToken
                // db query
                if (!id.equals(userId)) {
                    var fetchedId = refreshService.getUserIdByRefreshToken(token)
                            .orElseThrow(() -> new ForbiddenException("You are logged out. Try logging in again"));
                    if (!fetchedId.equals(id))
                        throw new ForbiddenException("You don't have permission to " + operation);
                }
            }

        } else
            throw new ForbiddenException("You are logged out. Try logging in again");
    }
}
