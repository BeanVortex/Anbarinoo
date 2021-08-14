package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FinancialUtils {

    private final FinancialService financialService;

    @Autowired
    public FinancialUtils(FinancialService financialService) {
        this.financialService = financialService;
    }

    public void sellProduct(ProductModel product, ProductModel preProduct) {
//        var soldCount = product.getSoldCount();
//        var price = preProduct.getPrice();
//        var buyPrice = preProduct.getBuyPrice();
//        var earnings = price.multiply(BigDecimal.valueOf(soldCount));
//        var preEarnings = buyPrice.multiply(BigDecimal.valueOf(soldCount));
//        var preFinancial = preProduct.getUser().getFinancial();
//        var profit =
//        preFinancial.setEarnings(preFinancial.getEarnings().add(earnings));
//        preFinancial.setProfit(preFinancial.getProfit().add());
    }
}
