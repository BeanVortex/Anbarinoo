package ir.darkdeveloper.anbarinoo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok().body(service.saveProduct(model, request));
    }
}
