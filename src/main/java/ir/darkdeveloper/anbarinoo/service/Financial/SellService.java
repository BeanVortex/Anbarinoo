package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
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
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class SellService {

    private final SellRepo repo;
    private final JwtUtils jwtUtils;
    private final ProductService productService;

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel saveSell(SellModel sell, HttpServletRequest req) {
        try {
            if (sell.getProduct() == null || sell.getProduct().getId() == null)
                throw new BadRequestException("Product id is null, Can't sell");
            if (sell.getId() != null)
                throw new BadRequestException("Id must be null to save a sell record");
            checkUserIsSameUserForRequest(sell.getProduct().getId(), null, null, req, "save sell record of");
            return repo.save(sell);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel updateSell(SellModel sell, Long sellId, HttpServletRequest req) {
        try {
            if (sell.getId() != null)
                throw new BadRequestException("sell id should null for body");
            var preSellOpt = repo.findById(sellId);
            if (preSellOpt.isPresent()) {
                checkUserIsSameUserForRequest(preSellOpt.get().getProduct().getId(), null, null, req, "update sell record of");
                preSellOpt.get().update(sell);
                return repo.save(preSellOpt.get());
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Sell record do not exist.");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfProduct(Long productId, HttpServletRequest req, Pageable pageable) {
        try {
            checkUserIsSameUserForRequest(productId, null, null, req, "fetch");
            return repo.findAllByProductId(productId, pageable);
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
            checkUserIsSameUserForRequest(null, null, userId, req, "fetch");
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
            checkUserIsSameUserForRequest(null, sellId, null, req, "fetch");
            var foundSellRecord = repo.findById(sellId);
            if (foundSellRecord.isPresent())
                return foundSellRecord.get();
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

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public void deleteSell(Long sellId, HttpServletRequest req) {
        try {
            checkUserIsSameUserForRequest(null, sellId, null, req, "delete sell record of");
            repo.deleteById(sellId);
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
            checkUserIsSameUserForRequest(null, null, userId, req, "fetch");
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


    private void checkUserIsSameUserForRequest(Long productId, Long sellId, Long userId, HttpServletRequest req,
                                               String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (userId == null) {
            var product = new ProductModel();
            // for delete
            if (productId == null) {
                var sell = repo.findById(sellId);
                if (sell.isPresent()) {
                    product = sell.get().getProduct();
                } else {
                    throw new NoContentException("Sell record do not exist.");
                }
            } else {
                product = productService.getProduct(productId, req);
            }
            if (!product.getCategory().getUser().getId().equals(id))
                throw new ForbiddenException("You can't " + operation + " another user's products");
        } else if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");

    }
}
