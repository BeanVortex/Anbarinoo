package ir.darkdeveloper.anbarinoo.util.UserUtils;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.EmailNotValidException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.VerificationModel;
import ir.darkdeveloper.anbarinoo.repository.UserRepo;
import ir.darkdeveloper.anbarinoo.service.RefreshService;
import ir.darkdeveloper.anbarinoo.service.VerificationService;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Operations {

    private final VerificationService verificationService;
    private final IOUtils ioUtils;
    private final EmailService emailService;
    private final String domainName;
    private final RefreshService refreshService;
    private final UserRepo repo;
    private final PasswordUtils passwordUtils;

    public void deleteUser(UserModel user) {
        if (!user.isEnabled())
            throw new EmailNotValidException("Email is not verified! Check your emails");

        try {
            ioUtils.deleteUserImages(user);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        user.getCategories().forEach(cat -> ioUtils.deleteProductImagesOfUser(Optional.of(cat.getProducts())));

        refreshService.deleteTokenByUserId(user.getId());
        repo.deleteById(user.getId());
    }

    public UserModel updateUser(Optional<UserModel> user, Long id) {
        //email update
        //userUtils.validateEmail(model);
        user.orElseThrow(() -> new BadRequestException("User can't be null"));
        var profileImageExists = user.map(UserModel::getProfileImage).isPresent();
        var shopImageExists = user.map(UserModel::getShopImage).isPresent();
        if (profileImageExists || shopImageExists) {
            user.get().setProfileImage(null);
            user.get().setShopImage(null);
        }
        var foundUser = repo.findUserById(id).orElseThrow(() -> new NoContentException("User not found"));
        passwordUtils.updatePasswordUsingPrevious(user, foundUser);
        foundUser.update(user.get());
        return foundUser;
    }

    public UserModel updateUserImages(Optional<UserModel> user, Long id) {
        user.orElseThrow(() -> new BadRequestException("User can't be null"));
        var foundUser = repo.findUserById(id).orElseThrow(() -> new NoContentException("User not found"));
        try {
            ioUtils.updateUserImages(user, foundUser);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        //changed merge to update
        foundUser.update(user.get());
        return foundUser;
    }

    public UserModel updateDeleteUserImages(Optional<UserModel> user, Long id) {
        var foundUser = repo.findUserById(id)
                .orElseThrow(() -> new NoContentException("User not found"));
        try {
            ioUtils.updateDeleteUserImages(user, foundUser);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return foundUser;
    }


    public void sendEmail(UserModel user) {
        var token = UUID.randomUUID().toString();
        var emailVerify = new VerificationModel(token, user, LocalDateTime.now().plusMinutes(20));
        verificationService.saveToken(emailVerify);

        var link = domainName + "/api/user/verify/?t=" + token;
        emailService.send(user.getEmail(), emailService.buildEmail(user.getName(), link));

    }

}
