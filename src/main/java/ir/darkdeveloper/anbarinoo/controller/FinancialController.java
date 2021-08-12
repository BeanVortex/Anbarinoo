package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/financial")
public class FinancialController {

    private final FinancialService service;

    @Autowired
    public FinancialController(FinancialService service) {
        this.service = service;
    }
}
