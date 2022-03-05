package ir.darkdeveloper.anbarinoo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ir.darkdeveloper.anbarinoo.dto.UserRoleDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.UserRoleMapper;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import ir.darkdeveloper.anbarinoo.service.UserRolesService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/role")
@RequiredArgsConstructor
public class UserRolesController {

    private final UserRolesService service;
    private final UserRoleMapper mapper;


    @PostMapping("/")
    @PreAuthorize("hasAuthority('OP_ADD_ROLE')")
    public ResponseEntity<?> saveRole(@RequestBody UserRole role) {
        return service.saveRole(role);
    }

    record UserRoleDtos(List<UserRoleDto> userRoles) {
    }

    @GetMapping("/all/")
    @PreAuthorize("hasAuthority('OP_ACCESS_ROLE')")
    public ResponseEntity<UserRoleDtos> getAllRoles() {
        return ResponseEntity.ok(new UserRoleDtos(service.getAllRoles().stream().map(mapper::userRoleToDto).toList()));

    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_DELETE_ROLE')")
    public ResponseEntity<?> deleteRole(@PathVariable("id") Long id) {
        return service.deleteRole(id);
    }
}
