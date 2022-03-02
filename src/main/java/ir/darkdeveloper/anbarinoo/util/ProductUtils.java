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
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    public ProductModel saveProduct(Optional<ProductModel> productOpt, HttpServletRequest req) {
        productOpt.map(ProductModel::getId).ifPresent(i -> productOpt.get().setId(null));
        var product = productOpt.orElseThrow(() -> new BadRequestException("Product can't be null"));
        var fetchedCat = categoryService.getCategoryById(product.getCategory().getId(), req);
        checkUserIsSameUserForRequest(fetchedCat.getUser().getId(), req, "create");
        product.setCategory(fetchedCat);
        try {
            ioUtils.saveProductImages(product);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return repo.save(product);
    }

    public ProductModel updateProduct(ProductModel product, ProductModel preProduct) {
        preProduct.update(product);
        return repo.save(preProduct);
    }

    public ProductModel updateProductImages(Optional<ProductModel> product, ProductModel preProduct) throws IOException {
        product.map(ProductModel::getFiles)
                .orElseThrow(() -> new BadRequestException("Image files are empty"));

        ioUtils.addProductImages(product.get(), preProduct);
        product.get().getCategory().setUser(new UserModel(preProduct.getCategory().getUser().getId()));
        product.get().update(preProduct);
        return repo.save(product.get());
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
    public void updateBuyWithProductUpdate(Optional<ProductModel> product, ProductModel preProduct,
                                           BuyService buyService, HttpServletRequest req) {
        if (preProduct.getCanUpdate()) {
            var buy = new AtomicReference<>(Optional.<BuyModel>empty());
            var firstBuyId = preProduct.getFirstBuyId();
            var isPriceUpdated = new AtomicBoolean(false);
            var isCountUpdated = new AtomicBoolean(false);

            product.map(ProductModel::getPrice)
                    .filter(price -> price.compareTo(preProduct.getPrice()) != 0)
                    .ifPresent(b -> {
                        var tmp = buyService.getBuy(firstBuyId, req);
                        tmp.setPrice(product.get().getPrice());
                        buy.set(Optional.of(tmp));
                        isPriceUpdated.set(true);
                    });

            product.map(ProductModel::getTotalCount)
                    .filter(count -> count.compareTo(preProduct.getTotalCount()) != 0)
                    .ifPresent(b -> {
                        buy.get().ifPresentOrElse(
                                buyModel -> buyModel.setCount(product.get().getTotalCount()),
                                () -> {
                                    buy.set(Optional.of(buyService.getBuy(firstBuyId, req)));
                                    buy.get().ifPresent(buyModel -> buyModel.setCount(product.get().getTotalCount()));
                                }
                        );
                        isCountUpdated.set(true);
                    });


            buy.get().ifPresent(buyModel -> {
                if (!isCountUpdated.get())
                    buyModel.setCount(preProduct.getTotalCount());
                if (!isPriceUpdated.get())
                    buyModel.setPrice(preProduct.getPrice());

                preProduct.setCanUpdate(false);
                buyRepo.save(buyModel);
            });

        } else
            throw new BadRequestException("You can't update the product's totalCount or Price. You have already" +
                    " updated these once or you are updating after selling or buying this product." +
                    " You can only update product's totalCount or price for once right after saving product");
    }
}
