package ir.darkdeveloper.anbarinoo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
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

    private final String path = "profiles/";
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

        var user = repo.findByEmailOrUsername(username);

        UsernamePasswordAuthenticationToken auth = null;
        if (rawPass != null)
            auth = new UsernamePasswordAuthenticationToken(username, rawPass);
        else
            auth = new UsernamePasswordAuthenticationToken(username, password);

        try {
            authManager.authenticate(auth);
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
        sendEmail(model);
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

    private void sendEmail(UserModel model) {
        String token = UUID.randomUUID().toString();
        VerificationModel emailVerify = new VerificationModel(token, model, LocalDateTime.now().plusMinutes(20));
        verificationService.saveToken(emailVerify);

        String link = "http://localhost:8080/api/user/verify/?t=" + token;
        emailService.send(model.getEmail(), buildEmail(model.getName(), link));

    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" + "\n"
                + "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" + "\n"
                + "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
                + "    <tbody><tr>\n" + "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" + "        \n"
                + "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n"
                + "          <tbody><tr>\n" + "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n"
                + "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
                + "                  <tbody><tr>\n" + "                    <td style=\"padding-left:10px\">\n"
                + "                  \n" + "                    </td>\n"
                + "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n"
                + "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n"
                + "                    </td>\n" + "                  </tr>\n" + "                </tbody></table>\n"
                + "              </a>\n" + "            </td>\n" + "          </tr>\n" + "        </tbody></table>\n"
                + "        \n" + "      </td>\n" + "    </tr>\n" + "  </tbody></table>\n"
                + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
                + "    <tbody><tr>\n" + "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n"
                + "      <td>\n" + "        \n"
                + "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
                + "                  <tbody><tr>\n"
                + "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n"
                + "                  </tr>\n" + "                </tbody></table>\n" + "        \n" + "      </td>\n"
                + "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" + "    </tr>\n"
                + "  </tbody></table>\n" + "\n" + "\n" + "\n"
                + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
                + "    <tbody><tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n" + "    <tr>\n"
                + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
                + "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n"
                + "        \n"
                + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name
                + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\""
                + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>"
                + "        \n" + "      </td>\n" + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
                + "    </tr>\n" + "    <tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n"
                + "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" + "\n" + "</div></div>";
    }

}
