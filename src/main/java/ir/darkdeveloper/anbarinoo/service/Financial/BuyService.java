package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NotFoundException;
import ir.darkdeveloper.anbarinoo.model.BuyModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.BuyRepo;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


@Service
public class BuyService {

    private final BuyRepo repo;
    private final ProductService productService;
    private final FinancialUtils fUtils;
    private final UserAuthUtils userAuthUtils;

    public BuyService(BuyRepo repo, @Lazy ProductService productService,
                      @Lazy FinancialUtils fUtils, UserAuthUtils userAuthUtils) {
        this.repo = repo;
        this.productService = productService;
        this.fUtils = fUtils;
        this.userAuthUtils = userAuthUtils;
    }

    @Transactional
    public BuyModel saveBuy(Optional<BuyModel> buy, Boolean isFromSaveProduct, HttpServletRequest req) {
        checkBuyData(buy, Optional.empty());
        if (!isFromSaveProduct)
            updateProductCount(buy.orElseThrow(), req);
        // checked buy data validity in checkBuyData, so it is safe to use orElseThrow
        return repo.save(buy.orElseThrow());
    }

    @Transactional
    public BuyModel updateBuy(Optional<BuyModel> buy, Long buyId, HttpServletRequest req) {

        checkBuyData(buy, Optional.of(buyId));
        var preBuy = repo.findById(buyId)
                .orElseThrow(() -> new NotFoundException("Buy record doesn't exist"));
        // checked buy data validity in checkBuyData, so it is safe to use orElseThrow
        updateProductCount(buy.orElseThrow(), preBuy, req);
        preBuy.update(buy.orElseThrow());
        return repo.save(preBuy);
    }

    public Page<BuyModel> getAllBuyRecordsOfProduct(Long productId, HttpServletRequest req, Pageable pageable) {
        // will be checked the user is same user in getProduct method
        productService.getProduct(productId, req);
        return repo.findAllByProductId(productId, pageable);
    }

    public Page<BuyModel> getAllBuyRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch buys");
        return repo.findAllByProductCategoryUserId(userId, pageable);
    }

    public BuyModel getBuy(Long buyId, HttpServletRequest req) {
        var foundBuyRecord = repo.findById(buyId)
                .orElseThrow(() -> new NotFoundException("Buy record doesn't exist"));
        var userId = foundBuyRecord.getProduct().getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch a buy");
        return foundBuyRecord;
    }

    @Transactional
    public ResponseEntity<String> deleteBuy(Long buyId, HttpServletRequest req) {
        var foundBuyRecord = repo.findById(buyId)
                .orElseThrow(() -> new NotFoundException("Buy record doesn't exist"));
        var userId = foundBuyRecord.getProduct().getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "delete a buy record");
        repo.deleteById(buyId);
        deleteProductCount(foundBuyRecord, req);
        return ResponseEntity.ok("Deleted the buy record");
    }

    public Page<BuyModel> getAllBuyRecordsOfUserFromDateTo(Long userId, Optional<FinancialDto> financial,
                                                           HttpServletRequest req, Pageable pageable) {
        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch buys");
        return repo.findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(userId, from, to, pageable);
    }

    public Page<BuyModel> getAllBuyRecordsOfProductFromDateTo(Long productId, Optional<FinancialDto> financial,
                                                              HttpServletRequest req, Pageable pageable) {
        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        var preProduct = productService.getProduct(productId, req);
        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch buys");
        return repo.findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(productId, from, to, pageable);
    }

    public void updateNullProduct(ProductModel product){
        repo.updateNullProduct(product);
    }


    private void updateProductCount(BuyModel buy, HttpServletRequest req) {
        var preProduct = productService.getProduct(buy.getProduct().getId(), req);
        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "save a buy record");
        var product = ProductModel.builder()
                .totalCount(preProduct.getTotalCount().add(buy.getCount()))
                .build();
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct);
    }

    private void updateProductCount(BuyModel buy, BuyModel preBuy, HttpServletRequest req) {
        var preProduct = productService.getProduct(buy.getProduct().getId(), req);
        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "save a buy record");
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
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct);
    }

    private void deleteProductCount(BuyModel buy, HttpServletRequest req) {
        var preProduct = productService.getProduct(buy.getProduct().getId(), req);
        var product = ProductModel.builder()
                .totalCount(preProduct.getTotalCount().subtract(buy.getCount()))
                .build();
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct);
    }


    private void checkBuyData(Optional<BuyModel> buy, Optional<Long> buyId) {
        buy.map(BuyModel::getProduct)
                .map(ProductModel::getId)
                .orElseThrow(() -> new BadRequestException("Product id is null, Can't buy"));

        buy.ifPresent(buyModel -> buyId.ifPresentOrElse(buyModel::setId, () -> buyModel.setId(null)));

        buy.map(BuyModel::getCount)
                .filter(c -> c.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new BadRequestException("Count of buy can't be null or zero"));

        buy.map(BuyModel::getPrice)
                .filter(c -> c.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new BadRequestException("Price of buy can't be null or zero"));
    }

}

