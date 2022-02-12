package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FinancialService {

    private final JwtUtils jwtUtils;
    private final FinancialUtils fUtils;


    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public FinancialModel getCosts(Optional<FinancialModel> financial, HttpServletRequest req, Pageable pageable) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));

        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        var newFinancial = FinancialModel.builder()
                .fromDate(from)
                .toDate(to)
                .build();

        var buyCosts = fUtils.getBuyCosts(financial, req, pageable, userId);

        var dodCosts = fUtils.getDodCosts(req, pageable, userId, from, to);

        newFinancial.setCosts(buyCosts.get().add(dodCosts.get()));
        return newFinancial;
    }


    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public FinancialModel getIncomes(Optional<FinancialModel> financial, HttpServletRequest req, Pageable pageable) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));

        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        var newFinancial = FinancialModel.builder()
                .fromDate(from)
                .toDate(to)
                .build();

        var sellIncomes = fUtils.getSellIncomes(financial, req, pageable, userId);
        var dodIncomes = fUtils.getDodIncomes(req, pageable, userId, from, to);

        newFinancial.setIncomes(sellIncomes.get().add(dodIncomes.get()));
        return newFinancial;
    }

    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public FinancialModel getProfitOrLoss(Optional<FinancialModel> financial, HttpServletRequest req, Pageable pageable) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));

        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        var newFinancial = FinancialModel.builder()
                .fromDate(from)
                .toDate(to)
                .build();

        var sellIncomes = fUtils.getSellIncomes(financial, req, pageable, userId);
        var dodIncomes = fUtils.getDodIncomes(req, pageable, userId, from, to);
        var buyCosts = fUtils.getBuyCosts(financial, req, pageable, userId);
        var dodCosts = fUtils.getDodCosts(req, pageable, userId, from, to);

        var incomes = sellIncomes.get().add(dodIncomes.get());
        var costs = buyCosts.get().add(dodCosts.get());

        if (incomes.compareTo(costs) > 0)
            newFinancial.setProfit(calculateProfit(incomes, costs));
        else if (incomes.compareTo(costs) < 0)
            newFinancial.setLoss(calculateLoss(incomes, costs));
        else {
            newFinancial.setLoss(BigDecimal.valueOf(0));
            newFinancial.setProfit(BigDecimal.valueOf(0));
        }
        return newFinancial;
    }


    private BigDecimal calculateProfit(BigDecimal incomes, BigDecimal costs) {
        var profit = incomes.multiply(BigDecimal.valueOf(100)).divide(costs, RoundingMode.CEILING);
        return profit.subtract(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateLoss(BigDecimal incomes, BigDecimal costs) {
        var loss = incomes.multiply(BigDecimal.valueOf(100)).divide(costs, RoundingMode.CEILING);
        return BigDecimal.valueOf(100).subtract(loss);
    }

}
