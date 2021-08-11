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
    public ResponseEntity<?> saveProduct(@ModelAttribute ProductModel model, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveProduct(model, request));
    }

    @PostMapping("/search/")
    public ResponseEntity<?> findByNameContains(@RequestParam String name, Pageable pageable,
                                                HttpServletRequest request) {
        return ResponseEntity.ok().body(service.findByNameContains(name, pageable, request));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateProduct(@ModelAttribute ProductModel model, @PathVariable("id") Long productId,
                                           HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateProduct(model, productId, request));
    }

    @PutMapping("/update/images/{id}/")
    public ResponseEntity<?> updateProductImages(@ModelAttribute ProductModel model, @PathVariable("id") Long productId,
                                                 HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateProductImages(model, productId, request));
    }

    @PutMapping("/update/delete/images/{id}/")
    public ResponseEntity<?> updateDeleteProductImages(@ModelAttribute ProductModel model, @PathVariable("id") Long productId,
                                                       HttpServletRequest request) {
        return service.updateDeleteProductImages(model, productId, request);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getProduct(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getProduct(id, request));
    }

    @GetMapping("/user/{id}/")
    public ResponseEntity<?> getOneUserProducts(@PathVariable("id") Long userId, Pageable pageable,
                                                HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getOneUserProducts(userId, pageable, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        return service.deleteProduct(id, request);
    }


}
