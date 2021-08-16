package ir.darkdeveloper.anbarinoo.service.Financial;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.ChequeRepo;
import javassist.NotFoundException;

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
            cheque.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(cheque);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel updateCheque(ChequeModel cheque, HttpServletRequest req) {
        try {
            if (cheque.getId() == null) throw new BadRequestException("Cheque id is null, can't update");
            checkUserIsSameUserForRequest(null, cheque.getId(), null, null, req, "update");
            return repo.save(cheque);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
//    @PreAuthorize("authentication.name.equals(@userService.getAdminUser().getUsername()) || #id != null")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteCheque(Long id, HttpServletRequest req) {
        try {
            checkUserIsSameUserForRequest(null, id, null, null, req, "delete");
            repo.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> getChequesByUserId(Long userId, HttpServletRequest req) {
        try {
            checkUserIsSameUserForRequest(userId, null, null, null, req, "fetch");
            return repo.findChequeModelsByUser_Id(userId);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel getCheque(Long id, HttpServletRequest req) {
        try {
            Optional<ChequeModel> cheque = repo.findById(id);
            if (cheque.isPresent()) {
                checkUserIsSameUserForRequest(null, null, cheque.get().getUser().getId(), null, req, "fetch");
                return cheque.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }
        return null;
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> findByPayToContains(String payTo, HttpServletRequest req) {
        try {
            var fetchedData = repo.findChequeModelByPayToContains(payTo);
            checkUserIsSameUserForRequest(null, null, null, fetchedData, req, "fetch");
            return fetchedData;
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }
    }

    private void checkUserIsSameUserForRequest(Long userId, Long chequeId, Long fetchedChequeUserId, List<ChequeModel> data, HttpServletRequest req, String operation) {
        if (userId == null) {
            if (chequeId != null) {
                var chequeFound = repo.findById(chequeId);
                if (chequeFound.isPresent())
                    userId = chequeFound.get().getUser().getId();
                else
                    throw new NoContentException("Cheque does not exist.");
            } else if (fetchedChequeUserId != null) {
                userId = fetchedChequeUserId;
            } else {
                if (data.size() != 0)
                    userId = data.get(0).getUser().getId();
                else
                    throw new NoContentException("Cheques do not exist.");
            }
        }

        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's cheques");
    }

}
