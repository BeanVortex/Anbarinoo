package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.BuyRepo;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class BuyService {

    private final BuyRepo repo;
    private final JwtUtils jwtUtils;
    @Lazy
    private final ProductService productService;
    @Lazy
    private final FinancialUtils fUtils;

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public BuyModel saveBuy(Optional<BuyModel> buy, Boolean isSaveProduct, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkBuyData(buy, Optional.empty());
            if (!isSaveProduct)
                saveProductCount(buy.orElseThrow(), req);
            // checked buy data validity in checkBuyData, so it is safe to use orElseThrow
            return repo.save(buy.orElseThrow());
        });
    }

    @Transactional
    public BuyModel updateBuy(Optional<BuyModel> buy, Long buyId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkBuyData(buy, Optional.of(buyId));
            var preBuy = repo.findById(buyId)
                    .orElseThrow(() -> new NoContentException("Buy record doesn't exist"));
            // checked buy data validity in checkBuyData, so it is safe to use orElseThrow
            updateProductCount(buy.orElseThrow(), preBuy, req);
            preBuy.update(buy.orElseThrow());
            return repo.save(preBuy);
        });
    }

    public Page<BuyModel> getAllBuyRecordsOfProduct(Long productId, HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            // will be checked the user is same user in getProduct method
            var product = productService.getProduct(productId, req);
            return repo.findAllByProductId(product.getId(), pageable);
        });
    }

    public Page<BuyModel> getAllBuyRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserId(userId, pageable);
        });
    }

    public BuyModel getBuy(Long buyId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundBuyRecord = repo.findById(buyId)
                    .orElseThrow(() -> new NoContentException("Buy record doesn't exist"));
            checkUserIsSameUserForRequest(foundBuyRecord.getProduct(), null, req, "fetch");
            return foundBuyRecord;
        });
    }

    @Transactional
    public ResponseEntity<String> deleteBuy(Long buyId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var buy = repo.findById(buyId)
                    .orElseThrow(() -> new NoContentException("Buy record doesn't exist"));
            checkUserIsSameUserForRequest(buy.getProduct(), null, req, "delete buy record of");
            repo.deleteById(buyId);
            deleteProductCount(buy, req);
            return ResponseEntity.ok("Deleted the buy record");
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<BuyModel> getAllBuyRecordsOfUserFromDateTo(Long userId, Optional<FinancialModel> financial,
                                                           HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            var from = fUtils.getFromDate(financial);
            var to = fUtils.getToDate(financial);
            checkUserIsSameUserForRequest(null, userId, req, "fetch");
            return repo.findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(userId,
                    from, to, pageable);
        });
    }

    public Page<BuyModel> getAllBuyRecordsOfProductFromDateTo(Long productId, Optional<FinancialModel> financial,
                                                              HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            var from = fUtils.getFromDate(financial);
            var to = fUtils.getToDate(financial);
            var product = productService.getProduct(productId, req);
            checkUserIsSameUserForRequest(product, null, req, "fetch");
            return repo.findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(productId,
                    from, to, pageable);
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

    private void saveProductCount(BuyModel buy, HttpServletRequest req) {
        var preProduct = productService.getProduct(buy.getProduct().getId(), req);
        checkUserIsSameUserForRequest(preProduct, null, req, "save buy record of");
        var product = ProductModel.builder()
                .totalCount(preProduct.getTotalCount().add(buy.getCount()))
                .canUpdate(false)
                .build();
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct, req);

    }

    private void updateProductCount(BuyModel buy, BuyModel preBuy, HttpServletRequest req) {
        var preProduct = productService.getProduct(buy.getProduct().getId(), req);
        checkUserIsSameUserForRequest(preProduct, null, req, "save buy record of");
        var difference = (BigDecimal) null;
        var product = new ProductModel();
        product.setPrice(buy.getPrice());
        if (buy.getCount().compareTo(preBuy.getCount()) > 0) {
            difference = buy.getCount().subtract(preBuy.getCount());
            product.setTotalCount(preProduct.getTotalCount().add(difference));
        } else if (buy.getCount().compareTo(preBuy.getCount()) < 0) {
            difference = preBuy.getCount().subtract(buy.getCount());
            product.setTotalCount(preProduct.getTotalCount().subtract(difference));
        }
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct, req);
    }

    private void deleteProductCount(BuyModel buy, HttpServletRequest req) {
        var preProduct = productService.getProduct(buy.getProduct().getId(), req);
        var product = ProductModel.builder()
                .totalCount(preProduct.getTotalCount().subtract(buy.getCount()))
                .build();
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct, req);
    }


    private void checkBuyData(Optional<BuyModel> buy, Optional<Long> buyId) {
        buy.map(BuyModel::getProduct)
                .map(ProductModel::getId)
                .orElseThrow(() -> new BadRequestException("Product id is null, Can't sell"));

        buy.ifPresent(buyModel -> buyId.ifPresentOrElse(buyModel::setId, () -> buyModel.setId(null)));

        buy.map(BuyModel::getCount)
                .filter(c -> c.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new BadRequestException("Count of buy can't be null or zero"));

        buy.map(BuyModel::getPrice)
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
            throw new DataExistsException("Buy record exists!");
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

}

