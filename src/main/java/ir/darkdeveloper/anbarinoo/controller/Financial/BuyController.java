package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/category/products/buy")
public class BuyController {

    private final BuyService service;

    @Autowired
    public BuyController(BuyService service) {
        this.service = service;
    }


    @PostMapping("/save/")
    public ResponseEntity<?> saveBuy(@RequestBody BuyModel buy, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.saveBuy(buy, request));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateBuy(@RequestBody BuyModel buy, @PathVariable Long id,
                                       HttpServletRequest request) {
        return ResponseEntity.ok().body(service.updateBuy(buy, id, request));
    }

    @GetMapping("/get-by-product/{id}/")
    public ResponseEntity<?> getAllBuyRecordsOfProduct(@PathVariable("id") Long productId,
                                                        HttpServletRequest request, Pageable pageable) {
        return ResponseEntity.ok().body(service.getAllBuyRecordsOfProduct(productId, request, pageable));
    }

    @GetMapping("/get-by-user/{id}/")
    public ResponseEntity<?> getAllBuyRecordsOfUser(@PathVariable("id") Long userId, HttpServletRequest request,
                                                     Pageable pageable) {
        return ResponseEntity.ok().body(service.getAllBuyRecordsOfUser(userId, request, pageable));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getBuy(@PathVariable("id") Long buyId, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.getBuy(buyId, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteBuy(@PathVariable("id") Long buyId, HttpServletRequest request) {
        service.deleteBuy(buyId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
