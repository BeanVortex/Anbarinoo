package ir.darkdeveloper.anbarinoo.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.util.IOUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final JwtUtils jwtUtils;
    private final UserUtils userUtils;
    private final IOUtils ioUtils;

    @Autowired
    public ProductService(ProductRepository repo, JwtUtils jwtUtils, UserUtils userUtils, IOUtils ioUtils) {
        this.repo = repo;
        this.jwtUtils = jwtUtils;
        this.userUtils = userUtils;
        this.ioUtils = ioUtils;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel saveProduct(ProductModel productModel, HttpServletRequest request) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(request);
            return saveProductModel(productModel, request);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public Page<ProductModel> findByNameContains(String name, Pageable pageable, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            return repo.findByNameContains(name, pageable);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel getProduct(Long productId, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            Optional<ProductModel> product = repo.findById(productId);
            if (product.isPresent())
                return product.get();
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Data you are looking for is not found");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public Page<ProductModel> getOneUserProducts(Long userId, Pageable pageable, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            return repo.findAllByUserId(userId, pageable);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ProductModel updateProduct(ProductModel productModel, HttpServletRequest request) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(request);
            if (productModel.getId() == null) throw new NotFoundException("Product id is null, can't update");
            return saveProductModel(productModel, request);
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
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            Optional<ProductModel> productOpt = repo.findById(id);
            ioUtils.deleteProductFiles(productOpt);
            repo.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @NotNull
    private ProductModel saveProductModel(ProductModel product, HttpServletRequest request) throws IOException {
        String refreshToken = request.getHeader("refresh_token");
        Long userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();
        product.setUser(new UserModel(userId));

        Optional<ProductModel> prevProduct = Optional.empty();
        if (product.getId() != null)
            prevProduct = repo.findById(product.getId());

        ioUtils.handleUserProductImages(product, prevProduct);

        return repo.save(product);
    }


}
