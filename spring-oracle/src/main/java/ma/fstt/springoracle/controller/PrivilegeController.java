package ma.fstt.springoracle.controller;

import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.dto.PrivilegeDTO;
import ma.fstt.springoracle.model.Privilege;
import ma.fstt.springoracle.service.PrivilegeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/privileges")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    @PostMapping
    public ResponseEntity<Privilege> createPrivilege(@RequestBody PrivilegeDTO privilegeDTO) {
        return ResponseEntity.ok(privilegeService.createPrivilege(privilegeDTO));
    }

    @GetMapping
    public ResponseEntity<List<Privilege>> getAllPrivileges() {
        return ResponseEntity.ok(privilegeService.getAllPrivileges());
    }

    @GetMapping("/{name}")
    public ResponseEntity<Privilege> getPrivilege(@PathVariable String name) {
        return privilegeService.getPrivilege(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deletePrivilege(@PathVariable String name) {
        privilegeService.deletePrivilege(name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/grant/system")
    public ResponseEntity<Void> grantSystemPrivilege(
            @RequestParam String privilegeName,
            @RequestParam String userName,
            @RequestParam(defaultValue = "false") boolean withAdminOption) {
        privilegeService.grantSystemPrivilege(privilegeName, userName, withAdminOption);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/grant/object")
    public ResponseEntity<Void> grantObjectPrivilege(
            @RequestParam String privilegeName,
            @RequestParam String objectName,
            @RequestParam String userName) {
        privilegeService.grantObjectPrivilege(privilegeName, objectName, userName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke/system")
    public ResponseEntity<Void> revokeSystemPrivilege(
            @RequestParam String privilegeName,
            @RequestParam String userName) {
        privilegeService.revokeSystemPrivilege(privilegeName, userName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke/object")
    public ResponseEntity<Void> revokeObjectPrivilege(
            @RequestParam String privilegeName,
            @RequestParam String objectName,
            @RequestParam String userName) {
        privilegeService.revokeObjectPrivilege(privilegeName, objectName, userName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/system/{userName}")
    public ResponseEntity<List<String>> getGrantedSystemPrivileges(@PathVariable String userName) {
        return ResponseEntity.ok(privilegeService.getGrantedSystemPrivileges(userName));
    }

    @GetMapping("/object/{userName}")
    public ResponseEntity<List<String>> getGrantedObjectPrivileges(@PathVariable String userName) {
        return ResponseEntity.ok(privilegeService.getGrantedObjectPrivileges(userName));
    }
}