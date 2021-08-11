package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class ProductUtils {

    private final JwtUtils jwtUtils;
    private final ProductRepository repo;
    private final IOUtils ioUtils;

    @Autowired
    public ProductUtils(JwtUtils jwtUtils, ProductRepository repo, IOUtils ioUtils) {
        this.jwtUtils = jwtUtils;
        this.repo = repo;
        this.ioUtils = ioUtils;
    }

    @NotNull
    public ProductModel saveProduct(ProductModel product, HttpServletRequest req) throws IOException {
        if (product.getId() != null) throw new BadRequestException("Product id should null, can't update");
        var refreshToken = req.getHeader("refresh_token");
        var userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();
        product.setUser(new UserModel(userId));
        ioUtils.saveProductImages(product);
        return repo.save(product);
    }

    public ProductModel updateProduct(ProductModel product, ProductModel preProduct) {
        if (product.getImages() != null || product.getFiles() != null)
            throw new ForbiddenException("You can't update images with other data for a product. update images in another way");
        if (product.getUser() != null) throw new ForbiddenException("You can't change the post owner");

        preProduct.merge(product);
        return repo.save(preProduct);
    }

    public ProductModel updateProductImages(ProductModel product, ProductModel preProduct) throws IOException {
        if (product.getFiles() == null)
            throw new BadRequestException("Image files are empty");
        if (product.getUser() != null) throw new ForbiddenException("You can't change the post owner");

        ioUtils.addProductImages(product, preProduct);
        return repo.save(product);
    }

    public void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {

        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }

    public void updateDeleteProductImages(ProductModel product, ProductModel preProduct) {
        ioUtils.updateDeleteProductImages(product, preProduct);
        repo.save(preProduct);
    }
}
