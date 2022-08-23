package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.BuyModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.ProductUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private final IOUtils ioUtils;
    private final ProductUtils productUtils;
    private final BuyService buyService;
    private final UserAuthUtils userAuthUtils;
    private final CategoryService categoryService;
    private final JwtUtils jwtUtils;

    /**
     * saves a new product for the user with userId provided in refresh token
     * if image files are null, then sets a default image
     * if not, saves files and sets images
     */
    @Transactional
    public ProductModel saveProduct(Optional<ProductModel> productOpt, HttpServletRequest req) {

        var product = productUtils.validateAndGetProduct(productOpt);

        productOpt.map(ProductModel::getId).ifPresent(i -> productOpt.get().setId(null));
        var fetchedCat = categoryService.getCategoryById(product.getCategory().getId(), req);
        userAuthUtils.checkUserIsSameUserForRequest(fetchedCat.getUser().getId(), req, "create");
        product.setCategory(fetchedCat);
        ioUtils.saveProductImages(product);
        repo.save(product);

        var buy = BuyModel.builder()
                .product(product).count(product.getTotalCount())
                .price(product.getPrice()).tax(product.getTax()).build();
        buyService.saveBuy(Optional.of(buy), true, req);
        return product;

    }

    /**
     * For regular update with no images:
     * If images and files and id provided, they will be ignored
     *
     * @param product Contains any data except for id, image files, unless will be ignored
     * @return updated product with images kept
     */
    @Transactional
    public ProductModel updateProduct(Optional<ProductModel> product, Long productId, HttpServletRequest req) {

        product.orElseThrow(() -> new BadRequestException("Product can't be null"));
        var preProduct = repo.findById(productId)
                .orElseThrow(() -> new NoContentException("This product does not exist"));
        product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));

        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "update");

        preProduct.update(product.get());
        return repo.save(preProduct);
    }

    /**
     * If a sell or buy record gets updated, product model of that will be updated too.
     * because when you buy or sell any product in your warehouse, total number of that product changes
     */
    public void updateProductFromBuyOrSell(Optional<ProductModel> product, ProductModel preProduct) {
        product.orElseThrow(() -> new BadRequestException("Product can't be null"));
        product.map(ProductModel::getTotalCount)
                .orElseThrow(() -> new BadRequestException("You can't perform buy action with no total count"));
        repo.totalCount(product.get().getTotalCount(), preProduct.getId());
    }

    public Page<ProductModel> findByNameContains(String name, Pageable pageable, HttpServletRequest req) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
        return repo.findByNameContainsAndUserId(name, userId, pageable);
    }

    public ProductModel getProduct(Long productId, HttpServletRequest req) {
        var foundProduct = repo.findById(productId)
                .orElseThrow(() -> new NoContentException("This product does not exist"));
        var userId = foundProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch");
        return foundProduct;
    }

    public Page<ProductModel> getAllProductsOfUser(Pageable pageable, HttpServletRequest req) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
        return repo.findAllByUserId(userId, pageable);
    }

    public List<ProductModel> getAllProducts(HttpServletRequest req) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
        return repo.findAllByUserId(userId);
    }

    /**
     * For adding more images
     *
     * @param product Contains images files, id
     * @return updated product with new images
     */
    @Transactional
    public ProductModel addNewProductImages(Optional<ProductModel> product,
                                            Long productId, HttpServletRequest req) {
        product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));
        var preProduct = repo.findById(productId)
                .orElseThrow(() -> new NoContentException("This product does not exist"));

        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "update");
        return productUtils.updateProductImages(product, preProduct);
    }

    /**
     * For Images delete only
     *
     * @param product Contains images names that are going to be deleted
     */
    @Transactional
    public String deleteProductImages(Optional<ProductModel> product, Long productId,
                                      HttpServletRequest req) {
        product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));
        var preProduct = repo.findById(productId)
                .orElseThrow(() -> new NoContentException("This product does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(preProduct.getCategory().getUser().getId(),
                req, "delete images of another user's product");
        product.orElseThrow(() -> new BadRequestException("Product can't be null"));
        ioUtils.deleteProductImages(product.get(), preProduct);
        repo.save(preProduct);
        return "deleted product images";
    }

    @Transactional
    public String deleteProduct(Long id, HttpServletRequest req) {
        var preProduct = repo.findById(id)
                .orElseThrow(() -> new NoContentException("This product does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(preProduct.getCategory().getUser().getId(),
                req, "delete");
        repo.deleteById(id);
        ioUtils.deleteProductImages(preProduct, preProduct);
        return "Deleted the product";
    }

}