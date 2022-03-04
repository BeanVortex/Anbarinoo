package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.ChequeRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ChequeService {

    private final ChequeRepo repo;
    private final JwtUtils jwtUtils;
    private final DebtOrDemandService dodService;

    /**
     * Saving a new cheque will save in debt or demand model too
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel saveCheque(Optional<ChequeModel> cheque, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            cheque.orElseThrow(() -> new BadRequestException("Cheque can't be null"));
            checkId(cheque);
            cheque.get().setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            var savedCheque = repo.save(cheque.get());
            var dod = createDodFromCheque(savedCheque);
            dodService.saveDOD(Optional.of(dod), req);
            return savedCheque;
        });
    }


    /**
     * Updating a cheque will update in debt or demand model too
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel updateCheque(Optional<ChequeModel> cheque, Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            cheque.orElseThrow(() -> new BadRequestException("Cheque can't be null"));
            checkId(cheque);
            var foundCheque = repo.findById(id)
                    .orElseThrow(() -> new NoContentException("Cheque does not exist"));
            checkUserIsSameUserForRequest(foundCheque.getUser().getId(), req, "update");
            foundCheque.update(cheque.get());
            var savedCheque = repo.save(foundCheque);
            var dod = createDodFromCheque(savedCheque);
            dodService.updateDODByChequeId(Optional.of(dod), req);
            return savedCheque;
        });
    }


    /**
     * Deleting a cheque will delete in debt or demand model too
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteCheque(Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundCheque = repo.findById(id)
                    .orElseThrow(() -> new NoContentException("Cheque does not exist"));
            checkUserIsSameUserForRequest(foundCheque.getUser().getId(), req, "delete");
            repo.deleteById(id);
            dodService.deleteDODByChequeId(id, req);
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> getChequesByUserId(Long userId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(userId, req, "fetch");
            return repo.findChequeModelsByUser_Id(userId);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel getCheque(Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var cheque = repo.findById(id)
                    .orElseThrow(() -> new NoContentException("Cheque does not exist"));
            checkUserIsSameUserForRequest(cheque.getUser().getId(), req, "fetch");
            return cheque;
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> findByPayToContains(String payTo, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var fetchedData = repo.findChequeModelByPayToContains(payTo);
            checkUserIsSameUserForRequest(fetchedData.get(0).getUser().getId(), req, "fetch");
            return fetchedData;
        });
    }


    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's cheques");
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

    private <T> T exceptionHandlers(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (DataException | BadRequestException e) {
            throw new BadRequestException(e.getLocalizedMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getLocalizedMessage());
        } catch (NoContentException e) {
            throw new NoContentException(e.getLocalizedMessage());
        } catch (DataIntegrityViolationException e) {
            throw new DataExistsException("Cheque exists!");
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }
}
