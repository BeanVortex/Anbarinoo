package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import javassist.NotFoundException;
import org.hibernate.exception.DataException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final JwtUtils jwtUtils;
    private final IOUtils ioUtils;

    @Autowired
    public ProductService(ProductRepository repo, JwtUtils jwtUtils, IOUtils ioUtils) {
        this.repo = repo;
        this.jwtUtils = jwtUtils;
        this.ioUtils = ioUtils;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel saveProduct(ProductModel productModel, HttpServletRequest request) {
        try {
            productModel.setUser(new UserModel(jwtUtils.getUserId(request.getHeader("refresh_token"))));
            return saveProductModel(productModel, request);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public Page<ProductModel> findByNameContains(String name, Pageable pageable, HttpServletRequest req) {
        try {
            var foundData = repo.findByNameContains(name, pageable);
            checkUserIsSameUserForRequest(foundData.getContent().get(0).getUser().getId(), null,
                    req, "fetch");
            return foundData;
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel getProduct(Long productId, HttpServletRequest req) {
        try {
            Optional<ProductModel> product = repo.findById(productId);
            if (product.isPresent()) {
                checkUserIsSameUserForRequest(product.get().getUser().getId(), null, req, "fetch");
                return product.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Product does not exist");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public Page<ProductModel> getOneUserProducts(Long userId, Pageable pageable, HttpServletRequest req) {
        try {
            checkUserIsSameUserForRequest(userId, null, req, "fetch");
            return repo.findAllByUserId(userId, pageable);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel updateProduct(ProductModel productModel, HttpServletRequest req) {
        try {
            if (productModel.getId() == null) throw new NotFoundException("Product id is null, can't update");
            checkUserIsSameUserForRequest(null, productModel.getId(), req, "update");
            return saveProductModel(productModel, req);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NotFoundException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }


    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteProduct(Long id, HttpServletRequest req) {
        try {
            var productOpt = repo.findById(id);
            if (productOpt.isPresent()) {
                checkUserIsSameUserForRequest(productOpt.get().getUser().getId(), null, req, "delete");
                ioUtils.deleteProductFiles(productOpt);
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
        throw new NoContentException("Product does not exist");
    }

    @NotNull
    private ProductModel saveProductModel(ProductModel product, HttpServletRequest req) throws IOException {
        String refreshToken = req.getHeader("refresh_token");
        Long userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();
        product.setUser(new UserModel(userId));

        Optional<ProductModel> prevProduct = Optional.empty();
        if (product.getId() != null)
            prevProduct = repo.findById(product.getId());

        ioUtils.handleUserProductImages(product, prevProduct);

        return repo.save(product);
    }

    private void checkUserIsSameUserForRequest(Long userId, Long productId,
                                               HttpServletRequest req, String operation) {

        if (userId == null) {
            var productFound = repo.findById(productId);
            if (productFound.isPresent())
                userId = productFound.get().getUser().getId();
            else
                throw new NoContentException("Product does not exist.");
        }

        Long id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }
}
