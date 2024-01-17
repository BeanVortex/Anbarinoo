package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.dto.ChequeDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.ChequeMapper;
import ir.darkdeveloper.anbarinoo.model.ChequeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ir.darkdeveloper.anbarinoo.service.Financial.ChequeService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/financial/cheque")
@RequiredArgsConstructor
public class ChequeController {

    private final ChequeService service;
    private final ChequeMapper mapper;

    @PostMapping("/save/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<ChequeDto> saveCheque(@RequestBody ChequeModel cheque, HttpServletRequest req) {
        return new ResponseEntity<>(mapper.chequeToDto(service.saveCheque(Optional.ofNullable(cheque), req)),
                HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<ChequeDto> updateCheque(@RequestBody ChequeModel cheque, @PathVariable Long id,
                                                  HttpServletRequest req) {
        return ResponseEntity.ok(mapper.chequeToDto(service.updateCheque(Optional.ofNullable(cheque), id, req)));
    }

    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<ChequeDto> getCheque(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(mapper.chequeToDto(service.getCheque(id, req)));
    }

    record ChequesList(List<ChequeDto> cheques) {
    }

    @GetMapping("/user/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<ChequesList> getChequesByUserId(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(new ChequesList(
                service.getChequesByUserId(id, req).stream().map(mapper::chequeToDto).toList()
        ));
    }

    @GetMapping("/search/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<ChequesList> findByPayToContains(@RequestParam String payTo, HttpServletRequest request) {
        return ResponseEntity.ok(new ChequesList(
                service.findByPayToContains(payTo, request).stream().map(mapper::chequeToDto).toList()
        ));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> deleteCheque(@PathVariable Long id, HttpServletRequest req) {
        return service.deleteCheque(id, req);
    }

}
