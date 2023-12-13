package ir.darkdeveloper.anbarinoo.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.model.VerificationModel;
import ir.darkdeveloper.anbarinoo.repository.VerificationRepo;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRepo repo;

    public void saveToken(VerificationModel model) {
        repo.save(model);
    }

    public Optional<VerificationModel> findByToken(String token) {
        return repo.findByToken(token);
    }

}
