package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.CategoryRepo;
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
public class CategoryService {

    private final CategoryRepo repo;
    private final JwtUtils jwtUtils;

    @Autowired
    public CategoryService(CategoryRepo repo, JwtUtils jwtUtils) {
        this.repo = repo;
        this.jwtUtils = jwtUtils;
    }


    /**
     * Only save a category. children will ignored
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public CategoryModel saveCategory(CategoryModel model, HttpServletRequest req) {
        try {
            if (model.getId() != null) throw new ForbiddenException("Id of category must be null to save a category");
            model.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(model);
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Saves a category under a parent (children of this sub cat will ignored)
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public CategoryModel saveSubCategory(CategoryModel model, Long parentId, HttpServletRequest req) {
        try {
            var fetchedCategory = getCategoryById(parentId, req);
            model.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            model.setParent(fetchedCategory);
            fetchedCategory.addChild(model);
            return repo.save(model);
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public List<CategoryModel> getCategoriesByUser(HttpServletRequest req) {
        try {
//            checkUserIsSameUserForRequest(userId, null, req, "fetch");
            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.findAllByUserId(userId);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteCategory(Long categoryId, HttpServletRequest req) {

        try {
            checkUserIsSameUserForRequest(null, categoryId, req, "delete");
            repo.deleteById(categoryId);
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (DataException s) {
            throw new BadRequestException(s.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public CategoryModel getCategoryById(Long id, HttpServletRequest req) {
        try {
            checkUserIsSameUserForRequest(null, id, req, "fetch");
            return repo.findById(id).orElse(null);
        } catch (NoContentException n) {
            throw new NoContentException(n.getLocalizedMessage());
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

    private void checkUserIsSameUserForRequest(Long userId, Long categoryId, HttpServletRequest req, String operation) {
        if (userId == null) {
            var catFound = repo.findById(categoryId);
            if (catFound.isPresent())
                userId = catFound.get().getUser().getId();
            else
                throw new NoContentException("Category does not exist.");
        }

        Long id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's categories");
    }

}
