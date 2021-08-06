package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> saveCheque(@RequestBody ChequeModel cheque) {
        return ResponseEntity.ok().body(service.saveCheque(cheque));
    }

    @PostMapping("/update/")
    public ResponseEntity<?> updateCheque(@RequestBody ChequeModel cheque, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.updateCheque(cheque, req));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> updateCheque(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.deleteCheque(id, req));
    }

}