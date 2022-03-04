package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/category/products/buy")
@RequiredArgsConstructor
public class BuyController {

    private final BuyService service;

    @PostMapping("/save/")
    public ResponseEntity<?> saveBuy(@RequestBody BuyModel buy, HttpServletRequest req) {
        return ResponseEntity.ok(service.saveBuy(Optional.ofNullable(buy), false, req));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateBuy(@RequestBody BuyModel buy, @PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(service.updateBuy(Optional.ofNullable(buy), id, req));
    }

    @GetMapping("/get-by-product/{id}/")
    public ResponseEntity<?> getAllBuyRecordsOfProduct(@PathVariable("id") Long productId,
                                                       HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getAllBuyRecordsOfProduct(productId, req, pageable));
    }

    @GetMapping("/get-by-user/{id}/")
    public ResponseEntity<?> getAllBuyRecordsOfUser(@PathVariable("id") Long userId, HttpServletRequest req,
                                                    Pageable pageable) {
        return ResponseEntity.ok(service.getAllBuyRecordsOfUser(userId, req, pageable));
    }

    @PostMapping("/get-by-product/date/{id}/")
    public ResponseEntity<?> getAllBuyRecordsOfProductFromDateTo(@PathVariable("id") Long productId,
                                                                 @RequestBody FinancialModel financial,
                                                                 HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getAllBuyRecordsOfProductFromDateTo(productId, Optional.ofNullable(financial),
                req, pageable));
    }

    @PostMapping("/get-by-user/date/{id}/")
    public ResponseEntity<?> getAllBuyRecordsOfUserFromDateTo(@PathVariable("id") Long userId,
                                                              @RequestBody FinancialModel financial,
                                                              HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getAllBuyRecordsOfUserFromDateTo(userId, Optional.ofNullable(financial), req, pageable));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getBuy(@PathVariable("id") Long buyId, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.getBuy(buyId, req));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteBuy(@PathVariable("id") Long buyId, HttpServletRequest req) {
        return service.deleteBuy(buyId, req);
    }

}
