package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.model.FinancialModel;
import ir.darkdeveloper.anbarinoo.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/financial")
public class FinancialController {

    private final FinancialService service;

    @Autowired
    public FinancialController(FinancialService service) {
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<?> saveFinancial(@RequestBody FinancialModel financial, HttpServletRequest req) {
        return ResponseEntity.ok(service.saveFinancial(financial, req));
    }

    @PutMapping("/{id}/")
    public ResponseEntity<?> updateFinancialById(@RequestBody FinancialModel financial, @PathVariable Long id,
                                                 HttpServletRequest req) {
        return ResponseEntity.ok(service.updateFinancial(financial, id, req));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getFinancialById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(service.getFinancial(id, req));
    }


}
