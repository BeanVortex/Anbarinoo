package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.ChequeRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.hibernate.exception.DataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class ChequeService {

    private final ChequeRepo repo;
    private final JwtUtils jwtUtils;
    private final DebtOrDemandService dodService;

    /**
     * Saving a new cheque will save in debt or demand model too
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel saveCheque(ChequeModel cheque, HttpServletRequest req) {
        try {
            if (cheque.getId() != null) throw new ForbiddenException("Id of cheque must be null");
            cheque.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            var savedCheque = repo.save(cheque);
            var dod = new DebtOrDemandModel(null, savedCheque.getNameOf(), savedCheque.getPayTo(),
                    savedCheque.getIsDebt(), savedCheque.getIsCheckedOut(), savedCheque.getAmount(),
                    savedCheque.getId(), null, savedCheque.getIssuedAt(), savedCheque.getValidTill(),
                    null, null);
            dodService.saveDOD(dod, req);
            return savedCheque;
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getLocalizedMessage());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Updating a cheque will update in debt or demand model too
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel updateCheque(ChequeModel cheque, Long id, HttpServletRequest req) {
        try {
            if (cheque.getId() != null) throw new BadRequestException("Cheque id is not null, can't update");
            var foundCheque = repo.findById(id);
            if (foundCheque.isPresent()) {
                checkUserIsSameUserForRequest(foundCheque.get().getUser().getId(), req, "update");
                foundCheque.get().update(cheque);
                var savedCheque = repo.save(foundCheque.get());
                var dod = new DebtOrDemandModel(null, savedCheque.getNameOf(), savedCheque.getPayTo(),
                        savedCheque.getIsDebt(), savedCheque.getIsCheckedOut(), savedCheque.getAmount(), savedCheque.getId(), null,
                        savedCheque.getIssuedAt(), savedCheque.getValidTill(), null, null);
                dodService.updateDODByChequeId(dod, req);
                return savedCheque;
            }
            throw new NoContentException("Cheque does not exist");
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (NoContentException n) {
            throw new NoContentException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Deleting a cheque will delete in debt or demand model too
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteCheque(Long id, HttpServletRequest req) {
        try {
            var foundCheque = repo.findById(id);
            if (foundCheque.isPresent()) {
                checkUserIsSameUserForRequest(foundCheque.get().getUser().getId(), req, "delete");
                repo.deleteById(id);
                dodService.deleteDODByChequeId(id, req);
                return new ResponseEntity<>(HttpStatus.OK);
            } else throw new NoContentException("Cheque does not exist");
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (NoContentException n) {
            throw new NoContentException(n.getLocalizedMessage());
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> getChequesByUserId(Long userId, HttpServletRequest req) {
        try {
            checkUserIsSameUserForRequest(userId, req, "fetch");
            return repo.findChequeModelsByUser_Id(userId);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel getCheque(Long id, HttpServletRequest req) {
        try {
            var cheque = repo.findById(id);
            if (cheque.isPresent()) {
                checkUserIsSameUserForRequest(cheque.get().getUser().getId(), req, "fetch");
                return cheque.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        return null;
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> findByPayToContains(String payTo, HttpServletRequest req) {
        try {
            var fetchedData = repo.findChequeModelByPayToContains(payTo);
            if (fetchedData.size() > 0) {
                checkUserIsSameUserForRequest(fetchedData.get(0).getUser().getId(), req, "fetch");
                return fetchedData;
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Cheques do not exist");
    }

    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's cheques");
    }

}
