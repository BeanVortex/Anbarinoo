package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.util.Financial.FinancialUtils;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@AllArgsConstructor
public class FinancialService {

    private final JwtUtils jwtUtils;
    private final FinancialUtils fUtils;


    @PreAuthorize("hasAuthority('OP_ACCESS_USER') && #financial.getFromDate() != null " +
            "&& #financial.getToDate() != null ")
    public FinancialModel getCosts(FinancialModel financial, HttpServletRequest req, Pageable pageable) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));

        var newFinancial = new FinancialModel();
        var from = financial.getFromDate();
        var to = financial.getToDate();
        newFinancial.setFromDate(from);
        newFinancial.setToDate(to);

        var buyCosts = fUtils.getBuyCosts(financial, req, pageable, userId);

        var dodCosts = fUtils.getDodCosts(req, pageable, userId, from, to);

        newFinancial.setCosts(buyCosts.get().add(dodCosts.get()));
        return newFinancial;
    }


    @PreAuthorize("hasAuthority('OP_ACCESS_USER') && #financial.getFromDate() != null " +
            "&& #financial.getToDate() != null ")
    public FinancialModel getIncomes(FinancialModel financial, HttpServletRequest req, Pageable pageable) {
        var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));


        var newFinancial = new FinancialModel();
        var from = financial.getFromDate();
        var to = financial.getToDate();
        newFinancial.setFromDate(from);
        newFinancial.setToDate(to);

        var sellIncomes = fUtils.getSellIncomes(financial, req, pageable, userId);

        var dodIncomes = fUtils.getDodIncomes(req, pageable, userId, from, to);

        newFinancial.setIncomes(sellIncomes.get().add(dodIncomes.get()));
        return newFinancial;
    }


}
