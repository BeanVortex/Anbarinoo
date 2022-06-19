package ir.darkdeveloper.anbarinoo.service;

import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.repository.UserRolesRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import static ir.darkdeveloper.anbarinoo.util.ExceptionUtils.exceptionHandlers;

@Service
@RequiredArgsConstructor
public class UserRolesService {

    private final UserRolesRepo repo;
    private static final String DATA_EXISTS_MESSAGE = "Role exists!";

    @Transactional
    public ResponseEntity<?> saveRole(UserRole role) {
        return exceptionHandlers(() -> {
            repo.save(role);
            return new ResponseEntity<>("Role created", HttpStatus.CREATED);
        }, DATA_EXISTS_MESSAGE);
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
            return ResponseEntity.ok("Role deleted");
        }, DATA_EXISTS_MESSAGE);
    }

    public Boolean exists(String name) {
        return repo.findByName(name).isPresent();
    }


}
