package ma.fstt.springoracle.service;

import ma.fstt.springoracle.dto.UserDTO;
import ma.fstt.springoracle.model.OracleUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserService {
    //User CRUD
    OracleUser createUser(UserDTO userDTO);
    OracleUser updateUser(String username, UserDTO userDTO);
    void deleteUser(String username);
    Optional<OracleUser> getUser(String username);
    List<OracleUser> getAllUsers();

    //Account Status
    void lockAccount(String username);
    void unlockAccount(String username);

    void resetPassword(String username, String newPassword);
    void setQuota(String username, String tablespace, String quota);

    //Password Policy
    boolean validatePasswordPolicy(String password, UserDTO.PasswordPolicy policy);
    void updatePasswordExpiryDate(String username, int expiryDays);
    void recordLoginAttempt(String username, boolean successful);

    //Roles
    void grantRole(String username, String roleName);
    void revokeRole(String username, String roleName);
}