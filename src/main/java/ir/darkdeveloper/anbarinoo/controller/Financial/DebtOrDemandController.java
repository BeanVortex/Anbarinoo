package ir.darkdeveloper.anbarinoo.controller.Financial;

import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import ir.darkdeveloper.anbarinoo.service.Financial.DebtOrDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/financial/debt-demand")
public class DebtOrDemandController {

    private final DebtOrDemandService service;

    @Autowired
    public DebtOrDemandController(DebtOrDemandService service) {
        this.service = service;
    }


    @PostMapping("/save/")
    public ResponseEntity<?> saveDOD(@RequestBody DebtOrDemandModel dod, HttpServletRequest req) {
        return ResponseEntity.ok(service.saveDOD(dod, req));
    }

    @PutMapping("/update/{id}/")
    public ResponseEntity<?> updateDOD(@RequestBody DebtOrDemandModel dod, @PathVariable Long id,
                                       HttpServletRequest req) {
        return ResponseEntity.ok(service.updateDOD(dod, id, req));
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
        service.deleteDOD(dodId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
