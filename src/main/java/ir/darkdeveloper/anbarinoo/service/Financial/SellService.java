package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.SellRepo;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.hibernate.exception.DataException;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class SellService {

    private final SellRepo repo;
    private final JwtUtils jwtUtils;
    private final ProductService productService;
    private final FinancialUtils fUtils;

    public SellService(SellRepo repo, JwtUtils jwtUtils,
                       ProductService productService, @Lazy FinancialUtils fUtils) {
        this.repo = repo;
        this.jwtUtils = jwtUtils;
        this.productService = productService;
        this.fUtils = fUtils;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel saveSell(Optional<SellModel> sell, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkSellData(sell, Optional.empty());
            // checked sell data validity in checkSellData, so it is safe to use orElseThrow
            saveProductCount(sell.orElseThrow(), req);
            return repo.save(sell.orElseThrow());
        });
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel updateSell(Optional<SellModel> sell, Long sellId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkSellData(sell, Optional.of(sellId));
            var preSell = repo.findById(sellId)
                    .orElseThrow(() -> new NoContentException("Sell record doesn't exist"));
            // checked sell data validity in checkSellData, so it is safe to use orElseThrow
            updateProductCount(sell.orElseThrow(), preSell, req);
            preSell.update(sell.orElseThrow());
            return repo.save(preSell);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfProduct(Long productId, HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            // checked the user is same user in this method
            var product = productService.getProduct(productId, req);
            return repo.findAllByProductId(product.getId(), pageable);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserId(userId, pageable);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public SellModel getSell(Long sellId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundSellRecord = repo.findById(sellId)
                    .orElseThrow(() -> new NoContentException("Sell record doesn't exist"));
            checkUserIsSameUserForRequest(foundSellRecord.getProduct(), null, req, "fetch");
            return foundSellRecord;
        });
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> deleteSell(Long sellId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var sell = repo.findById(sellId)
                    .orElseThrow(() -> new NoContentException("Sell record doesn't exist"));
            checkUserIsSameUserForRequest(sell.getProduct(), null, req, "delete sell record of");
            repo.deleteById(sellId);
            deleteProductCount(sell, req);
            return ResponseEntity.ok("Deleted the sell record");
        });
    }


    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfUserFromDateTo(Long userId, Optional<FinancialModel> financial,
                                                             HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            var from = fUtils.getFromDate(financial);
            var to = fUtils.getToDate(financial);
            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(userId, from, to, pageable);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<SellModel> getAllSellRecordsOfProductFromDateTo(Long productId, Optional<FinancialModel> financial,
                                                                HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            var from = fUtils.getFromDate(financial);
            var to = fUtils.getToDate(financial);
            var product = productService.getProduct(productId, req);
            checkUserIsSameUserForRequest(product, null, req, "fetch");
            return repo.findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(productId, from, to, pageable);
        });
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
            productService.updateProductFromBuyOrSell(Optional.of(product), preProduct, req);
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
            } else if (sell.getCount().compareTo(preSell.getCount()) < 0) {
                difference = preSell.getCount().subtract(sell.getCount());
                product.setTotalCount(preProduct.getTotalCount().add(difference));
            }
            productService.updateProductFromBuyOrSell(Optional.of(product), preProduct, req);
        } else throw new BadRequestException("Not enough product left in stuck to sell!");
    }

    private void deleteProductCount(SellModel sell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        product.setTotalCount(preProduct.getTotalCount().subtract(sell.getCount()));
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct, req);
    }


    private void checkSellData(Optional<SellModel> sell, Optional<Long> sellId) {
        sell.map(SellModel::getProduct)
                .map(ProductModel::getId)
                .orElseThrow(() -> new BadRequestException("Product id is null, Can't sell"));


        sell.ifPresent(sellModel -> sellId.ifPresentOrElse(sellModel::setId, () -> sellModel.setId(null)));

        sell.map(SellModel::getCount)
                .filter(c -> c.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new BadRequestException("Count of sell can't be null or zero"));

        sell.map(SellModel::getPrice)
                .filter(c -> c.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new BadRequestException("Price of buy can't be null or zero"));
    }


    private <T> T exceptionHandlers(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (DataException | BadRequestException e) {
            throw new BadRequestException(e.getLocalizedMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        } catch (DataIntegrityViolationException e) {
            throw new DataExistsException("Sell record exists!");
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }


}
