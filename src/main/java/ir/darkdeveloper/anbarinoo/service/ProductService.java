package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.BuyModel;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static ir.darkdeveloper.anbarinoo.util.ExceptionUtils.exceptionHandlers;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private final IOUtils ioUtils;
    private final ProductUtils productUtils;
    private final BuyService buyService;
    private final UserAuthUtils userAuthUtils;
    private final JwtUtils jwtUtils;
    private static final String DATA_EXISTS_MESSAGE = "Product exists!";

    /**
     * saves a new product to the user id of refresh token
     * if image files are null, then sets a default image
     * if not, saves files and sets images
     *
     * @param product id should be null
     * @param req     should contain refresh token
     */
    @Transactional
    public ProductModel saveProduct(Optional<ProductModel> product, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            product.map(ProductModel::getCategory).orElseThrow(() -> new BadRequestException("Product can't be null"));
            product.map(ProductModel::getCategory).map(CategoryModel::getId)
                    .orElseThrow(() -> new BadRequestException("Product category or category id can't be empty"));
            product.get().setTax(product.map(ProductModel::getTax).orElse(9));
            var savedProduct = productUtils.saveProduct(product, req);
            var buy = BuyModel.builder()
                    .product(savedProduct).count(savedProduct.getTotalCount())
                    .price(savedProduct.getPrice()).tax(savedProduct.getTax()).build();
            buyService.saveBuy(Optional.of(buy), true, req);
            savedProduct.setFirstBuyId(buy.getId());
            return repo.save(savedProduct);
        }, DATA_EXISTS_MESSAGE);
    }

    /**
     * For regular update with no images: another users can't update, not users who owned products
     * If images and files and id provided, then they will be ignored
     * If price or count value is going to update, it will also update the first buy record of product
     *
     * @param product   should files and id and user be null
     * @param productId should not to be null
     * @param req       should contain refresh token
     * @return updated product with kept images
     */
    @Transactional
    public ProductModel updateProduct(Optional<ProductModel> product, Long productId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));

            var foundProduct = repo.findById(productId)
                    .orElseThrow(() -> new NoContentException("This product does not exist"));
            userAuthUtils.checkUserIsSameUserForRequest(foundProduct.getCategory().getUser().getId(),
                    req, "update");

            if (product.map(ProductModel::getPrice).isPresent()
                    || product.map(ProductModel::getTotalCount).isPresent())
                productUtils.updateBuyWithProductUpdate(product, foundProduct, buyService, req);

            product.orElseThrow(() -> new BadRequestException("Product can't be null"));
            return productUtils.updateProduct(product.get(), foundProduct);
        }, DATA_EXISTS_MESSAGE);
    }

    /**
     * If a sell or buy record gets updated, product model of that will be updated to
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public void updateProductFromBuyOrSell(Optional<ProductModel> product,
                                           ProductModel preProduct,
                                           HttpServletRequest req) {
        exceptionHandlers(() -> {
            product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));
            userAuthUtils.checkUserIsSameUserForRequest(preProduct.getCategory().getUser().getId(),
                    req, "update");
            product.orElseThrow(() -> new BadRequestException("Product can't be null"));
            productUtils.updateProduct(product.get(), preProduct);
            return null;
        }, DATA_EXISTS_MESSAGE);
    }

    public Page<ProductModel> findByNameContains(String name, Pageable pageable, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.findByNameContainsAndUserId(name, userId, pageable);
        }, DATA_EXISTS_MESSAGE);
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel getProduct(Long productId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundProduct = repo.findById(productId)
                    .orElseThrow(() -> new NoContentException("This product does not exist"));
            userAuthUtils.checkUserIsSameUserForRequest(foundProduct.getCategory().getUser().getId(),
                    req, "fetch");
            return foundProduct;
        }, DATA_EXISTS_MESSAGE);
    }

    public Page<ProductModel> getAllProducts(Pageable pageable, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.findAllByUserId(userId, pageable);
        }, DATA_EXISTS_MESSAGE);
    }

    public List<ProductModel> getAllProducts(HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.findAllByUserId(userId);
        }, DATA_EXISTS_MESSAGE);
    }

    /**
     * For Images update only: another users can't, update not owned products
     *
     * @param product   should files and id and images not to be null and user be null
     * @param productId should not to be null
     * @param req       should contain refresh token
     * @return updated product with new images
     */
    @Transactional
    public ProductModel updateProductImages(Optional<ProductModel> product,
                                            Long productId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));
            var foundProduct = repo.findById(productId)
                    .orElseThrow(() -> new NoContentException("This product does not exist"));

            userAuthUtils.checkUserIsSameUserForRequest(foundProduct.getCategory().getUser().getId(),
                    req, "update");
            return productUtils.updateProductImages(product, foundProduct);
        }, DATA_EXISTS_MESSAGE);
    }

    /**
     * For Images delete only: another users can't update, not owned products
     *
     * @param product   should images name not to be null and user and id be null, image names are going to delete
     * @param productId should not to be null
     * @param req       should contain refresh token
     */
    @Transactional
    public ResponseEntity<?> updateDeleteProductImages(Optional<ProductModel> product, Long productId,
                                                       HttpServletRequest req) {
        return exceptionHandlers(() -> {
            product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));
            var foundProduct = repo.findById(productId)
                    .orElseThrow(() -> new NoContentException("This product does not exist"));
            userAuthUtils.checkUserIsSameUserForRequest(foundProduct.getCategory().getUser().getId(),
                    req, "delete images of another user's product");
            product.orElseThrow(() -> new BadRequestException("Product can't be null"));
            productUtils.updateDeleteProductImages(product.get(), foundProduct);
            return new ResponseEntity<>(HttpStatus.OK);
        }, DATA_EXISTS_MESSAGE);
    }

    @Transactional
    public ResponseEntity<?> deleteProduct(Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundProduct = repo.findById(id)
                    .orElseThrow(() -> new NoContentException("This product does not exist"));
            userAuthUtils.checkUserIsSameUserForRequest(foundProduct.getCategory().getUser().getId(),
                    req, "delete");
            repo.deleteById(id);
            ioUtils.deleteProductFiles(foundProduct);
            return ResponseEntity.ok("Deleted the product");
        }, DATA_EXISTS_MESSAGE);
    }

}