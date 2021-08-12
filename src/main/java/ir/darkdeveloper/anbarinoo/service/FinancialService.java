package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.repository.FinancialRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FinancialService {
    private final FinancialRepo repo;

}
