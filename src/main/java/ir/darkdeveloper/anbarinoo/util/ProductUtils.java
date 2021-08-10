package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

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
    public ProductModel saveProductModel(ProductModel product, HttpServletRequest req) throws IOException {
        var refreshToken = req.getHeader("refresh_token");
        var userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();
        product.setUser(new UserModel(userId));

        Optional<ProductModel> prevProduct = Optional.empty();
        if (product.getId() != null)
            prevProduct = repo.findById(product.getId());

        ioUtils.handleUserProductImages(product, prevProduct);

        return repo.save(product);
    }

    public void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {

        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }
}
