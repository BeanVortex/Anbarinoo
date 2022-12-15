package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.ChequeRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChequeService {

    private final ChequeRepo repo;
    private final JwtUtils jwtUtils;
    private final UserAuthUtils userAuthUtils;
    private final DebtOrDemandService dodService;

    /**
     * Saving a new cheque will save in debt or demand model too
     */
    @Transactional
    public ChequeModel saveCheque(Optional<ChequeModel> cheque, HttpServletRequest req) {
        cheque.orElseThrow(() -> new BadRequestException("Cheque can't be null"));
        checkId(cheque);
        cheque.get().setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
        var savedCheque = repo.save(cheque.get());
        var dod = createDodFromCheque(savedCheque);
        dodService.saveDOD(Optional.of(dod), req);
        return savedCheque;
    }


    /**
     * Updating a cheque will update in debt or demand model too
     */
    @Transactional
    public ChequeModel updateCheque(Optional<ChequeModel> cheque, Long id, HttpServletRequest req) {
        cheque.orElseThrow(() -> new BadRequestException("Cheque can't be null"));
        checkId(cheque);
        var foundCheque = repo.findById(id)
                .orElseThrow(() -> new NoContentException("Cheque does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(foundCheque.getUser().getId(), req, "update");
        foundCheque.update(cheque.get());
        var savedCheque = repo.save(foundCheque);
        var dod = createDodFromCheque(savedCheque);
        dodService.updateDOD(Optional.of(dod), null, true, req);
        return savedCheque;
    }


    public List<ChequeModel> getChequesByUserId(Long userId, HttpServletRequest req) {
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "fetch");
        return repo.findChequeModelsByUser_Id(userId);
    }

    public ChequeModel getCheque(Long id, HttpServletRequest req) {
        var cheque = repo.findById(id)
                .orElseThrow(() -> new NoContentException("Cheque does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(cheque.getUser().getId(), req, "fetch");
        return cheque;
    }

    public List<ChequeModel> findByPayToContains(String payTo, HttpServletRequest req) {
        var fetchedData = repo.findChequeModelByPayToContains(payTo);
        userAuthUtils.checkUserIsSameUserForRequest(fetchedData.get(0).getUser().getId(), req, "fetch");
        return fetchedData;
    }


    /**
     * Deleting a cheque will delete in debt or demand model too
     */
    @Transactional
    public ResponseEntity<?> deleteCheque(Long id, HttpServletRequest req) {
        var foundCheque = repo.findById(id)
                .orElseThrow(() -> new NoContentException("Cheque does not exist"));
        userAuthUtils.checkUserIsSameUserForRequest(foundCheque.getUser().getId(), req, "delete");
        repo.deleteById(id);
        var res = dodService.deleteDODByChequeId(id, req);
        return ResponseEntity.ok("Deleted cheque and " + res);
    }

    private DebtOrDemandModel createDodFromCheque(ChequeModel preCheque) {
        return DebtOrDemandModel.builder()
                .nameOf(preCheque.getNameOf()).payTo(preCheque.getPayTo())
                .isDebt(preCheque.getIsDebt()).isCheckedOut(preCheque.getIsCheckedOut())
                .amount(preCheque.getAmount()).chequeId(preCheque.getId()).issuedAt(preCheque.getIssuedAt())
                .validTill(preCheque.getValidTill()).build();
    }

    private void checkId(Optional<ChequeModel> cheque) {
        cheque.map(ChequeModel::getId).ifPresent(id -> cheque.get().setId(null));
    }

}
