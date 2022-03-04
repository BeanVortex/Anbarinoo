package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.service.Financial.DebtOrDemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/financial/debt-demand")
@RequiredArgsConstructor
public class DebtOrDemandController {

    private final DebtOrDemandService service;

    @PostMapping("/save/")
    public ResponseEntity<?> saveDOD(@RequestBody DebtOrDemandModel dod, HttpServletRequest req) {
        return ResponseEntity.ok(service.saveDOD(Optional.ofNullable(dod), req));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateDOD(@RequestBody DebtOrDemandModel dod, @PathVariable Long id,
                                       HttpServletRequest req) {
        return ResponseEntity.ok(service.updateDOD(Optional.of(dod), id, req));
    }

    @GetMapping("/get-by-user/{id}/")
    public ResponseEntity<?> getAllDODRecordsOfUser(@PathVariable("id") Long userId, HttpServletRequest request,
                                                     Pageable pageable) {
        return ResponseEntity.ok(service.getAllDODRecordsOfUser(userId, request, pageable));
    }

    @GetMapping("/{id}/")
    public ResponseEntity<?> getDOD(@PathVariable("id") Long dodId, HttpServletRequest request) {
        return ResponseEntity.ok(service.getDOD(dodId, request));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteDOD(@PathVariable("id") Long dodId, HttpServletRequest request) {
        return service.deleteDOD(dodId, request);
    }


}
