package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.CategoryRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static ir.darkdeveloper.anbarinoo.util.ExceptionUtils.exceptionHandlers;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo repo;
    private final JwtUtils jwtUtils;
    private final UserAuthUtils userAuthUtils;
    private static final String DATA_EXISTS_MESSAGE = "Category exists!";


    /**
     * Only save a category. children will be ignored
     */
    @Transactional
    public CategoryModel saveCategory(Optional<CategoryModel> model, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var category = model.orElseThrow(() -> new BadRequestException("Category can't be empty"));
            model.map(CategoryModel::getId).ifPresent(id -> category.setId(null));
            userAuthUtils.checkUserIsSameUserForRequest(null, req, "save a cat");
            category.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            return repo.save(category);
        }, DATA_EXISTS_MESSAGE);
    }

    /**
     * Saves a category under a parent (children of this sub cat will be ignored)
     */
    @Transactional
    public CategoryModel saveSubCategory(Optional<CategoryModel> model, Long parentId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var category = model.orElseThrow(() -> new BadRequestException("Category can't be empty"));
            var fetchedCategory = getCategoryById(parentId, req);
            userAuthUtils.checkUserIsSameUserForRequest(null, req, "save a sub cat");
            category.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
            category.setParent(fetchedCategory);
            fetchedCategory.addChild(category);
            return repo.save(category);
        }, DATA_EXISTS_MESSAGE);
    }

    public List<CategoryModel> getCategoriesByUser(HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var userId = jwtUtils.getUserId(req.getHeader("refresh_token"));
            return repo.findAllByUserId(userId);
        }, DATA_EXISTS_MESSAGE);
    }

    @Transactional
    public String deleteCategory(Long categoryId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            userAuthUtils.checkUserIsSameUserForRequest(null, req, "delete the cat");
            repo.deleteById(categoryId);
            return "Deleted the category";
        }, DATA_EXISTS_MESSAGE);
    }

    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public CategoryModel getCategoryById(Long categoryId, HttpServletRequest req) {
        return exceptionHandlers(() -> {
            var category = repo.findById(categoryId)
                    .orElseThrow(() -> new NoContentException("Category is not found"));
            userAuthUtils.checkUserIsSameUserForRequest(null, req, "fetch the cat");
            return category;
        }, DATA_EXISTS_MESSAGE);
    }


}
