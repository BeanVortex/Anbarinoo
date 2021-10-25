package ir.darkdeveloper.anbarinoo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;

@RestController
@RequestMapping("/api/category")
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
        return ResponseEntity.ok().body(service.saveSubCategory(model, parentId, request));
    }


    @GetMapping("/user/")
    public ResponseEntity<?> getCategoriesByUserId( HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getCategoriesByUser(request));
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
