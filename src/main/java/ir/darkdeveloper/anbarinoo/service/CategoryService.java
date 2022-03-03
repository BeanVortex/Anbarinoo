package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.*;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.CategoryRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo repo;
    private final JwtUtils jwtUtils;


    /**
     * Only save a category. children will be ignored
     */
    @Transactional
    public CategoryModel saveCategory(CategoryModel model, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            if (model.getId() != null) model.setId(null);
            model.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(model);
        });
    }

    /**
     * Saves a category under a parent (children of this sub cat will be ignored)
     */
    @Transactional
    public CategoryModel saveSubCategory(CategoryModel model, Long parentId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var fetchedCategory = getCategoryById(parentId, req);
            model.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            model.setParent(fetchedCategory);
            fetchedCategory.addChild(model);
            return repo.save(model);
        });
    }

    public List<CategoryModel> getCategoriesByUser(HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.findAllByUserId(userId);
        });
    }

    @Transactional
    public ResponseEntity<?> deleteCategory(Long categoryId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(categoryId, req, "delete");
            repo.deleteById(categoryId);
            return ResponseEntity.ok("Deleted the category");
        });
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public CategoryModel getCategoryById(Long categoryId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            checkUserIsSameUserForRequest(categoryId, req, "fetch");
            return repo.findById(categoryId).orElseThrow(() -> new NoContentException("Category is not found"));
        });
    }

    private void checkUserIsSameUserForRequest(Long categoryId, HttpServletRequest req, String operation) {
        var foundCategory = repo.findById(categoryId)
                .orElseThrow(() -> new NoContentException("Category does not exist"));
        var userId = foundCategory.getUser().getId();
        var id = jwtUtils.getUserId(req.getHeader("refresh_token"));
        if (!userId.equals(id))
            throw new ForbiddenException("You can't " + operation + " another user's categories");
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
            throw new DataExistsException("Category exists!");
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

}
