package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.DebtOrDemandRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DebtOrDemandService {

    private final DebtOrDemandRepo repo;
    private final JwtUtils jwtUtils;

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public DebtOrDemandModel saveDOD(Optional<DebtOrDemandModel> dodOpt, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var dod = checkDODAndGet(dodOpt);
            dod.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(dod);
        });
    }

    /**
     * Only id, nameOf, isDebt, payTo, amount, issuedAt, validTill will take effect
     */
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public DebtOrDemandModel updateDOD(Optional<DebtOrDemandModel> dodOpt, Long id, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var dod = checkDODAndGet(dodOpt);
            var foundDod = repo.findById(id)
                    .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));
            checkUserIsSameUserForRequest(foundDod.getUser().getId(), req, "update Debt or Demand record");
            foundDod.update(dod);
            return repo.save(foundDod);
        });
    }


    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public void updateDODByChequeId(Optional<DebtOrDemandModel> dodOpt, HttpServletRequest req) {
        exceptionHandlers(() -> {
            var dod = checkDODAndGet(dodOpt);
            var foundDod = repo.findByChequeId(dod.getChequeId()).orElseThrow(() -> new NoContentException("Debt or Demand record do not exist"));
            checkUserIsSameUserForRequest(foundDod.getUser().getId(), req, "update Debt or Demand record");
            foundDod.update(dod);
            repo.save(foundDod);
            return null;
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public void deleteDODByChequeId(Long chequeId, HttpServletRequest req) {
        exceptionHandlers(() -> {
            var foundDod = repo.findByChequeId(chequeId)
                    .orElseThrow(() -> new InternalServerException("Debt or Demand can't be found with cheque"));
            checkUserIsSameUserForRequest(foundDod.getUser().getId(), req,
                    "delete Debt or Demand record");
            repo.deleteById(foundDod.getId());
            return null;
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<DebtOrDemandModel> getAllDODRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(userId, req, "fetch Debt or Demand record");
            return repo.findAllByUserId(userId, pageable);
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public DebtOrDemandModel getDOD(Long dodId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundDod = repo.findById(dodId)
                    .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));
            checkUserIsSameUserForRequest(foundDod.getUser().getId(), req, "fetch Debt or Demand record");
            return foundDod;
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> deleteDOD(Long dodId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var foundDod = repo.findById(dodId)
                    .orElseThrow(() -> new NoContentException("Debt or Demand record does not exist"));
            checkUserIsSameUserForRequest(foundDod.getUser().getId(), req,
                    "delete Debt or Demand record");
            repo.deleteById(dodId);
            return ResponseEntity.ok("Deleted the Debt or Demand");
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<DebtOrDemandModel> getDODFromToDate(Long userId, Boolean isDebt, Boolean isCheckedOut,
                                                    LocalDateTime from, LocalDateTime to, HttpServletRequest req,
                                                    Pageable pageable) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(userId, req, "fetch");
            return repo.findAllByUserIdAndIsDebtAndIsCheckedOutAndCreatedAtAfterAndCreatedAtBefore(
                    userId, isDebt, isCheckedOut, from, to, pageable);
        });
    }


    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " of another user");
    }

    private DebtOrDemandModel checkDODAndGet(Optional<DebtOrDemandModel> dodOpt) {
        dodOpt.map(DebtOrDemandModel::getUser).ifPresent(userId -> dodOpt.get().setUser(null));
        dodOpt.map(DebtOrDemandModel::getId).ifPresent(i -> dodOpt.get().setId(null));
        return dodOpt
                .orElseThrow(() -> new BadRequestException("Debt Or Demand can't be null"));
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
            throw new DataExistsException("Debt or Demand exists!");
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }
}
