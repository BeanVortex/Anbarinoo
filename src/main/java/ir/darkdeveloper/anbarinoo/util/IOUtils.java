package ir.darkdeveloper.anbarinoo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import ir.darkdeveloper.anbarinoo.model.UserModel;

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
    private String saveFile(MultipartFile file, String path) throws IOException {
        if (file != null) {
            // first it may not upload and save file in the path. should create static/img
            // folder in resources
            String location = ResourceUtils.getFile(path).getAbsolutePath();
            byte[] bytes = file.getBytes();
            String fileName = UUID.randomUUID() + "." + Objects.requireNonNull(file.getContentType()).split("/")[1];
            Files.write(Paths.get(location + File.separator + fileName), bytes);
            return fileName;
        }
        return null;
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
     * to make profile and shop image to default, send their names as 'default'
     * to update profile and shop image, send their files
     *
     * @param model-
     * @param isUpdate-
     * @param preUser-
     * @throws IOException -
     */
    public void handleUserImage(UserModel model, Boolean isUpdate, Optional<UserModel> preUser)
            throws IOException {
        if (!isUpdate) {
            if (model.getShopImage() == null)
                model.setShopImage(DEFAULT_SHOP_IMAGE);
            if (model.getProfileImage() == null)
                model.setProfileImage(DEFAULT_PROFILE_IMAGE);
        }

        //second condition is for default image when user wants to delete the previous image
        if (model.getShopFile() != null ||
                (model.getShopImage() != null
                        && model.getShopImage().equals("default")
                        && preUser.isPresent()
                        && !preUser.get().getShopImage().equals(DEFAULT_SHOP_IMAGE))) {

            if (preUser.isPresent())
                deleteShopFile(preUser.get());
            model.setShopImage(DEFAULT_SHOP_IMAGE);
        }

        if (model.getProfileFile() != null ||
                (model.getProfileImage() != null
                        && model.getProfileImage().equals("default")
                        && preUser.isPresent()
                        && !preUser.get().getProfileImage().equals(DEFAULT_PROFILE_IMAGE))) {

            if (preUser.isPresent())
                deleteProfileFile(preUser.get());
            model.setProfileImage(DEFAULT_PROFILE_IMAGE);
        }


        String profileFileName = saveFile(model.getProfileFile(), USER_IMAGE_PATH);
        if (profileFileName != null)
            model.setProfileImage(profileFileName);

        String shopFileName = saveFile(model.getShopFile(), USER_IMAGE_PATH);
        if (shopFileName != null)
            model.setShopImage(shopFileName);
    }

    /**
     * Used in deleting user
     *
     * @param preModel: should be fetched from db
     */
    public void deleteUserImages(UserModel preModel) throws IOException {
        if (!preModel.getProfileImage().equals(DEFAULT_PROFILE_IMAGE))
            deleteProfileFile(preModel);
        if (!preModel.getShopImage().equals(DEFAULT_SHOP_IMAGE))
            deleteShopFile(preModel);
    }

    private void deleteProfileFile(UserModel preModel) throws IOException {
        if (preModel != null && preModel.getId() != null && preModel.getProfileImage() != null) {
            var imgPath = getImagePath(USER_IMAGE_PATH, preModel.getProfileImage());
            if (imgPath != null)
                Files.delete(Paths.get(imgPath));

        }
    }

    private void deleteShopFile(UserModel preModel) throws IOException {
        if (preModel != null && preModel.getId() != null && preModel.getShopImage() != null) {
            var imgPath = getImagePath(USER_IMAGE_PATH, preModel.getShopImage());
            if (imgPath != null)
                Files.delete(Paths.get(imgPath));
        }
    }


    // Product image handling

    /**
     * Saving new images of a product(At creation)
     *
     * @param product: in this you should send new images files
     */
    public void saveProductImages(ProductModel product)
            throws IOException {

        var fileNames = new ArrayList<String>();
        var files = product.getFiles();
        if (files == null || files.size() == 0) {
            fileNames.add(DEFAULT_PRODUCT_IMAGE);
            product.setImages(fileNames);
            return;
        }

        if (files.size() > 5)
            throw new BadRequestException("You can't have images more than 5!");

        for (MultipartFile file : files)
            fileNames.add(saveFile(file, PRODUCT_IMAGE_PATH));

        if (!fileNames.isEmpty())
            product.setImages(fileNames);
    }

    /**
     * Adding new images to the product
     *
     * @param product:    in this you should send new images files
     * @param preProduct: data of this object will merged with product and adds remaining images to product
     */
    public void addProductImages(ProductModel product, ProductModel preProduct) throws IOException {
        var fileNames = new ArrayList<String>();
        var files = product.getFiles();

        if (preProduct.getImages().size() + files.size() > 5)
            throw new BadRequestException("You can't have images more than 5!");

        for (MultipartFile file : files)
            fileNames.add(saveFile(file, PRODUCT_IMAGE_PATH));

        //adding remaining images name in previous product
        product.merge(preProduct);
        fileNames.addAll(preProduct.getImages());
        if (!fileNames.isEmpty())
            product.setImages(fileNames);
    }

    /**
     * Deleting a product's to update it
     *
     * @param product:    in this you should specify which image is going to delete
     * @param preProduct: will iterate in this product images and find the one which is going to delete and deletes it
     */
    public void updateDeleteProductImages(ProductModel product, ProductModel preProduct) {
        preProduct.getImages().forEach(oldImg -> {
            if (product.getImages().contains(oldImg)) {
                try {
                    var imgPath = getImagePath(PRODUCT_IMAGE_PATH, oldImg);
                    if (!oldImg.equals(DEFAULT_PRODUCT_IMAGE) && imgPath != null)
                        Files.delete(Paths.get(imgPath));
                    preProduct.getImages().remove(oldImg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * When deleting user is useful: it deletes any image own by a user's product
     * it won't delete the default image
     *
     * @param products : delete these product's images
     */
    public void deleteProductImagesOfUser(List<ProductModel> products) throws IOException {
        if (products != null)
            for (ProductModel product : products)
                for (String name : product.getImages()) {
                    var imgPath = getImagePath(PRODUCT_IMAGE_PATH, name);
                    if (!name.equals(DEFAULT_PRODUCT_IMAGE) && imgPath != null)
                        Files.delete(Paths.get(imgPath));
                }

    }

    /**
     * When deleting a product is useful: it deletes any image own by this product
     *
     * @param product: delete files of a product
     */
    public void deleteProductFiles(ProductModel product) throws IOException {
        var names = product.getImages();
        for (String name : names) {
            var imgPath = getImagePath(PRODUCT_IMAGE_PATH, name);
            if (!name.equals(DEFAULT_PRODUCT_IMAGE) && imgPath != null)
                Files.delete(Paths.get(imgPath));
        }
    }
}
