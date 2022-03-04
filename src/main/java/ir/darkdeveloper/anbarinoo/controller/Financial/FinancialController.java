package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.dto.FinancialDto;
import ir.darkdeveloper.anbarinoo.service.Financial.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/financial/")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService service;

    @PostMapping("/costs/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> getCosts(@RequestBody FinancialDto financial,
                                      HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getCosts(Optional.ofNullable(financial), req, pageable));
    }

    @PostMapping("/incomes/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> getIncomes(@RequestBody FinancialDto financial,
                                        HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getIncomes(Optional.ofNullable(financial), req, pageable));
    }

    @PostMapping("/profit-loss/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> getProfitOrLoss(@RequestBody FinancialDto financial,
                                             HttpServletRequest req, Pageable pageable) {
        return ResponseEntity.ok(service.getProfitOrLoss(Optional.ofNullable(financial), req, pageable));
    }
}
