package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
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
import java.math.BigDecimal;
import java.util.Optional;

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
            if (sell.getCount() == null || sell.getPrice() == null)
                throw new BadRequestException("Count or Price for sell save, can't be null");
            saveProductCount(sell, req);
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
            if (sell.getCount() == null || sell.getPrice() == null)
                throw new BadRequestException("Count or Price to update a sell, can't be null");
            if (sell.getProduct() == null || sell.getProduct().getId() == null)
                throw new BadRequestException("Product id to update a sell, can't be null");

            var preSellOpt = repo.findById(sellId);
            if (preSellOpt.isPresent()) {
                updateProductCount(sell, preSellOpt.get(), req);
                preSellOpt.get().update(sell);
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
                deleteProductCount(sell.get(), req);
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
    public Page<SellModel> getAllSellRecordsOfUserFromDateTo(Long userId, Optional<FinancialModel> financial,
                                                             HttpServletRequest req, Pageable pageable) {
        try {
            var from = financial
                    .map(FinancialModel::getFromDate)
                    .orElseThrow(() -> new BadRequestException("From date must not be null"));
            var to = financial
                    .map(FinancialModel::getToDate)
                    .orElseThrow(() -> new BadRequestException("To date must not be null"));

            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(userId,
                    from, to, pageable);
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

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfProductFromDateTo(Long productId, FinancialModel financial,
                                                                HttpServletRequest req, Pageable pageable) {
        try {
            if (financial.getFromDate() == null || financial.getToDate() == null)
                throw new BadRequestException("fromDate and toDate date must not be null");
            var product = productService.getProduct(productId, req);
            checkUserIsSameUserForRequest(product, null, req, "fetch");
            return repo.findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(productId,
                    financial.getFromDate(), financial.getToDate(), pageable);
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

    private void saveProductCount(SellModel sell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        checkUserIsSameUserForRequest(preProduct, null, req, "save buy record of");
        if (preProduct.getTotalCount().compareTo(sell.getCount()) >= 0) {
            product.setTotalCount(preProduct.getTotalCount().subtract(sell.getCount()));
            preProduct.setCanUpdate(false);
            productService.updateProductFromBuyOrSell(product, preProduct, req);
        } else throw new BadRequestException("Not enough product left in stuck to sell!");
    }

    private void updateProductCount(SellModel sell, SellModel preSell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        checkUserIsSameUserForRequest(preProduct, null, req, "save buy record of");
        var difference = (BigDecimal) null;
        product.setPrice(sell.getPrice());
        if (preProduct.getTotalCount().compareTo(sell.getCount()) >= 0) {

            if (sell.getCount().compareTo(preSell.getCount()) > 0) {
                difference = sell.getCount().subtract(preSell.getCount());
                product.setTotalCount(preProduct.getTotalCount().subtract(difference));
                productService.updateProductFromBuyOrSell(product, preProduct, req);
            } else if (sell.getCount().compareTo(preSell.getCount()) < 0) {
                difference = preSell.getCount().subtract(sell.getCount());
                product.setTotalCount(preProduct.getTotalCount().add(difference));
                productService.updateProductFromBuyOrSell(product, preProduct, req);
            }
        } else throw new BadRequestException("Not enough product left in stuck to sell!");
    }

    private void deleteProductCount(SellModel sell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        product.setTotalCount(preProduct.getTotalCount().subtract(sell.getCount()));
        productService.updateProductFromBuyOrSell(product, preProduct, req);
    }

}
