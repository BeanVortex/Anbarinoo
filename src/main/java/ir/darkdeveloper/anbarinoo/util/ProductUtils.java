package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
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
    private final CategoryService categoryService;

    @Autowired
    public ProductUtils(JwtUtils jwtUtils, ProductRepository repo, IOUtils ioUtils, CategoryService categoryService) {
        this.jwtUtils = jwtUtils;
        this.repo = repo;
        this.ioUtils = ioUtils;
        this.categoryService = categoryService;
    }

    @NotNull
    public ProductModel saveProduct(ProductModel product, HttpServletRequest req) throws IOException {
        if (product.getId() != null) throw new BadRequestException("Product id should null, can't update");
        var fetchedCat = categoryService.getCategoryById(product.getCategory().getId(), req);
        checkUserIsSameUserForRequest(fetchedCat.getUser().getId(), req, "create");
        ioUtils.saveProductImages(product);
        return repo.save(product);
    }

    public ProductModel updateProduct(ProductModel product, ProductModel preProduct) {
        if (product.getImages() != null || product.getFiles() != null)
            throw new ForbiddenException("You can't update images with other data for a product. update images in another way");

        preProduct.update(product);
        return repo.save(preProduct);
    }

    public ProductModel updateProductImages(ProductModel product, ProductModel preProduct) throws IOException {
        if (product.getFiles() == null)
            throw new BadRequestException("Image files are empty");

        ioUtils.addProductImages(product, preProduct);
        product.getCategory().setUser(new UserModel(preProduct.getCategory().getUser().getId()));
        product.update(preProduct);
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
