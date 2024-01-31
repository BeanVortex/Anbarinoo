package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NotFoundException;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.SellModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.SellRepo;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class SellService {

    private final SellRepo repo;
    private final UserAuthUtils userAuthUtils;
    private final ProductService productService;
    private final FinancialUtils fUtils;

    public SellService(SellRepo repo, UserAuthUtils userAuthUtils,
                       @Lazy ProductService productService, @Lazy FinancialUtils fUtils) {
        this.repo = repo;
        this.userAuthUtils = userAuthUtils;
        this.productService = productService;
        this.fUtils = fUtils;
    }


    @Transactional
    public SellModel saveSell(Optional<SellModel> sell, HttpServletRequest req) {
        checkSellData(sell, Optional.empty());
        // checked sell data validity in checkSellData, so it is safe to use orElseThrow
        saveProductCount(sell.orElseThrow(), req);
        return repo.save(sell.orElseThrow());

    }

    @Transactional
    public SellModel updateSell(Optional<SellModel> sell, Long sellId, HttpServletRequest req) {
        checkSellData(sell, Optional.of(sellId));
        var preSell = repo.findById(sellId)
                .orElseThrow(() -> new NotFoundException("Sell record doesn't exist"));
        // checked sell data validity in checkSellData, so it is safe to use orElseThrow
        updateProductCount(sell.orElseThrow(), preSell, req);
        preSell.update(sell.orElseThrow());
        return repo.save(preSell);
    }

    public Page<SellModel> getAllSellRecordsOfProduct(Long productId, HttpServletRequest req, Pageable pageable) {
        // checked the user is same user in this method
        var sells = repo.findAllByProductId(productId, pageable);
        if (!sells.getContent().isEmpty()) {
            var userId = sells.getContent().get(0).getProduct().getCategory().getUser().getId();
            userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch sell records");
            return sells;
        }
        return Page.empty();

    }

    public Page<SellModel> getAllSellRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {

        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch sell records");
        return repo.findAllByProductCategoryUserId(userId, pageable);

    }

    public SellModel getSell(Long sellId, HttpServletRequest req) {

        var foundSellRecord = repo.findById(sellId)
                .orElseThrow(() -> new NotFoundException("Sell record doesn't exist"));
        var userId = foundSellRecord.getProduct().getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch sell records");
        return foundSellRecord;

    }

    @Transactional
    public String deleteSell(Long sellId, HttpServletRequest req) {
        var foundSellRecord = repo.findById(sellId)
                .orElseThrow(() -> new NotFoundException("Sell record doesn't exist"));
        var userId = foundSellRecord.getProduct().getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "delete sell record");
        repo.deleteById(sellId);
        deleteProductCount(foundSellRecord, req);
        return "Sell record Deleted";

    }


    public Page<SellModel> getAllSellRecordsOfUserFromDateTo(Long userId, Optional<FinancialDto> financial,
                                                             HttpServletRequest req, Pageable pageable) {
        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch sell records");
        return repo.findAllByProductCategoryUserIdAndCreatedAtAfterAndCreatedAtBefore(userId, from, to, pageable);
    }

    public Page<SellModel> getAllSellRecordsOfProductFromDateTo(Long productId, Optional<FinancialDto> financial,
                                                                HttpServletRequest req, Pageable pageable) {
        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        var userId = productService.getProduct(productId, req).getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch sell records");
        return repo.findAllByProductIdAndCreatedAtAfterAndCreatedAtBefore(productId, from, to, pageable);

    }

    public void updateNullProduct(ProductModel product){
        repo.updateNullProduct(product);
    }


    private void saveProductCount(SellModel sell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "save sell records");

        if (preProduct.getTotalCount().compareTo(sell.getCount()) >= 0) {
            product.setTotalCount(preProduct.getTotalCount().subtract(sell.getCount()));
            productService.updateProductFromBuyOrSell(Optional.of(product), preProduct);
        } else throw new BadRequestException("Not enough product left in stuck to sell!");
    }

    private void updateProductCount(SellModel sell, SellModel preSell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        var userId = preProduct.getCategory().getUser().getId();
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "update buy a record");

        BigDecimal difference;
        product.setPrice(sell.getPrice());
        if (preProduct.getTotalCount().compareTo(sell.getCount()) >= 0) {

            if (sell.getCount().compareTo(preSell.getCount()) > 0) {
                difference = sell.getCount().subtract(preSell.getCount());
                product.setTotalCount(preProduct.getTotalCount().subtract(difference));
            } else if (sell.getCount().compareTo(preSell.getCount()) < 0) {
                difference = preSell.getCount().subtract(sell.getCount());
                product.setTotalCount(preProduct.getTotalCount().add(difference));
            }
            productService.updateProductFromBuyOrSell(Optional.of(product), preProduct);
        } else throw new BadRequestException("Not enough product left in stuck to sell!");
    }

    private void deleteProductCount(SellModel sell, HttpServletRequest req) {
        var preProduct = productService.getProduct(sell.getProduct().getId(), req);
        var product = new ProductModel();
        product.setTotalCount(preProduct.getTotalCount().subtract(sell.getCount()));
        productService.updateProductFromBuyOrSell(Optional.of(product), preProduct);
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


}
