package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/category/products")
public class ProductController {
    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping("/save/")
    public ResponseEntity<?> saveProduct(@ModelAttribute ProductModel product, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveProduct(product, request));
    }

    @GetMapping("/search/")
    public ResponseEntity<?> findByNameContains(@RequestParam String name, Pageable pageable,
                                                HttpServletRequest request) {
        return ResponseEntity.ok().body(service.findByNameContains(name, pageable, request));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateProduct(@RequestBody ProductModel product, @PathVariable("id") Long productId,
                                           HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateProduct(Optional.ofNullable(product), productId, request));
    }

    @PutMapping("/update/images/{id}/")
    public ResponseEntity<?> updateProductImages(@ModelAttribute ProductModel product, @PathVariable("id") Long productId,
                                                 HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateProductImages(Optional.ofNullable(product), productId, request));
    }

    @PutMapping("/update/delete-images/{id}/")
    public ResponseEntity<?> updateDeleteProductImages(@RequestBody ProductModel product, @PathVariable("id") Long productId,
                                                       HttpServletRequest request) {
        return service.updateDeleteProductImages(Optional.ofNullable(product), productId, request);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getProduct(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getProduct(id, request));
    }

    @GetMapping("/user/")
    public ResponseEntity<?> getAllProducts(Pageable pageable,
                                            HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getAllProducts(pageable, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        return service.deleteProduct(id, request);
    }


}
