package ir.darkdeveloper.anbarinoo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.service.CategoryService;

@RestController
@RequestMapping("/api/products/category")
public class CategoryController {

    private final CategoryService service;
    
    @Autowired
    public CategoryController(CategoryService service) {
        this.service = service;
    }



    @PostMapping("/save/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public ResponseEntity<CategoryModel> saveCategory(@ModelAttribute CategoryModel model) {
        return ResponseEntity.ok().body(service.saveCategory(model));
    }

    @PostMapping("/search/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public ResponseEntity<List<CategoryModel>> findByNameContains(@RequestParam String name) {
        return ResponseEntity.ok().body(service.findByNameContains(name));
    }

    @PostMapping("/update/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN', 'OP_ACCESS_USER')")
    public ResponseEntity<CategoryModel> updateCategory(@ModelAttribute CategoryModel model) {
        return ResponseEntity.ok().body(service.saveCategory(model));
    }

}
