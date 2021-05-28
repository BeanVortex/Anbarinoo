package ir.darkdeveloper.anbarinoo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ProductModel saveProduct(@ModelAttribute ProductModel model, HttpServletRequest request) {
        return service.saveProduct(model, request);
    }

    @PostMapping("/search/")
    public Page<ProductModel> findByNameContains(@RequestParam String name, Pageable pageable) {
        return service.findByNameContains(name, pageable);
    }


    @PostMapping("/update/")
    public ProductModel updateProduct(@ModelAttribute ProductModel model, HttpServletRequest request) {
        return service.saveProduct(model, request);
    }

    /*@GetMapping({"/api/user/{id}", "/api/user/{id}/"})
    public List<ProductModel> findByUserId(@PathVariable("id") int id) {
        return service.findByUserID(id);
    }*/
}
