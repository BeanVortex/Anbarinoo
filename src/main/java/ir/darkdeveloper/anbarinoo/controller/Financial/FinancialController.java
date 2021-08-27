package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.FinancialModel;
import ir.darkdeveloper.anbarinoo.service.Financial.FinancialService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/financial/")
@AllArgsConstructor
public class FinancialController {

    private final FinancialService service;

    @PostMapping("/costs/")
    public ResponseEntity<?> getCosts(@RequestBody FinancialModel financial,
                                      HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getCosts(financial, req, pageable));
    }

    @PostMapping("/incomes/")
    public ResponseEntity<?> getIncomes(@RequestBody FinancialModel financial,
                                        HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getIncomes(financial, req, pageable));
    }
}
