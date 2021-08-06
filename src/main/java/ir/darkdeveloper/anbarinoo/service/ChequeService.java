package ir.darkdeveloper.anbarinoo.service;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.repository.ChequeRepo;
import ir.darkdeveloper.anbarinoo.util.UserUtils;
import javassist.NotFoundException;

@Service
public class ChequeService {

    private final ChequeRepo repo;
    private final UserUtils userUtils;

    @Autowired
    public ChequeService(ChequeRepo repo, UserUtils userUtils) {
        this.repo = repo;
        this.userUtils = userUtils;
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<ChequeModel> getChequesByUserId(Long userId, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            return repo.findChequeModelsByUser_Id(userId);
        } catch (Exception e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel saveCheque(ChequeModel cheque, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            return repo.save(cheque);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel updateCheque(ChequeModel cheque, HttpServletRequest req) {
        try {
            if (cheque.getId() == null) throw new NotFoundException("Cheque id is null, can't update");
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            return repo.save(cheque);
        } catch (NotFoundException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("authentication.name.equals(@userService.getAdminUser().getUsername()) || #id != null")
    public ResponseEntity<?> deleteCheque(Long id, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            repo.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ChequeModel getCheque(Long id, HttpServletRequest req) {
        try {
            userUtils.checkCurrentUserIsTheSameAuthed(req);
            Optional<ChequeModel> cheque = repo.findById(id);
            if (cheque.isPresent())
                return cheque.get();
        } catch (Exception e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }
        throw new NoContentException("Data you are looking for is not found");
    }

}
