package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import ir.darkdeveloper.anbarinoo.service.Financial.SellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/category/products/sell")
public class SellController {

    private final SellService service;

    @Autowired
    public SellController(SellService service) {
        this.service = service;
    }


    @PostMapping("/save/")
    public ResponseEntity<?> saveSell(@RequestBody SellModel sell, HttpServletRequest request) {
        return ResponseEntity.ok(service.saveSell(Optional.ofNullable(sell), request));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateSell(@RequestBody SellModel sell, @PathVariable Long id,
                                        HttpServletRequest request) {
        return ResponseEntity.ok(service.updateSell(Optional.ofNullable(sell), id, request));
    }

    @GetMapping("/get-by-product/{id}/")
    public ResponseEntity<?> getAllSellRecordsOfProduct(@PathVariable("id") Long productId,
                                                        HttpServletRequest request, Pageable pageable) {
        return ResponseEntity.ok(service.getAllSellRecordsOfProduct(productId, request, pageable));
    }

    @GetMapping("/get-by-user/{id}/")
    public ResponseEntity<?> getAllSellRecordsOfUser(@PathVariable("id") Long userId, HttpServletRequest request,
                                                     Pageable pageable) {
        return ResponseEntity.ok(service.getAllSellRecordsOfUser(userId, request, pageable));
    }

    @PostMapping("/get-by-product/date/{id}/")
    public ResponseEntity<?> getAllSellRecordsOfProductFromDateTo(@PathVariable("id") Long productId,
                                                                  @RequestBody FinancialModel financial,
                                                                  HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getAllSellRecordsOfProductFromDateTo(productId,
                Optional.ofNullable(financial), req, pageable));
    }

    @PostMapping("/get-by-user/date/{id}/")
    public ResponseEntity<?> getAllSellRecordsOfUserFromDateTo(@PathVariable("id") Long userId,
                                                               @RequestBody FinancialModel financial,
                                                               HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getAllSellRecordsOfUserFromDateTo(userId,
                Optional.ofNullable(financial), req, pageable));
    }


    @GetMapping("/{id}/")
    public ResponseEntity<?> getSell(@PathVariable("id") Long sellId, HttpServletRequest request) {
        return ResponseEntity.ok(service.getSell(sellId, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteSell(@PathVariable("id") Long sellId, HttpServletRequest request) {
        service.deleteSell(sellId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
