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

    private static final String USER_IMAGE_PATH = "profile_images/";
    private static final String PRODUCT_IMAGE_PATH = "product_images/";
    private static final String DEFAULT_PROFILE_IMAGE = "noProfile.jpeg";
    private static final String DEFAULT_SHOP_IMAGE = "noImage.png";


    /**
     * @param file MultipartFile
     * @param path after user/
     */
    private String saveFile(MultipartFile file, String path) throws IOException {
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

    public void handleUserImage(UserModel model, UserUtils utils, Boolean isUpdate, Long updateUserId)
            throws IOException {
        UserModel preModel = null;
        if (isUpdate)
            preModel = utils.getUserById(updateUserId);
        else {
            if (model.getShopImage() == null)
                model.setShopImage("noImage.png");
            if (model.getProfileImage() == null)
                model.setProfileImage("noProfile.jpeg");
        }


        //second condition is for default image when user wants to delete the previous image
        if (model.getShopFile() != null ||
                (model.getShopImage() != null
                        && model.getShopImage().equals("default")
                        && preModel != null
                        && !preModel.getShopImage().equals(DEFAULT_SHOP_IMAGE))) {

            deleteShopFile(preModel);
            model.setShopImage(DEFAULT_SHOP_IMAGE);
        }

        if (model.getProfileFile() != null ||
                (model.getProfileImage() != null
                        && model.getProfileImage().equals("default")
                        && preModel != null
                        && !preModel.getProfileImage().equals(DEFAULT_PROFILE_IMAGE))) {

            deleteProfileFile(preModel);
            model.setProfileImage(DEFAULT_PROFILE_IMAGE);
        }


        String profileFileName = saveFile(model.getProfileFile(), USER_IMAGE_PATH);
        if (profileFileName != null)
            model.setProfileImage(profileFileName);

        String shopFileName = saveFile(model.getShopFile(), USER_IMAGE_PATH);
        if (shopFileName != null)
            model.setShopImage(shopFileName);
    }

    public void deleteUserImages(UserModel preModel) throws IOException {
        if (!preModel.getProfileImage().equals(DEFAULT_PROFILE_IMAGE))
            deleteProfileFile(preModel);
        if (!preModel.getShopImage().equals(DEFAULT_SHOP_IMAGE))
            deleteShopFile(preModel);
    }

    private void deleteProfileFile(UserModel preModel) throws IOException {
        if (preModel != null && preModel.getId() != null && preModel.getProfileImage() != null) {
            String imgPath = getImagePath(USER_IMAGE_PATH, preModel.getProfileImage());
            if (imgPath != null)
                Files.delete(Paths.get(imgPath));

        }
    }

    private void deleteShopFile(UserModel preModel) throws IOException {
        if (preModel != null && preModel.getId() != null && preModel.getShopImage() != null) {
            String imgPath = getImagePath(USER_IMAGE_PATH, preModel.getShopImage());
            if (imgPath != null)
                Files.delete(Paths.get(imgPath));
        }
    }


    public void handleUserProductImages(ProductModel product, Optional<ProductModel> prevProduct)
            throws IOException {

        List<String> fileNames = new ArrayList<>();
        List<MultipartFile> files = product.getFiles();
        if (files == null || files.size() == 0)
            return;


        if (prevProduct.isPresent()) {

            // deleting previous product images file
            if (product.getImages() != null)
                prevProduct.get().getImages().forEach(image -> {
                    if (!product.getImages().contains(image)) {
                        try {
                            prevProduct.get().getImages().remove(image);
                            Files.delete(Paths.get(PRODUCT_IMAGE_PATH + image));
                        } catch (IOException e) {
                            throw new BadRequestException("Can't delete previous images");
                        }
                    }
                });


            if (prevProduct.get().getImages().size() + files.size() > 5)
                throw new BadRequestException("You can't upload images more than 5!");
            //adding remaining images name in previous product
            fileNames.addAll(prevProduct.get().getImages());
        }


        for (MultipartFile file : files)
            fileNames.add(saveFile(file, PRODUCT_IMAGE_PATH));


        if (!fileNames.isEmpty()) {
//            product.getImages().addAll(fileNames);
            product.setImages(fileNames);
        }

    }


    public void deleteUserProductImages(List<ProductModel> products) throws IOException {
        if (products != null)
            for (ProductModel product : products)
                for (String file : product.getImages())
                    Files.delete(Paths.get(PRODUCT_IMAGE_PATH + file));

    }

    public void deleteProductFiles(Optional<ProductModel> productOpt) throws IOException {
        if (productOpt.isEmpty())
            throw new NoContentException("Product does not exists");
        ProductModel product = productOpt.get();
        List<String> names = product.getImages();
        String path = ResourceUtils.getFile("classpath:static/user/product_images").getAbsolutePath();
        for (String name : names)
            Files.delete(Paths.get(path + File.separator + name));

    }
}
