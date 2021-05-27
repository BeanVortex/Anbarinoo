package ir.darkdeveloper.anbarinoo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping({"/products/save", "/products/save/"})
    @PreAuthorize("#model.user.email.equals(authentication.name) ||" +
            "#model.user.email.equals('DarkDeveloper')")
    public ProductModel saveProduct(@ModelAttribute ProductModel model) {
        return service.saveProduct(model);
    }

    @PostMapping({"/products/search/", "/products/search"})
    public Page<ProductModel> findByNameContains(@RequestParam String name, Pageable pageable) {
        return service.findByNameContains(name, pageable);
    }


    @PostMapping({"/products/update", "/products/update/"})
    @PreAuthorize("#model.user.email.equals(authentication.name) ||" +
            "#model.user.email.equals('DarkDeveloper')")
    public ProductModel updateProduct(@ModelAttribute ProductModel model) {
        return service.saveProduct(model);
    }

    /*@GetMapping({"/api/user/{id}", "/api/user/{id}/"})
    public List<ProductModel> findByUserId(@PathVariable("id") int id) {
        return service.findByUserID(id);
    }*/
}
