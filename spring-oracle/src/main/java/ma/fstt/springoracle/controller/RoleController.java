package ma.fstt.springoracle.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.dto.RoleDTO;
import ma.fstt.springoracle.model.Role;
import ma.fstt.springoracle.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.createRole(roleDTO));
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{name}")
    public ResponseEntity<Role> getRole(@PathVariable String name) {
        return roleService.getRole(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteRole(@PathVariable String name) {
        roleService.deleteRole(name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roleName}/privileges/{privilegeName}")
    public ResponseEntity<Void> grantPrivilege(
            @PathVariable String roleName,
            @PathVariable String privilegeName) {
        roleService.grantPrivilege(roleName, privilegeName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roleName}/privileges")
    public ResponseEntity<Void> grantPrivileges(
            @PathVariable String roleName,
            @RequestBody Set<String> privilegeNames) {
        roleService.grantPrivileges(roleName, privilegeNames);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleName}/privileges/{privilegeName}")
    public ResponseEntity<Void> revokePrivilege(
            @PathVariable String roleName,
            @PathVariable String privilegeName) {
        roleService.revokePrivilege(roleName, privilegeName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roleName}/privileges")
    public ResponseEntity<Set<String>> getRolePrivileges(@PathVariable String roleName) {
        return ResponseEntity.ok(roleService.getRolePrivileges(roleName));
    }

    @GetMapping("/{roleName}/privileges/{privilegeName}")
    public ResponseEntity<Boolean> hasPrivilege(
            @PathVariable String roleName,
            @PathVariable String privilegeName) {
        return ResponseEntity.ok(roleService.hasPrivilege(roleName, privilegeName));
    }

    @GetMapping("/available-privileges")
    public ResponseEntity<List<String>> getAvailablePrivileges() {
        return ResponseEntity.ok(roleService.getAvailablePrivileges());
    }
}