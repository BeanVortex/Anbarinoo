package ir.darkdeveloper.anbarinoo.util.UserUtils;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
import ir.darkdeveloper.anbarinoo.exception.PasswordException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class Validations {

    private final PasswordEncoder encoder;
    private final AdminUserProperties adminUser;

    /**
     * updates preUser Password
     */
    public void updatePasswordUsingPrevious(UserModel user, UserModel preUser) {
        //update pass
        if (user.getPassword() != null && user.getPasswordRepeat() != null) {
            if (user.getPrevPassword() != null) {
                validatePassword(user);
                if (encoder.matches(user.getPrevPassword(), preUser.getPassword()))
                    preUser.setPassword(encoder.encode(user.getPassword()));
                else
                    throw new PasswordException("Previous password is wrong");
            } else
                throw new PasswordException("Enter previous password to change");
        }
        //else keep preUser pass

    }

    public void validateUsername(String username) {
        if (username != null && username.equals(adminUser.getUsername()))
            throw new BadRequestException("User exists!");
        if (username != null && username.length() < 6)
            throw new BadRequestException("Username length can't be less than 6 or null");
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
            if (rawPass.length() < 6)
                throw new PasswordException("Password length can't be less than 6");
            if (regexChecker("[A-Z]", rawPass))
                throw new PasswordException("Password does not have at least one Uppercase character");
            if (regexChecker("[0-9]", rawPass))
                throw new PasswordException("Password does not have at least one number");
            if (regexChecker("[^a-zA-Z0-9]", rawPass))
                throw new PasswordException("Password does not have at least one special character");

            return rawPass;
        }
        throw new PasswordException("Passwords are null");

    }

    private boolean regexChecker(String rgx, String value) {
        var pattern = Pattern.compile(rgx);
        var matcher = pattern.matcher(value);
        return !matcher.find();
    }

}
