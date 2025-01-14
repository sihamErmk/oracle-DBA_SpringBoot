package ma.fstt.springoracle.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.dto.UserDTO;
import ma.fstt.springoracle.model.OracleUser;
import ma.fstt.springoracle.model.Role;
import ma.fstt.springoracle.repository.RoleRepository;
import ma.fstt.springoracle.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;

import java.util.regex.Pattern;

import static org.hibernate.internal.CoreLogging.logger;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OracleUser createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("User already exists");
        }

        if (userDTO.getPasswordPolicy() != null) {
            if (!validatePasswordPolicy(userDTO.getPassword(), userDTO.getPasswordPolicy())) {
                throw new RuntimeException("Password does not meet policy requirements");
            }
        }

        // Create Oracle user with proper quoting
        String createUserSql = String.format(
                "CREATE USER \"%s\" IDENTIFIED BY \"%s\" DEFAULT TABLESPACE \"%s\" TEMPORARY TABLESPACE \"%s\"",
                userDTO.getUsername().toUpperCase().trim(),
                userDTO.getPassword().trim(),
                userDTO.getDefaultTablespace().toUpperCase().trim(),
                userDTO.getTemporaryTablespace().toUpperCase().trim()
        );
        jdbcTemplate.execute(createUserSql);

        // Set quota if specified
        if (userDTO.getQuotaLimit() != null) {
            setQuota(userDTO.getUsername(), userDTO.getDefaultTablespace(), userDTO.getQuotaLimit());
        }

        // Create JPA entity
        OracleUser user = new OracleUser();
        user.setUsername(userDTO.getUsername().toUpperCase());
        user.setDefaultTablespace(userDTO.getDefaultTablespace().toUpperCase());
        user.setTemporaryTablespace(userDTO.getTemporaryTablespace().toUpperCase());
        user.setQuotaLimit(userDTO.getQuotaLimit());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(
                userDTO.getPasswordPolicy() != null ? userDTO.getPasswordPolicy().getExpiryDays() : 90
        ));

        return userRepository.save(user);
    }
    @Override
    @Transactional
    public OracleUser updateUser(String username, UserDTO userDTO) {
        String upperUsername = username.toUpperCase();

        OracleUser user = userRepository.findByUsername(upperUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + upperUsername));

        if (userDTO.getPassword() != null) {
            if (userDTO.getPasswordPolicy() != null &&
                    !validatePasswordPolicy(userDTO.getPassword(), userDTO.getPasswordPolicy())) {
                throw new RuntimeException("Password does not meet policy requirements");
            }
            jdbcTemplate.execute(String.format("ALTER USER \"%s\" IDENTIFIED BY \"%s\"",
                    upperUsername,
                    userDTO.getPassword()));
        }

        if (userDTO.getQuotaLimit() != null) {
            setQuota(upperUsername, user.getDefaultTablespace(), userDTO.getQuotaLimit());
            user.setQuotaLimit(userDTO.getQuotaLimit());
        }

        // Additional checks, logging, etc.
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        OracleUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        jdbcTemplate.execute(String.format("DROP USER %s CASCADE", username));
        userRepository.delete(user);
    }


    @Override
    public Optional<OracleUser> getUser(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<OracleUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void lockAccount(String username) {
        jdbcTemplate.execute(String.format("ALTER USER \"%s\" ACCOUNT LOCK", username.toUpperCase()));
        userRepository.updateAccountLockStatus(username.toUpperCase(), true);
    }

    @Override
    @Transactional
    public void unlockAccount(String username) {
        jdbcTemplate.execute(String.format("ALTER USER \"%s\" ACCOUNT UNLOCK", username.toUpperCase()));
        userRepository.updateAccountLockStatus(username.toUpperCase(), false);
    }

    @Override
    @Transactional
    public void resetPassword(String username, String newPassword) {
        jdbcTemplate.execute(
                String.format("ALTER USER \"%s\" IDENTIFIED BY \"%s\"",
                        username.toUpperCase(),
                        newPassword)
        );
    }

    @Override
    @Transactional
    public void setQuota(String username, String tablespace, String quota) {
        jdbcTemplate.execute(
                String.format("ALTER USER \"%s\" QUOTA %s ON \"%s\"",
                        username.toUpperCase(),
                        quota,
                        tablespace.toUpperCase())
        );
    }

    @Override
    public boolean validatePasswordPolicy(String password, UserDTO.PasswordPolicy policy) {
        if (password.length() < policy.getMinLength()) {
            return false;
        }
        if (policy.isRequireSpecialChar() && !Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
            return false;
        }
        if (policy.isRequireNumber() && !Pattern.compile("[0-9]").matcher(password).find()) {
            return false;
        }
        if (policy.isRequireUpperCase() && !Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void updatePasswordExpiryDate(String username, int expiryDays) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPasswordExpiryDate(LocalDateTime.now().plusDays(expiryDays));
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void recordLoginAttempt(String username, boolean successful) {
        if (successful) {
            userRepository.updateLastLoginDate(username, LocalDateTime.now());
            jdbcTemplate.execute(String.format("ALTER USER %s ACCOUNT UNLOCK", username));
        } else {
            userRepository.incrementFailedLoginAttempts(username);
            Optional<OracleUser> user = userRepository.findByUsername(username);
            if (user.isPresent() && user.get().getFailedLoginAttempts() >= 3) {
                lockAccount(username);
            }
        }
    }

    @Override
    @Transactional
    public void grantRole(String username, String roleName) {
        try {
            // First try to grant the role in Oracle
            String sql = String.format("GRANT \"%s\" TO \"%s\"",
                    roleName.toUpperCase(),
                    username.toUpperCase()
            );
            jdbcTemplate.execute(sql);

            // If Oracle grant succeeds, update application database
            userRepository.findByUsername(username.toUpperCase())
                    .ifPresent(user -> {
                        // Get or create role
                        Role role = roleRepository.findByName(roleName.toUpperCase())
                                .orElseGet(() -> {
                                    Role newRole = new Role();
                                    newRole.setName(roleName.toUpperCase());
                                    return roleRepository.save(newRole);
                                });

                        user.getRoles().add(role);
                        userRepository.save(user);
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to grant role: " + e.getMessage(), e);
        }
    }


    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    @Transactional
    public void revokeRole(String username, String roleName) {
        try {
            // Log SQL for debugging
            String sql = String.format("REVOKE \"%s\" FROM \"%s\"",
                    roleName.toUpperCase(),
                    username.toUpperCase());
            logger.info("Executing SQL: {}", sql);

            // First try to revoke in Oracle
            jdbcTemplate.execute(sql);

            // If Oracle revoke succeeds, update application database
            userRepository.findByUsername(username.toUpperCase())
                    .ifPresent(user -> {
                        roleRepository.findByName(roleName.toUpperCase())
                                .ifPresent(role -> {
                                    user.getRoles().remove(role);
                                    userRepository.save(user);
                                });
                    });

        } catch (Exception e) {
            // Log the error message for debugging
            logger.error("Error while revoking role for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to revoke role: " + e.getMessage(), e);
        }
    }


}