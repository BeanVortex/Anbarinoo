package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.dto.CategoryDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.CategoryMapper;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;
    private final CategoryMapper mapper;


    @PostMapping("/save/")
    @PreAuthorize("hasAuthority('OP_ADD_PRODUCT')")
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody CategoryModel model,
                                                    HttpServletRequest request) {
        var savedCat = service.saveCategory(Optional.ofNullable(model), request);
        return new ResponseEntity<>(mapper.categoryToDto(savedCat), HttpStatus.CREATED);
    }

    @PostMapping("/sub-category/save/{parentId}/")
    @PreAuthorize("hasAuthority('OP_ADD_PRODUCT')")
    public ResponseEntity<CategoryDto> saveSubCategory(@RequestBody CategoryModel model,
                                                       @PathVariable Long parentId,
                                                       HttpServletRequest request) {
        var savedSubCat = service.saveSubCategory(Optional.ofNullable(model), parentId, request);
        return ResponseEntity.ok(mapper.categoryToDto(savedSubCat));
    }

    record CategoriesDto(List<CategoryDto> categories) {
    }

    @GetMapping("/user/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<CategoriesDto> getCategoriesByUser(HttpServletRequest req) {
        return ResponseEntity.ok(new CategoriesDto(
                service.getCategoriesByUser(req).stream().map(mapper::categoryToDto).toList()
        ));
    }

    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(mapper.categoryToDto(service.getCategoryById(id, request)));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_DELETE_PRODUCT')")
    public ResponseEntity<String> deleteCategoryById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(service.deleteCategory(id, request));
    }

}
