package ir.darkdeveloper.anbarinoo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import ir.darkdeveloper.anbarinoo.model.UserModel;

@Component
public class IOUtils {

    /**
     * @param file MultipartFile
     * @param path after user/
     */
    public String saveFile(MultipartFile file, String path) throws IOException {
        if (file != null) {
            // first it may not upload and save file in the path. should create static/img
            // folder in resources
            String location = ResourceUtils.getFile("classpath:static/user/" + path).getAbsolutePath();
            byte[] bytes = file.getBytes();
            String fileName = UUID.randomUUID() + "." + Objects.requireNonNull(file.getContentType()).split("/")[1];
            Files.write(Paths.get(location + File.separator + fileName), bytes);
            return fileName;
        }
        return null;
    }

    public String getImagePath(String path, String fileName) throws FileNotFoundException {
        if (fileName == null)
            return null;
        return ResourceUtils.getFile("classpath:static/user/" + path).getAbsolutePath() + File.separator + fileName;
    }

    public void handleUserImages(UserModel model, String path, UserUtils utils)
            throws IOException {
        UserModel preModel = (UserModel) utils.loadUserByUsername(model.getEmail());

        deleteUserImages(preModel, path);

        if (model.getProfileFile() != null && preModel != null && preModel.getProfileImage() != null)
            model.setProfileImage(preModel.getProfileImage());

        if (model.getShopFile() != null && preModel != null && preModel.getShopImage() != null)
            model.setShopImage(preModel.getShopImage());

        String profileFileName = saveFile(model.getProfileFile(), path);
        if (profileFileName != null)
            model.setProfileImage(profileFileName);

        String shopFileName = saveFile(model.getShopFile(), path);
        if (shopFileName != null)
            model.setShopImage(shopFileName);
    }

    public void deleteUserImages(UserModel model, String path) throws IOException {
        if (model != null && model.getId() != null && model.getProfileImage() != null) {
            String imgPath = getImagePath(path, model.getProfileImage());
            if (imgPath != null)
                Files.delete(Paths.get(imgPath));
        }

        if (model != null && model.getId() != null && model.getShopImage() != null) {
            String imgPath = getImagePath(path, model.getShopImage());
            if (imgPath != null)
                Files.delete(Paths.get(imgPath));
        }
    }

}
