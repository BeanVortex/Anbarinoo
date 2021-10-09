package ir.darkdeveloper.anbarinoo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.darkdeveloper.anbarinoo.model.VerificationModel;
import ir.darkdeveloper.anbarinoo.repository.VerificationRepo;

@Service
public class VerificationService {

    private final VerificationRepo verificationRepo;

    @Autowired
    public VerificationService(VerificationRepo verificationRepo) {
        this.verificationRepo = verificationRepo;
    }

    public void saveToken(VerificationModel model) {
        verificationRepo.save(model);
    }

    public Optional<VerificationModel> findByToken(String token) {
        return verificationRepo.findByToken(token);
    }

}
