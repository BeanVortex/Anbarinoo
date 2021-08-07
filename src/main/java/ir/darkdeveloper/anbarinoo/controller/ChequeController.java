package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.service.ChequeService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/financial/cheque")
public class ChequeController {

    private final ChequeService service;

    @Autowired
    public ChequeController(ChequeService service) {
        this.service = service;
    }

    @GetMapping("/user/{id}/")
    public ResponseEntity<?> getChequesByUserId(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.getChequesByUserId(id, req));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getCheque(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.getCheque(id, req));
    }

    @PostMapping("/save/")
    public ResponseEntity<?> saveCheque(@RequestBody ChequeModel cheque, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.saveCheque(cheque, req));
    }

    @PostMapping("/update/")
    public ResponseEntity<?> updateCheque(@RequestBody ChequeModel cheque, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.updateCheque(cheque, req));
    }

    @PostMapping("/search/")
    public ResponseEntity<?> findByPayToContains(@RequestParam String payTo, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.findByPayToContains(payTo, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> updateCheque(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.deleteCheque(id, req));
    }

}
