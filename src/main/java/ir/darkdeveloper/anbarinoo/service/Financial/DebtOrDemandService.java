package ir.darkdeveloper.anbarinoo.service.Financial;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.Financial.DebtOrDemandRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@AllArgsConstructor
public class DebtOrDemandService {

    private final DebtOrDemandRepo repo;
    private final JwtUtils jwtUtils;

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public DebtOrDemandModel saveDOD(DebtOrDemandModel dod, HttpServletRequest req) {
        try {
            if (dod.getUser() != null && dod.getUser().getId() != null)
                throw new BadRequestException("Debt or Demand user id should be null");
            if (dod.getId() != null)
                throw new BadRequestException("Id must be null to save a Debt or Demand record");
            dod.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(dod);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Only id, nameOf, isDebt, payTo, amount, issuedAt, validTill will take effect
     */
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public DebtOrDemandModel updateDOD(DebtOrDemandModel dod, Long id, HttpServletRequest req) {
        try {
            if (dod.getId() != null)
                throw new BadRequestException("Body id must be null to update the Debt or Demand record");
            var foundDod = repo.findById(id);
            if (foundDod.isPresent()) {
                checkUserIsSameUserForRequest(foundDod.get().getUser().getId(), req, "update Debt or Demand record");
                foundDod.get().update(dod);
                return repo.save(foundDod.get());
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Debt or Demand record do not exist");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public Page<DebtOrDemandModel> getAllDODRecordsOfUser(Long userId, HttpServletRequest req, Pageable pageable) {

        try {
            checkUserIsSameUserForRequest(userId, req, "fetch Debt or Demand record");
            return repo.findAllByUserId(userId, pageable);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public DebtOrDemandModel getDOD(Long dodId, HttpServletRequest req) {
        try {
            var foundDod = repo.findById(dodId);
            if (foundDod.isPresent()) {
                checkUserIsSameUserForRequest(foundDod.get().getUser().getId(), req,
                        "fetch Debt or Demand record");
                return foundDod.get();
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (NoContentException n) {
            throw new NoContentException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        throw new NoContentException("Debt or Demand record do not exist");
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER')")
    public void deleteDOD(Long dodId, HttpServletRequest req) {
        try {
            var foundDod = repo.findById(dodId);
            if (foundDod.isPresent()) {
                checkUserIsSameUserForRequest(foundDod.get().getUser().getId(), req,
                        "delete Debt or Demand record");
                repo.deleteById(dodId);
            }
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (BadRequestException n) {
            throw new BadRequestException(n.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    private void checkUserIsSameUserForRequest(Long userId, HttpServletRequest req, String operation) {
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " of another user");
    }

}
