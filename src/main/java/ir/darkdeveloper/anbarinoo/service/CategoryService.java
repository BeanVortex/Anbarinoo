package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.exception.NoContentException;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.CategoryRepo;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo repo;
    private final JwtUtils jwtUtils;
    private final UserAuthUtils userAuthUtils;


    /**
     * Only save a category. children will be ignored
     */
    public CategoryModel saveCategory(Optional<CategoryModel> model, HttpServletRequest req) {
        var category = model.orElseThrow(() -> new BadRequestException("Category can't be empty"));
        model.map(CategoryModel::getId).ifPresent(id -> category.setId(null));
        var userId = model.map(CategoryModel::getUser).map(UserModel::getId)
                .orElseThrow(() -> new BadRequestException("User id can't be null in category"));
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "save a cat");
        category.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
        return repo.save(category);
    }

    /**
     * Saves a category under a parent (children of this sub cat will be ignored)
     */
    @Transactional
    public CategoryModel saveSubCategory(Optional<CategoryModel> model, Long parentId, HttpServletRequest req) {
        var category = model.orElseThrow(() -> new BadRequestException("Category can't be empty"));
        var fetchedCategory = getCategoryById(parentId, req);
        userAuthUtils.checkUserIsSameUserForRequest(fetchedCategory.getUser().getId(), req, "save a sub category");
        category.setUser(new UserModel(jwtUtils.getUserId(req.getHeader("refresh_token"))));
        category.setParent(fetchedCategory);
        return repo.save(category);
    }

    public List<CategoryModel> getCategoriesByUser(HttpServletRequest req, Long userId) {
        userAuthUtils.checkUserIsSameUserForRequest(userId, req, "access the categories");
        return repo.findAllByUserId(userId);
    }

    @Transactional
    public String deleteCategory(Long categoryId, HttpServletRequest req) {
        userAuthUtils.checkUserIsSameUserForRequest(null, req, "delete the category");
        repo.deleteById(categoryId);
        return "Deleted the category";
    }

    public CategoryModel getCategoryById(Long categoryId, HttpServletRequest req) {
        var category = repo.findById(categoryId)
                .orElseThrow(() -> new NoContentException("Category is not found"));
        userAuthUtils.checkUserIsSameUserForRequest(category.getUser().getId(), req, "fetch the category");
        return category;
    }


}
