package ir.darkdeveloper.anbarinoo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping("/save/")
    public ResponseEntity<ProductModel> saveProduct(@ModelAttribute ProductModel model, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveProduct(model, request));
    }

    @PostMapping("/search/")
    public ResponseEntity<Page<ProductModel>> findByNameContains(@RequestParam String name, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.findByNameContains(name, pageable, request));
    }

    @PostMapping("/update/")
    public ResponseEntity<ProductModel> updateProduct(@ModelAttribute ProductModel model, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateProduct(model, request));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getProduct(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getProduct(id, request));
    }

    @GetMapping("/user/{id}/")
    public ResponseEntity<?> getOneUserProducts(@PathVariable("id") Long userId, Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getOneUserProducts(userId, pageable, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        return service.deleteProduct(id, request);
    }


}
