package ir.darkdeveloper.anbarinoo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products/category")
public class CategoryController {

    private final CategoryService service;

    @Autowired
    public CategoryController(CategoryService service) {
        this.service = service;
    }


    @PostMapping("/save/")
    public ResponseEntity<?> saveCategory(@RequestBody CategoryModel model, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveCategory(model, request));
    }

    @PostMapping("/sub-category/save/{parentId}/")
    public ResponseEntity<?> saveSubCategory(@RequestBody CategoryModel model, @PathVariable Long parentId, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveSubCategory(model, parentId,request));
    }


    @GetMapping("/user/{id}/")
    public ResponseEntity<?> getCategoriesByUserId(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getCategoriesByUserId(id, request));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getCategoryById(id, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteCategoryById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.deleteCategory(id, request));
    }

}
