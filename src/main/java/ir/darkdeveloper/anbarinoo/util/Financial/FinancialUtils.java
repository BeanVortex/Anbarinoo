package ir.darkdeveloper.anbarinoo.util.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import ir.darkdeveloper.anbarinoo.service.Financial.DebtOrDemandService;
import ir.darkdeveloper.anbarinoo.service.Financial.SellService;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
public class FinancialUtils {

    private final DebtOrDemandService dodService;
    private final SellService sellService;
    private final BuyService buyService;


    @NotNull
    public AtomicReference<BigDecimal> getDodCosts(HttpServletRequest req, Pageable pageable, Long userId,
                                                   LocalDateTime from, LocalDateTime to) {
        var dods = dodService.getDODFromToDate(userId, true,
                true, from, to, req, pageable);

        var dodCosts = new AtomicReference<>(new BigDecimal(0));

        dods.forEach(dod -> dodCosts.set(dodCosts.get().add(dod.getAmount())));
        return dodCosts;
    }

    @NotNull
    public AtomicReference<BigDecimal> getDodIncomes(HttpServletRequest req, Pageable pageable, Long userId,
                                                     LocalDateTime from, LocalDateTime to) {
        var dods = dodService.getDODFromToDate(userId, false,
                true, from, to, req, pageable);

        var dodIncomes = new AtomicReference<>(new BigDecimal(0));

        dods.forEach(dod -> dodIncomes.set(dodIncomes.get().add(dod.getAmount())));
        return dodIncomes;
    }

    @NotNull
    public AtomicReference<BigDecimal> getBuyCosts(FinancialModel financial, HttpServletRequest req,
                                                   Pageable pageable, Long userId) {
        var buys = buyService.getBuysFromToDate(userId, financial.getFromDate(),
                financial.getToDate(), req, pageable).getContent();
        var buyCosts = new AtomicReference<>(new BigDecimal(0));

        buys.forEach(buy -> {
            var initCost = buy.getCount().multiply(buy.getPrice());
            var tax = initCost.multiply(BigDecimal.valueOf(buy.getTax(), 2));
            buyCosts.set(buyCosts.get().add(initCost.add(tax)));
        });
        return buyCosts;
    }

    @NotNull
    public AtomicReference<BigDecimal> getSellIncomes(FinancialModel financial, HttpServletRequest req,
                                                      Pageable pageable, Long userId) {
        var sells = sellService.getSellsFromToDate(userId, financial.getFromDate(),
                financial.getToDate(), req, pageable).getContent();

        var incomes = new AtomicReference<>(new BigDecimal(0));

        sells.forEach(sell -> {
            var initCost = sell.getCount().multiply(sell.getPrice());
            var tax = initCost.multiply(BigDecimal.valueOf(sell.getTax(), 2));
            incomes.set(incomes.get().add(initCost.add(tax)));
        });
        return incomes;
    }

}