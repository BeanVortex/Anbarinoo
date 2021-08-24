package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.Financial.SellsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/category/products/sell")
public class SellsController {

    private final SellsService service;

    @Autowired
    public SellsController(SellsService service) {
        this.service = service;
    }


    @PostMapping("/save/")
    public ResponseEntity<?> saveSell(@RequestBody SellsModel sell, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveSell(sell, request));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateSell(@RequestBody SellsModel sell, @PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateSell(sell, id, request));
    }

    @GetMapping("/get-by-product/{id}/")
    public ResponseEntity<?> getAllSellRecordsOfProduct(@PathVariable("id") Long productId, HttpServletRequest request,
                                                        Pageable pageable) {
        return ResponseEntity.ok().body(service.getAllSellRecordsOfProduct(productId, request, pageable));
    }

    @GetMapping("/get-by-user/{id}/")
    public ResponseEntity<?> getAllSellRecordsOfUser(@PathVariable("id") Long userId, HttpServletRequest request,
                                                     Pageable pageable) {
        return ResponseEntity.ok().body(service.getAllSellRecordsOfUser(userId, request, pageable));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getSell(@PathVariable("id") Long sellId, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getSell(sellId, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteSell(@PathVariable("id") Long sellId, HttpServletRequest request) {
        service.deleteSell(sellId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
