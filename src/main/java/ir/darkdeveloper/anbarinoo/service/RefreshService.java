package ir.darkdeveloper.anbarinoo.service;



import org.springframework.stereotype.Service;
import ir.darkdeveloper.anbarinoo.model.RefreshModel;
import ir.darkdeveloper.anbarinoo.repository.RefreshRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final RefreshRepo repo;

    @Transactional
    public void saveToken(RefreshModel model) {
        repo.save(model);
    }

    public void deleteTokenByUserId(Long id) {
        repo.deleteTokenByUserId(id);
    }

    public RefreshModel getRefreshByUserId(Long id) {
        return repo.getRefreshByUserId(id);
    }

    public Long getIdByUserId(Long adminId) {
        return repo.getIdByUserId(adminId);
    }

    public Optional<Long> getUserIdByRefreshToken(String token) {
        return repo.findUserIdByRefreshToken(token);
    }

}
