package ir.darkdeveloper.anbarinoo.service.Financial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final FinancialUtils fUtils;


    public FinancialDto getCosts(Optional<FinancialDto> financial, HttpServletRequest req, Pageable pageable) {
        var userId = JwtUtils.getUserId(req.getHeader("refresh_token"));

        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);
        var buyCosts = fUtils.getBuyCosts(financial, req, pageable, userId);

        var dodCosts = fUtils.getDodCosts(req, pageable, userId, from, to);

        return new FinancialDto(buyCosts.get().add(dodCosts.get()), null, from, to);
    }


    public FinancialDto getIncomes(Optional<FinancialDto> financial, HttpServletRequest req, Pageable pageable) {
        var userId = JwtUtils.getUserId(req.getHeader("refresh_token"));

        var to = fUtils.getToDate(financial);
        var from = fUtils.getFromDate(financial);

        var sellIncomes = fUtils.getSellIncomes(financial, pageable, userId, req);
        var dodIncomes = fUtils.getDodIncomes(req, pageable, userId, from, to);

        return new FinancialDto(null, sellIncomes.get().add(dodIncomes.get()), from, to);
    }

    public FinancialDto getProfitOrLoss(Optional<FinancialDto> financial, HttpServletRequest req, Pageable pageable) {
        var userId = JwtUtils.getUserId(req.getHeader("refresh_token"));

        var from = fUtils.getFromDate(financial);
        var to = fUtils.getToDate(financial);

        var sellIncomes = fUtils.getSellIncomes(financial, pageable, userId, req);
        var dodIncomes = fUtils.getDodIncomes(req, pageable, userId, from, to);
        var buyCosts = fUtils.getBuyCosts(financial, req, pageable, userId);
        var dodCosts = fUtils.getDodCosts(req, pageable, userId, from, to);

        var incomes = sellIncomes.get().add(dodIncomes.get());
        var costs = buyCosts.get().add(dodCosts.get());
        var profit = BigDecimal.valueOf(0);
        var loss = BigDecimal.valueOf(0);

        if (incomes.compareTo(costs) > 0)
            profit = calculateProfit(incomes, costs);
        else if (incomes.compareTo(costs) < 0)
            loss = calculateLoss(incomes, costs);

        return new FinancialDto(costs, incomes, profit, loss, from, to);
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
