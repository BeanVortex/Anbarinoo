package ir.darkdeveloper.anbarinoo.util.UserUtils;

import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.VerificationModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.service.VerificationService;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.email.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
public class Operations {

    private final VerificationService verificationService;
    private final IOUtils ioUtils;
    private final EmailService emailService;
    private final String domainName;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordUtils passwordUtils;

    public void deleteUser(UserModel user) throws IOException {
        if (!user.isEnabled())
            throw new EmailNotValidException("Email is not verified! Check your emails");

        ioUtils.deleteUserImages(user);
        for (var cat : user.getCategories())
            ioUtils.deleteProductImagesOfUser(cat.getProducts());

        refreshService.deleteTokenByUserId(user.getId());
        repo.deleteById(user.getId());

    }

    public UserModel updateUser(UserModel user, Long id) {
        //email update
        //userUtils.validateEmail(model);
        if (user.getProfileImage() != null || user.getShopImage() != null) {
            user.setProfileImage(null);
            user.setShopImage(null);
        }
        var foundUser = repo.findUserById(id);
        if (foundUser.isPresent()) {
            passwordUtils.updatePasswordUsingPrevious(user, foundUser.get());
            foundUser.get().update(user);
            return foundUser.get();
        }
        throw new NoContentException("User not found");
    }

    public UserModel updateUserImages(UserModel user, Long id) throws IOException {
        var foundUser = repo.findUserById(id);
        if (foundUser.isPresent()) {
            ioUtils.updateUserImages(user, foundUser.get());
            //changed merge to update
            foundUser.get().update(user);
            return foundUser.get();
        }
        throw new NoContentException("User not found");
    }

    public UserModel updateDeleteUserImages(UserModel user, Long id) throws IOException {
        var foundUser = repo.findUserById(id);
        if (foundUser.isPresent()) {
            ioUtils.updateDeleteUserImages(user, foundUser.get());
            return foundUser.get();
        }
        throw new NoContentException("User not found");
    }


    public void sendEmail(UserModel user) {
        String token = UUID.randomUUID().toString();
        VerificationModel emailVerify = new VerificationModel(token, user, LocalDateTime.now().plusMinutes(20));
        verificationService.saveToken(emailVerify);

        String link = domainName + "/api/user/verify/?t=" + token;
        emailService.send(user.getEmail(), emailService.buildEmail(user.getName(), link));

    }

}
