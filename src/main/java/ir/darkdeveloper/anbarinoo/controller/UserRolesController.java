package ir.darkdeveloper.anbarinoo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.darkdeveloper.anbarinoo.model.UserRoles;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;

@RestController
@RequestMapping("/api/user/role")
@CrossOrigin("*")
public class UserRolesController {
    
    private final UserRolesService service;

    @Autowired
    public UserRolesController(UserRolesService service) {
        this.service = service;
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('OP_ACCESS_ROLE')")
    public List<UserRoles> getAllRoles(){
        return service.getAllRoles();
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('OP_ADD_ROLE')")
    public ResponseEntity<?> saveRole(@RequestBody UserRoles role){
        return service.saveRole(role);
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_DELETE_ROLE')")
    public ResponseEntity<?> deleteRole(@PathVariable("id") Long id){
        return service.deleteRole(id);
    }
}
