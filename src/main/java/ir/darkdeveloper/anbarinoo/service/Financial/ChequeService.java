package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.ChequeRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class ChequeService {

    private final ChequeRepo repo;
    private final JwtUtils jwtUtils;

    @Autowired
    public ChequeService(ChequeRepo repo, JwtUtils jwtUtils) {
        this.repo = repo;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel saveCheque(ChequeModel cheque, HttpServletRequest req) {
        try {
            if (cheque.getId() != null) throw new ForbiddenException("Id of cheque must be null");
            cheque.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(cheque);
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel updateCheque(ChequeModel cheque, Long id, HttpServletRequest req) {
        try {
            if (cheque.getId() != null) throw new BadRequestException("Cheque id is not null, can't update");
            var foundCheque = repo.findById(id);
            if (foundCheque.isPresent()) {
                checkUserIsSameUserForRequest(foundCheque.get().getUser().getId(), req, "update");
                foundCheque.get().update(cheque);
                return repo.save(foundCheque.get());
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Cheque does not exist");
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteCheque(Long id, HttpServletRequest req) {
        try {
            var foundCheque = repo.findById(id);
            if (foundCheque.isPresent()) {
                checkUserIsSameUserForRequest(foundCheque.get().getUser().getId(), req, "delete");
                repo.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Cheque does not exist");
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
        throw new NoContentException("Cheque does not exist");
    }

    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's cheques");
    }

}
