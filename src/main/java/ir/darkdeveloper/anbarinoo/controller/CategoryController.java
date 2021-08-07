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
    public ResponseEntity<CategoryModel> saveCategory(@RequestBody CategoryModel model) {
        return ResponseEntity.ok().body(service.saveCategory(model));
    }

    @PostMapping("/search/")
    public ResponseEntity<List<CategoryModel>> findByNameContains(@RequestParam String name) {
        return ResponseEntity.ok().body(service.findByNameContains(name));
    }

    @PostMapping("/update/")
    public ResponseEntity<CategoryModel> updateCategory(@ModelAttribute CategoryModel model) {
        return ResponseEntity.ok().body(service.saveCategory(model));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getCategoryById(id, request));
    }

}
