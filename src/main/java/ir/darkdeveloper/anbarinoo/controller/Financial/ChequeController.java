package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.service.Financial.ChequeService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/financial/cheque")
@RequiredArgsConstructor
public class ChequeController {

    private final ChequeService service;

    @PostMapping("/save/")
    public ResponseEntity<?> saveCheque(@RequestBody ChequeModel cheque, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.saveCheque(Optional.ofNullable(cheque), req));
    }

    @PostMapping("/update/{id}/")
    public ResponseEntity<?> updateCheque(@RequestBody ChequeModel cheque, @PathVariable Long id,
                                          HttpServletRequest req) {
        return ResponseEntity.ok().body(service.updateCheque(Optional.ofNullable(cheque), id, req));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getCheque(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.getCheque(id, req));
    }

    @GetMapping("/user/{id}/")
    public ResponseEntity<?> getChequesByUserId(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.getChequesByUserId(id, req));
    }

    @GetMapping("/search/")
    public ResponseEntity<?> findByPayToContains(@RequestParam String payTo, HttpServletRequest request) {
        return ResponseEntity.ok().body(service.findByPayToContains(payTo, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteCheque(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok().body(service.deleteCheque(id, req));
    }

}
