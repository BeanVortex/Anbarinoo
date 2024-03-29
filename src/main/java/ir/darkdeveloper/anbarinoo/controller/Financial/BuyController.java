package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.dto.BuyDto;
import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.BuySellMapper;
import ir.darkdeveloper.anbarinoo.model.BuyModel;
import ir.darkdeveloper.anbarinoo.service.Financial.BuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/category/products/buy")
@RequiredArgsConstructor
public class BuyController {

    private final BuyService service;
    private final BuySellMapper mapper;

    @PostMapping("/save/")
    @PreAuthorize("hasAuthority('OP_ADD_PRODUCT')")
    public ResponseEntity<BuyDto> saveBuy(@RequestBody BuyModel buy, HttpServletRequest req) {
        return new ResponseEntity<>(
                mapper.buyToDto(service.saveBuy(Optional.ofNullable(buy), false, req)),
                HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/")
    @PreAuthorize("hasAuthority('OP_EDIT_PRODUCT')")
    public ResponseEntity<BuyDto> updateBuy(@RequestBody BuyModel buy, @PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(mapper.buyToDto(service.updateBuy(Optional.ofNullable(buy), id, req)));
    }

    @GetMapping("/get-by-product/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<Page<BuyDto>> getAllBuyRecordsOfProduct(
            @PathVariable("id") Long productId,
            HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getAllBuyRecordsOfProduct(productId, req, pageable).map(mapper::buyToDto));
    }

    @GetMapping("/get-by-user/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<Page<BuyDto>> getAllBuyRecordsOfUser(
            @PathVariable("id") Long userId, HttpServletRequest req,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAllBuyRecordsOfUser(userId, req, pageable).map(mapper::buyToDto));
    }

    @PostMapping("/get-by-product/date/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<Page<BuyDto>> getAllBuyRecordsOfProductFromDateTo(
            @PathVariable("id") Long productId,
            @RequestBody FinancialDto financial,
            HttpServletRequest req, Pageable pageable) {
        var buys = service.getAllBuyRecordsOfProductFromDateTo(productId, Optional.ofNullable(financial),
                req, pageable).map(mapper::buyToDto);
        return ResponseEntity.ok(buys);
    }

    @PostMapping("/get-by-user/date/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<Page<BuyDto>> getAllBuyRecordsOfUserFromDateTo(
            @PathVariable("id") Long userId,
            @RequestBody FinancialDto financial,
            HttpServletRequest req, Pageable pageable) {
        var buys = service.getAllBuyRecordsOfUserFromDateTo(userId, Optional.ofNullable(financial),
                req, pageable).map(mapper::buyToDto);
        return ResponseEntity.ok(buys);
    }

    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<BuyDto> getBuy(@PathVariable("id") Long buyId, HttpServletRequest req) {
        return ResponseEntity.ok(mapper.buyToDto(service.getBuy(buyId, req)));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_DELETE_PRODUCT')")
    public ResponseEntity<String> deleteBuy(@PathVariable("id") Long buyId, HttpServletRequest req) {
        return service.deleteBuy(buyId, req);
    }

}
