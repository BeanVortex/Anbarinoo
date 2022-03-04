package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.exception.ForbiddenException;
import ir.darkdeveloper.anbarinoo.exception.InternalServerException;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.repository.UserRolesRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserRolesService {

    private final UserRolesRepo repo;


    @Transactional
    public ResponseEntity<?> saveRole(UserRole role) {
        return exceptionHandlers(() -> {
            repo.save(role);
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }

    public List<UserRole> getAllRoles() {
        return repo.findAll();
    }

    public Set<UserRole> findAllByName(String name) {
        return repo.findAllByName(name);
    }

    public ResponseEntity<?> deleteRole(Long id) {
        return exceptionHandlers(() -> {
            repo.deleteById(id);
            return ResponseEntity.ok("Deleted the role");
        });
    }

    public Boolean exists(String name) {
        return repo.findByName(name).isPresent();
    }

    private <T> T exceptionHandlers(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ForbiddenException f) {
            throw new ForbiddenException(f.getLocalizedMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getLocalizedMessage());
        }
    }

}
