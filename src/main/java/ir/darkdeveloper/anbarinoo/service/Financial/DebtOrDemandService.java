package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.DebtOrDemandRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DebtOrDemandService {

    private final DebtOrDemandRepo repo;
    private final JwtUtils jwtUtils;
    private final UserAuthUtils userAuthUtils;

    public DebtOrDemandModel saveDOD(Optional<DebtOrDemandModel> dodOpt, HttpServletRequest req) {
        var dod = checkDODAndGet(dodOpt);
        dod.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
        return repo.save(dod);
    }

    /**
     * Only id, nameOf, isDebt, payTo, amount, issuedAt, validTill will take effect
     */
    public DebtOrDemandModel updateDOD(Optional<DebtOrDemandModel> dodOpt, Long id, Boolean isFromCheque, HttpServletRequest req) {
        var dod = checkDODAndGet(dodOpt);
        DebtOrDemandModel foundDod;
        if (isFromCheque)
            foundDod = repo.findByChequeId(dod.getChequeId())
                    .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));
        else
            foundDod = repo.findById(id)
                    .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));

        userAuthUtils.checkUserIsSameUserForRequest(foundDod.getUser().getId(), req, "update Debt or Demand record");
        foundDod.update(dod);
        return repo.save(foundDod);
    }


    public String deleteDODByChequeId(Long chequeId, HttpServletRequest req) {
        var foundDod = repo.findByChequeId(chequeId)
                .orElseThrow(() -> new InternalServerException("Debt or Demand can't be found with cheque"));
        userAuthUtils.checkUserIsSameUserForRequest(foundDod.getUser().getId(), req,
                "delete Debt or Demand record");
        repo.deleteById(foundDod.getId());
        return "Deleted Debt or Demand";
    }

    public Page<DebtOrDemandModel> getAllDODRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch Debt or Demand record");
        return repo.findAllByUserId(userId, pageable);

    }

    public DebtOrDemandModel getDOD(Long dodId, HttpServletRequest req) {
        var foundDod = repo.findById(dodId)
                .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(foundDod.getUser().getId(), req, "fetch Debt or Demand record");
        return foundDod;
    }

    public String deleteDOD(Long dodId, HttpServletRequest req) {
        var foundDod = repo.findById(dodId)
                .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(foundDod.getUser().getId(), req,
                "delete Debt or Demand record");
        repo.deleteById(dodId);
        return "Debt or Demand deleted";
    }

    public Page<DebtOrDemandModel> getDODFromToDate(Long userId, Boolean isDebt, Boolean isCheckedOut,
                                                    LocalDateTime from, LocalDateTime to, HttpServletRequest req,
                                                    Pageable pageable) {
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch");
        return repo.findAllByUserIdAndIsDebtAndIsCheckedOutAndCreatedAtAfterAndCreatedAtBefore(
                userId, isDebt, isCheckedOut, from, to, pageable);

    }


    private DebtOrDemandModel checkDODAndGet(Optional<DebtOrDemandModel> dodOpt) {
        dodOpt.map(DebtOrDemandModel::getUser).ifPresent(userId -> dodOpt.get().setUser(null));
        dodOpt.map(DebtOrDemandModel::getId).ifPresent(i -> dodOpt.get().setId(null));
        return dodOpt
                .orElseThrow(() -> new BadRequestException("Debt Or Demand can't be null"));
    }

}
