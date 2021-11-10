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
public class PasswordUtils {

    private final PasswordEncoder encoder;

    /**
     * updates preUser Password
     */
    public void updatePasswordUsingPrevious(UserModel user, UserModel preUser) {
        //update pass
        if (user.getPassword() != null && user.getPasswordRepeat() != null) {
            if (user.getPrevPassword() != null) {
                if (encoder.matches(user.getPrevPassword(), preUser.getPassword()))
                    preUser.setPassword(encoder.encode(user.getPassword()));
                else
                    throw new PasswordException("Previous password is wrong");
            } else
                throw new PasswordException("Enter previous password to change");
        }
        //else keep preUser pass
    }

    public void passEqualityChecker(UserModel user) {
        if (!user.getPassword().equals(user.getPasswordRepeat()))
            throw new PasswordException("Passwords do not match");
    }
}
