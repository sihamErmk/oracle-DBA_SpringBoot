package ma.fstt.springoracle.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.dto.PasswordResetRequest;
import ma.fstt.springoracle.dto.RoleDTO;
import ma.fstt.springoracle.dto.UserDTO;
import ma.fstt.springoracle.model.OracleUser;
import ma.fstt.springoracle.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO user) {
        // Ensure user is correctly mapped and saved in the database
        try {
            userService.createUser(user);  // Call your service to create the user
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();  // Log any errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{username}")
    public ResponseEntity<OracleUser> updateUser(
            @PathVariable String username,
            @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(username, userDTO));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{username}")
    public ResponseEntity<OracleUser> getUser(@PathVariable String username) {
        return userService.getUser(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OracleUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/{username}/lock")
    public ResponseEntity<Void> lockAccount(@PathVariable String username) {
        userService.lockAccount(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/unlock")
    public ResponseEntity<Void> unlockAccount(@PathVariable String username) {
        userService.unlockAccount(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable String username,
            @RequestBody PasswordResetRequest request) {
        userService.resetPassword(username, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/roles")
    public ResponseEntity<?> grantRole(
            @PathVariable String username,
            @Valid @RequestBody RoleDTO roleDTO) {
        userService.grantRole(username, roleDTO.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/roles/{roleName}")
    public ResponseEntity<?> revokeRole(
            @PathVariable String username,
            @PathVariable String roleName) {
        userService.revokeRole(username, roleName);
        return ResponseEntity.ok().build();
    }
}