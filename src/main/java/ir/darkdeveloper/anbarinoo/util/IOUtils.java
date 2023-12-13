package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class IOUtils {

    private static final String USER_IMAGE_PATH = "classpath:static/user/profile_images/";
    private static final String PRODUCT_IMAGE_PATH = "classpath:static/user/product_images/";
    private static final String DEFAULT_PROFILE_IMAGE = "noProfile.jpeg";
    private static final String DEFAULT_SHOP_IMAGE = "noImage.png";
    private static final String DEFAULT_PRODUCT_IMAGE = "noImage.png";

    /**
     * @param file MultipartFile
     * @param path after user/
     */
    private Optional<String> saveFile(MultipartFile file, String path) {
        try {
            if (file != null) {
                // first it may not upload and save file in the path. should create static/img
                // folder in resources
                var location = ResourceUtils.getFile(path).getAbsolutePath();
                var bytes = file.getBytes();
                var fileName = UUID.randomUUID() + "." + Objects.requireNonNull(file.getContentType()).split("/")[1];
                Files.write(Paths.get(location + File.separator + fileName), bytes);
                return Optional.of(fileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Optional.empty();
    }

    /**
     * @param path     after classpath:static/user/
     * @param fileName file name
     * @return an absolute path of a file or null if filename is null
     */
    public String getImagePath(String path, String fileName) throws FileNotFoundException {
        if (fileName == null)
            return null;
        return ResourceUtils.getFile(path).getAbsolutePath() + File.separator + fileName;
    }

    /**
     * Saves user images
     *
     * @param user if images are null then the default will set
     */
    public void saveUserImages(Optional<UserModel> user) {

        if (user.isPresent()) {

            var isShopImage = user.map(UserModel::getShopImage).isPresent();
            var isShopFile = user.map(UserModel::getShopFile).isPresent();
            var isProfileImage = user.map(UserModel::getProfileImage).isPresent();
            var isProfileFile = user.map(UserModel::getProfileFile).isPresent();

            if (!isShopImage || !isShopFile)
                user.get().setShopImage(DEFAULT_SHOP_IMAGE);
            if (!isProfileImage || !isProfileFile)
                user.get().setProfileImage(DEFAULT_PROFILE_IMAGE);

            var profileFileName = saveFile(user.get().getProfileFile(), USER_IMAGE_PATH);
            profileFileName.ifPresent(user.get()::setProfileImage);

            var shopFileName = saveFile(user.get().getShopFile(), USER_IMAGE_PATH);
            shopFileName.ifPresent(user.get()::setShopImage);

        }
    }

    public void updateUserImages(Optional<UserModel> userOpt, UserModel preUser) {

        var user = userOpt.orElseThrow(() -> new BadRequestException("User can't be null"));

        if (user.getShopFile() != null && !preUser.getShopImage().equals(DEFAULT_SHOP_IMAGE))
            deleteShopFile(preUser);

        if (user.getProfileFile() != null && !preUser.getProfileImage().equals(DEFAULT_PROFILE_IMAGE))
            deleteProfileFile(preUser);


        var profileFileName = saveFile(user.getProfileFile(), USER_IMAGE_PATH);
        profileFileName.ifPresent(fileName -> {
            user.setProfileImage(fileName);
            preUser.setProfileImage(null);
        });

        var shopFileName = saveFile(user.getShopFile(), USER_IMAGE_PATH);
        shopFileName.ifPresent(fileName -> {
            user.setShopImage(fileName);
            preUser.setShopImage(null);
        });
    }


    public void updateDeleteUserImages(Optional<UserModel> userOpt, UserModel preUser) {
        var user = userOpt.orElseThrow(() -> new BadRequestException("User can't be null"));
        if (user.getShopImage() != null && user.getShopImage().equals(preUser.getShopImage())) {
            deleteShopFile(preUser);
            preUser.setShopImage(DEFAULT_SHOP_IMAGE);
        }

        if (user.getProfileImage() != null && user.getProfileImage().equals(preUser.getProfileImage())) {
            deleteProfileFile(preUser);
            preUser.setProfileImage(DEFAULT_PROFILE_IMAGE);
        }
    }

    /**
     * Used in deleting user
     *
     * @param preModel: should be fetched from db
     */
    public void deleteUserImages(UserModel preModel) {
        if (!preModel.getProfileImage().equals(DEFAULT_PROFILE_IMAGE))
            deleteProfileFile(preModel);
        if (!preModel.getShopImage().equals(DEFAULT_SHOP_IMAGE))
            deleteShopFile(preModel);
    }

    private void deleteProfileFile(UserModel preModel) {
        try {
            if (preModel != null && preModel.getId() != null && preModel.getProfileImage() != null) {
                var imgPath = getImagePath(USER_IMAGE_PATH, preModel.getProfileImage());
                if (imgPath != null) {
                    Files.delete(Paths.get(imgPath));
                }

            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void deleteShopFile(UserModel preModel) {
        try {
            if (preModel != null && preModel.getId() != null && preModel.getShopImage() != null) {
                var imgPath = getImagePath(USER_IMAGE_PATH, preModel.getShopImage());
                if (imgPath != null)
                    Files.delete(Paths.get(imgPath));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    // Product image handling

    /**
     * Saving new images of a product(At creation)
     *
     * @param product: in this you should send new images files
     */
    public void saveProductImages(ProductModel product) {

        var fileNames = new ArrayList<String>();
        var files = product.getFiles();
        if (files == null || files.size() == 0) {
            fileNames.add(DEFAULT_PRODUCT_IMAGE);
            product.setImages(fileNames);
            return;
        }

        if (files.size() > 5)
            throw new BadRequestException("You can't have images more than 5!");

        for (var file : files)
            fileNames.add(saveFile(file, PRODUCT_IMAGE_PATH).orElse(DEFAULT_PRODUCT_IMAGE));

        product.setImages(fileNames);
    }

    /**
     * Adding new images to the product
     *
     * @param product    Contains only new image files, rest will be ignored
     * @param preProduct Data of this object will be merged with product and adds remaining images to product
     */
    public void addMoreProductImages(ProductModel product, ProductModel preProduct) {
        var fileNames = new ArrayList<String>();
        var files = product.getFiles();

        if (preProduct.getImages().size() != 0
                && !preProduct.getImages().get(0).equals(DEFAULT_PRODUCT_IMAGE)
                && preProduct.getImages().size() + files.size() > 5)
            throw new BadRequestException("You can't have images more than 5!");

        if (files.size() > 5)
            throw new BadRequestException("You can't have images more than 5!");

        // saving files and adding the names to fileNames
        for (var file : files)
            fileNames.add(saveFile(file, PRODUCT_IMAGE_PATH).orElse(DEFAULT_PRODUCT_IMAGE));


        // before adding remaining images, delete noImage if exists in preProduct
        if (preProduct.getImages().size() == 1
                && preProduct.getImages().get(0).equals(DEFAULT_PRODUCT_IMAGE))
            preProduct.getImages().remove(DEFAULT_PRODUCT_IMAGE);

        // adding remaining images name in previous product
        fileNames.addAll(preProduct.getImages());
        if (!fileNames.isEmpty())
            product.setImages(fileNames);
    }

    /**
     * Deleting a product's images
     *
     * @param product    Contains images names
     * @param preProduct Deletes an image which is provided in product
     */
    public void deleteProductImages(ProductModel product, ProductModel preProduct) {
        var imageNamesToRemove = new ArrayList<String>();
        product.getImages().forEach(oldImg -> {
            if (preProduct.getImages().contains(oldImg))
                imageNamesToRemove.add(oldImg);
        });

        imageNamesToRemove.forEach(s -> {
            try {
                var imgPath = getImagePath(PRODUCT_IMAGE_PATH, s);
                if (!s.equals(DEFAULT_PRODUCT_IMAGE) && imgPath != null)
                    Files.delete(Paths.get(imgPath));
                preProduct.getImages().remove(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (preProduct.getImages().size() == 0)
            preProduct.getImages().add(DEFAULT_PRODUCT_IMAGE);
    }


    /**
     * When deleting user is useful: it deletes any image own by a user's product
     * it won't delete the default image
     *
     * @param productsOpt : delete this product's images
     */
    public void deleteProductImagesOfUser(Optional<List<ProductModel>> productsOpt) {
        productsOpt.ifPresent(products -> products.stream()
                .map(ProductModel::getImages)
                .flatMap(Collection::stream)
                .forEach(name -> {
                    try {
                        var imgPath = getImagePath(PRODUCT_IMAGE_PATH, name);
                        if (!name.equals(DEFAULT_PRODUCT_IMAGE) && imgPath != null) {
                            Files.delete(Paths.get(imgPath));
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
        );
    }


}
