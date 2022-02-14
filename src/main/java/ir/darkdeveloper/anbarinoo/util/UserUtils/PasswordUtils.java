package ir.darkdeveloper.anbarinoo.util.UserUtils;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
import ir.darkdeveloper.anbarinoo.exception.PasswordException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.AdminUserProperties;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class PasswordUtils {

    private final PasswordEncoder encoder;

    /**
     * updates preUser Password
     */
    public void updatePasswordUsingPrevious(Optional<UserModel> user, UserModel preUser) {
        //update pass
        var passExists = user.map(UserModel::getPassword).isPresent();
        var rPassExists = user.map(UserModel::getPasswordRepeat).isPresent();
        if (passExists && rPassExists) {
            user.map(UserModel::getPrevPassword).ifPresentOrElse(
                    prePass -> {
                        if (encoder.matches(prePass, preUser.getPassword()))
                            preUser.setPassword(encoder.encode(user.get().getPassword()));
                        else
                            throw new PasswordException("Previous password is wrong");
                    },
                    () -> {
                        throw new PasswordException("Enter previous password to change");
                    });
        }
        //else keep preUser pass
    }

    public void passEqualityChecker(UserModel user) {
        if (!user.getPassword().equals(user.getPasswordRepeat()))
            throw new PasswordException("Passwords do not match");
    }
}
