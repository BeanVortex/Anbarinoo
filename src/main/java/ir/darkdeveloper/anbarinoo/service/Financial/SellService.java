package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.SellRepo;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class SellService {

    private final SellRepo repo;
    private final JwtUtils jwtUtils;
    private final ProductService productService;

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel saveSell(SellModel sell, HttpServletRequest req) {
        try {
            if (sell.getProduct() == null || sell.getProduct().getId() == null)
                throw new BadRequestException("Product id is null, Can't sell");
            if (sell.getId() != null)
                throw new BadRequestException("Id must be null to save a sell record");
            subtractProductCount(sell, req);
            return repo.save(sell);
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


    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel updateSell(SellModel sell, Long sellId, HttpServletRequest req) {
        try {
            if (sell.getId() != null)
                throw new BadRequestException("sell id should null for body");
            var preSellOpt = repo.findById(sellId);
            if (preSellOpt.isPresent()) {
                preSellOpt.get().update(sell);
                subtractProductCount(preSellOpt.get(), req);
                return repo.save(preSellOpt.get());
            }
            throw new NoContentException("Sell record do not exist.");
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

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfProduct(Long productId, HttpServletRequest req, Pageable pageable) {
        try {
            // checked the user is same user in this method
            var product = productService.getProduct(productId, req);
            return repo.findAllByProductId(product.getId(), pageable);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {
        try {
            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserId(userId, pageable);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel getSell(Long sellId, HttpServletRequest req) {
        try {
            var foundSellRecord = repo.findById(sellId);
            if (foundSellRecord.isPresent()) {
                checkUserIsSameUserForRequest(foundSellRecord.get().getProduct(), null, req,
                        "fetch");
                return foundSellRecord.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (NoContentException n) {
            throw new NoContentException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Sell record do not exist.");
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public void deleteSell(Long sellId, HttpServletRequest req) {
        try {
            var sell = repo.findById(sellId);
            if (sell.isPresent()) {
                checkUserIsSameUserForRequest(sell.get().getProduct(), null, req,
                        "delete sell record of");
                repo.deleteById(sellId);
            } else {
                throw new NoContentException("Sell record does not exist");
            }
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


    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getSellsFromToDate(Long userId, LocalDateTime from, LocalDateTime to,
                                              HttpServletRequest req, Pageable pageable) {
        try {
            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(userId, from, to, pageable);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (NoContentException n) {
            throw new NoContentException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }


    private void checkUserIsSameUserForRequest(ProductModel product, Long userId, HttpServletRequest req,
                                               String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (userId == null) {
            if (!product.getCategory().getUser().getId().equals(id))
                throw new ForbiddenException("You can't " + operation + " another user's products");

        } else if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");

    }

    //TODO: don't save a sell if product amount is less than requested sell amount
    private void subtractProductCount(SellModel sell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        checkUserIsSameUserForRequest(preProduct, null, req, "save buy record of");
        product.setTotalCount(preProduct.getTotalCount().subtract(sell.getCount()));
        preProduct.setCanUpdate(false);
        productService.updateProductFromBuyOrSell(product, preProduct,  req);
    }
}
