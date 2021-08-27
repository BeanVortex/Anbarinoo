package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.ProductUtils;
import lombok.AllArgsConstructor;
import org.hibernate.exception.DataException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;


@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private final IOUtils ioUtils;
    private final ProductUtils productUtils;
    private final BuyService buyService;
    private final JwtUtils jwtUtils;

    /**
     * saves a new product to the user id of refresh token
     * if image files are null, then sets a default image
     * if not, saves files and sets images
     *
     * @param product id should be null
     * @param req     should contain refresh token
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER') && #product.getCategory() != null")
    public ProductModel saveProduct(ProductModel product, HttpServletRequest req) {
        try {
            var savedProduct = productUtils.saveProduct(product, req);
            var buy = new BuyModel();
            buy.setProduct(savedProduct);
            buy.setCount(savedProduct.getTotalCount());
            buy.setPrice(savedProduct.getPrice());
            buyService.saveBuy(buy, true, req);
            savedProduct.setFirstBuyId(buy.getId());
            return repo.save(savedProduct);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException b) {
            throw new BadRequestException(b.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * For regular update with no images: another users can't update not owned products
     * If images and files and id provided, then they will be ignored
     * If price or count value is going to update, it will also update the first buy record of product
     *
     * @param product   should files and id and user be null
     * @param productId should not to be null
     * @param req       should contain refresh token
     * @return updated product with kept images
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel updateProduct(ProductModel product, Long productId,
                                      HttpServletRequest req) {
        try {
            if (product.getId() != null) product.setId(null);

            var foundProduct = repo.findById(productId);
            if (foundProduct.isPresent()) {
                productUtils.checkUserIsSameUserForRequest(foundProduct.get().getCategory().getUser().getId(),
                        req, "update");
                if (foundProduct.get().getCanUpdate())
                    productUtils.updateBuyWithProductUpdate(product, foundProduct.get(), buyService, req);
                return productUtils.updateProduct(product, foundProduct.get());
            }
            throw new NoContentException("This product does not exist");

        } catch (NoContentException f) {
            throw new NoContentException(f.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * If a sell or buy record gets updated, product model of that will be updated to
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public void updateProductFromBuyOrSell(ProductModel product, ProductModel preProduct, HttpServletRequest req) {
        try {
            if (product.getId() != null) product.setId(null);
            productUtils.checkUserIsSameUserForRequest(preProduct.getCategory().getUser().getId(), req,
                    "update");
            productUtils.updateProduct(product, preProduct);

        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public Page<ProductModel> findByNameContains(String name, Pageable pageable, HttpServletRequest req) {
        try {
            var foundData = repo.findByNameContainsAndCategoryUserId(name,
                    jwtUtils.getUserId(req.getHeader("refresh_token")), pageable);
            if (!foundData.getContent().isEmpty() && foundData.getContent().get(0) != null)
                return foundData;
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("This product does not exist");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel getProduct(Long productId, HttpServletRequest req) {
        try {
            var product = repo.findById(productId);
            if (product.isPresent()) {
                productUtils.checkUserIsSameUserForRequest(product.get().getCategory().getUser().getId(), req, "fetch");
                return product.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("This product does not exist");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public Page<ProductModel> getOneUserProducts(Long userId, Pageable pageable, HttpServletRequest req) {
        try {
            productUtils.checkUserIsSameUserForRequest(userId, req, "fetch");
            return repo.findAllByCategoryUserId(userId, pageable);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * For Images update only: another users can't update not owned products
     *
     * @param product   should files and id and images not to be null and user be null
     * @param productId should not to be null
     * @param req       should contain refresh token
     * @return updated product with new images
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel updateProductImages(ProductModel product, Long productId, HttpServletRequest req) {
        try {
            if (product.getId() != null) throw new BadRequestException("Product id should null, can't update");
            var foundProduct = repo.findById(productId);
            if (foundProduct.isPresent()) {
                productUtils.checkUserIsSameUserForRequest(foundProduct.get().getCategory().getUser().getId(), req, "update");
                return productUtils.updateProductImages(product, foundProduct.get());
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("This product does not exist");
    }

    /**
     * For Images delete only: another users can't update not owned products
     *
     * @param product   should images name not to be null and user and id be null, image names are going to delete
     * @param productId should not to be null
     * @param req       should contain refresh token
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> updateDeleteProductImages(ProductModel product, Long productId, HttpServletRequest req) {
        try {
            if (product.getId() != null) throw new BadRequestException("Product id should null, can't update");
            var foundProduct = repo.findById(productId);
            if (foundProduct.isPresent()) {
                productUtils.checkUserIsSameUserForRequest(foundProduct.get().getCategory().getUser().getId(), req,
                        "delete images of");
                productUtils.updateDeleteProductImages(product, foundProduct.get());
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("This product does not exist");
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteProduct(Long id, HttpServletRequest req) {
        try {
            var productOpt = repo.findById(id);
            if (productOpt.isPresent()) {
                productUtils.checkUserIsSameUserForRequest(productOpt.get().getCategory().getUser().getId(), req, "delete");
                ioUtils.deleteProductFiles(productOpt.get());
                repo.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("This product does not exist");
    }

}
