package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.BuyRepo;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Component
public class ProductUtils {

    private final JwtUtils jwtUtils;
    private final ProductRepository repo;
    private final IOUtils ioUtils;
    private final CategoryService categoryService;
    private final BuyRepo buyRepo;

    @Autowired
    public ProductUtils(JwtUtils jwtUtils, ProductRepository repo, IOUtils ioUtils,
                        CategoryService categoryService, BuyRepo buyRepo) {
        this.jwtUtils = jwtUtils;
        this.repo = repo;
        this.ioUtils = ioUtils;
        this.categoryService = categoryService;
        this.buyRepo = buyRepo;
    }

    @NotNull
    public ProductModel saveProduct(ProductModel product, HttpServletRequest req) throws IOException {
        if (product.getId() != null) throw new BadRequestException("Product id should null, can't update");
        var fetchedCat = categoryService.getCategoryById(product.getCategory().getId(), req);
        checkUserIsSameUserForRequest(fetchedCat.getUser().getId(), req, "create");
        product.setCategory(fetchedCat);
        ioUtils.saveProductImages(product);
        return repo.save(product);
    }

    public ProductModel updateProduct(ProductModel product, ProductModel preProduct) {
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


    /**
     * If price or count value is going to update, it will also update the first buy record of product
     */
    public void updateBuyWithProductUpdate(ProductModel product, ProductModel preProduct,
                                           BuyService buyService, HttpServletRequest req) {
        if (preProduct.getCanUpdate()) {
            var buy = (BuyModel) null;
            var firstBuyId = preProduct.getFirstBuyId();
            var isPriceUpdated = false;
            var isCountUpdated = false;
            if (product.getPrice() != null && product.getPrice().compareTo(preProduct.getPrice()) != 0) {
                buy = buyService.getBuy(firstBuyId, req);
                buy.setPrice(product.getPrice());
                isPriceUpdated = true;
            }

            if (product.getTotalCount() != null && product.getTotalCount().compareTo(preProduct.getTotalCount()) != 0) {
                if (buy == null) {
                    buy = buyService.getBuy(firstBuyId, req);
                }
                buy.setCount(product.getTotalCount());
                isCountUpdated = true;
            }

            if (buy != null) {
                if (!isCountUpdated) {
                    buy.setCount(preProduct.getTotalCount());
                }

                if (!isPriceUpdated) {
                    buy.setPrice(preProduct.getPrice());
                }
                preProduct.setCanUpdate(false);
                buyRepo.save(buy);
            }
        } else
            throw new BadRequestException("You can't update the product's totalCount or Price. You have already" +
                    " updated these once or you are updating after selling or buying this product." +
                    " You can only update product's totalCount or price for once right after saving product");
    }
}
