package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.service.Financial.FinancialService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/financial/")
@AllArgsConstructor
public class FinancialController {

    private final FinancialService service;

    @GetMapping("/costs/")
    public ResponseEntity<?> getCosts(FinancialModel financial, HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getCosts(financial, req, pageable));
    }

    @GetMapping("/incomes/")
    public ResponseEntity<?> getIncomes(FinancialModel financial, HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getIncomes(financial, req, pageable));
    }
}
