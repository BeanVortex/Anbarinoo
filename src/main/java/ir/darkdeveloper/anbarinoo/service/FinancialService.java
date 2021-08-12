package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.FinancialModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.FinancialRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@AllArgsConstructor
public class FinancialService {

    private final FinancialRepo repo;
    private final JwtUtils jwtUtils;

    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER')")
    public FinancialModel saveFinancial(FinancialModel financial, HttpServletRequest req) {
        try {

            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            financial.setUser(new UserModel(userId));
            return repo.save(financial);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER') && #financialId != null")
    public FinancialModel updateFinancial(FinancialModel financial, Long financialId, HttpServletRequest req) {
        try {
            if (financial.getId() != null) throw new BadRequestException("Financial id should be null, can't update");

            var foundFinancial = repo.findById(financialId);
            if (foundFinancial.isPresent()) {
                checkUserIsSameUserForRequest(foundFinancial.get().getUser().getId(), req, "update");
                var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
                financial.setUser(new UserModel(userId));
                financial.update(foundFinancial.get());
                return repo.save(financial);
            }
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Financial not found");
    }

    @PreAuthorize("hasAnyAuthority('OP_EDIT_USER') && #financialId != null")
    public FinancialModel getFinancial(Long financialId, HttpServletRequest req) {
        try {
            if (financialId == null) throw new BadRequestException("Financial id should not to be null");
            var foundFinancial = repo.findById(financialId);
            if (foundFinancial.isPresent()) {
                checkUserIsSameUserForRequest(foundFinancial.get().getUser().getId(), req, "fetch");
                return foundFinancial.get();
            }
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Financial not found");
    }

    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's products");
    }
}
