package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.dto.DebtOrDemandDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.DebtOrDemandMapper;
import ir.darkdeveloper.anbarinoo.model.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.service.Financial.DebtOrDemandService;
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
@RequestMapping("/api/user/financial/debt-demand")
@RequiredArgsConstructor
public class DebtOrDemandController {

    private final DebtOrDemandService service;
    private final DebtOrDemandMapper mapper;

    @PostMapping("/save/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<DebtOrDemandDto> saveDOD(@RequestBody DebtOrDemandModel dod, HttpServletRequest req) {
        return new ResponseEntity<>(mapper.dodToDto(service.saveDOD(Optional.ofNullable(dod), req)),
                HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<DebtOrDemandDto> updateDOD(
            @RequestBody DebtOrDemandModel dod, @PathVariable Long id,
            HttpServletRequest req) {
        return ResponseEntity.ok(mapper.dodToDto(service.updateDOD(Optional.of(dod), id, false, req)));
    }

    @GetMapping("/user/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<Page<DebtOrDemandDto>> getAllDODRecordsOfUser(
            @PathVariable("id") Long userId, HttpServletRequest request,
            Pageable pageable) {
        return ResponseEntity.ok(service.getAllDODRecordsOfUser(userId, request, pageable).map(mapper::dodToDto));
    }

    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<DebtOrDemandDto> getDOD(@PathVariable("id") Long dodId, HttpServletRequest request) {
        return ResponseEntity.ok(mapper.dodToDto(service.getDOD(dodId, request)));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_USER')")
    public ResponseEntity<?> deleteDOD(@PathVariable("id") Long dodId, HttpServletRequest request) {
        return ResponseEntity.ok(service.deleteDOD(dodId, request));
    }


}
