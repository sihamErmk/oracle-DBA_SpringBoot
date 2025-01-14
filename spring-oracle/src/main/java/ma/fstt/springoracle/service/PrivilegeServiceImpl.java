package ma.fstt.springoracle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.springoracle.dto.PrivilegeDTO;
import ma.fstt.springoracle.exception.PrivilegeOperationException;
import ma.fstt.springoracle.model.Privilege;
import ma.fstt.springoracle.repository.PrivilegeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivilegeServiceImpl implements PrivilegeService {

    private final PrivilegeRepository privilegeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Privilege createPrivilege(PrivilegeDTO privilegeDTO) {
        String privilegeName = privilegeDTO.getName().toUpperCase();
        log.info("Attempting to create privilege: {}", privilegeName);

        try {
            if (privilegeRepository.existsByName(privilegeName)) {
                log.warn("Privilege already exists: {}", privilegeName);
                throw new PrivilegeOperationException("Privilege already exists: " + privilegeName);
            }

            if (!validatePrivilegeExists(privilegeName)) {
                log.warn("Invalid Oracle privilege: {}", privilegeName);
                throw new PrivilegeOperationException("Invalid Oracle privilege: " + privilegeName);
            }

            Privilege privilege = new Privilege();
            privilege.setName(privilegeName);
            privilege.setDescription(privilegeDTO.getDescription());

            log.info("Saving new privilege: {}", privilegeName);
            return privilegeRepository.save(privilege);
        } catch (Exception e) {
            log.error("Error saving privilege: {}", e.getMessage(), e);
            throw e;  // rethrow the exception to be handled by global exception handler
        }
    }

    @Override
    public Optional<Privilege> getPrivilege(String name) {
        log.info("Fetching privilege: {}", name);
        return privilegeRepository.findByName(name.toUpperCase());
    }

    @Override
    public List<Privilege> getAllPrivileges() {
        log.info("Fetching all privileges");
        return privilegeRepository.findAll();
    }

    @Override
    @Transactional
    public void deletePrivilege(String name) {
        log.info("Attempting to delete privilege: {}", name);
        privilegeRepository.findByName(name.toUpperCase())
                .ifPresentOrElse(
                        privilege -> {
                            privilegeRepository.delete(privilege);
                            log.info("Privilege deleted: {}", name);
                        },
                        () -> log.warn("Privilege not found for deletion: {}", name)
                );
    }

    @Override
    @Transactional
    public void grantSystemPrivilege(String privilegeName, String userName, boolean withAdminOption) {
        log.info("Attempting to grant system privilege: {} to user: {} with admin option: {}",
                privilegeName, userName, withAdminOption);

        if (!validateSystemPrivilegeExists(privilegeName)) {
            throw new PrivilegeOperationException("Invalid system privilege: " + privilegeName);
        }

        if (!isValidUser(userName)) {
            throw new PrivilegeOperationException("Invalid user: " + userName);
        }

        String sql = String.format(
                "GRANT %s TO %s%s",
                privilegeName.toUpperCase(),
                sanitizeIdentifier(userName),
                withAdminOption ? " WITH ADMIN OPTION" : ""
        );

        executePrivilegeCommand(sql, "grant system privilege");
    }

    @Override
    @Transactional
    public void grantObjectPrivilege(String privilegeName, String objectName, String userName) {
        log.info("Attempting to grant object privilege: {} on {} to user: {}",
                privilegeName, objectName, userName);

        if (!isValidUser(userName)) {
            throw new PrivilegeOperationException("Invalid user: " + userName);
        }

        if (!isValidObject(objectName)) {
            throw new PrivilegeOperationException("Invalid object: " + objectName);
        }

        String sql = String.format(
                "GRANT %s ON %s TO %s",
                sanitizeIdentifier(privilegeName),
                sanitizeIdentifier(objectName),
                sanitizeIdentifier(userName)
        );
        executePrivilegeCommand(sql, "grant object privilege");
    }

    private boolean isValidObject(String objectName) {
        String sql = "SELECT COUNT(*) FROM all_objects WHERE object_name = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, objectName.toUpperCase());
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.warn("Error checking object existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void revokeSystemPrivilege(String privilegeName, String userName) {
        log.info("Attempting to revoke system privilege: {} from user: {}", privilegeName, userName);

        if (!isValidUser(userName)) {
            throw new PrivilegeOperationException("Invalid user: " + userName);
        }

        String sql = String.format(
                "REVOKE %s FROM %s",
                privilegeName.toUpperCase(),
                sanitizeIdentifier(userName)
        );
        executePrivilegeCommand(sql, "revoke system privilege");
    }

    @Override
    @Transactional
    public void revokeObjectPrivilege(String privilegeName, String objectName, String userName) {
        log.info("Attempting to revoke object privilege: {} on {} from user: {}",
                privilegeName, objectName, userName);

        if (!isValidUser(userName)) {
            throw new PrivilegeOperationException("Invalid user: " + userName);
        }

        String sql = String.format(
                "REVOKE %s ON %s FROM %s",
                sanitizeIdentifier(privilegeName),
                sanitizeIdentifier(objectName),
                sanitizeIdentifier(userName)
        );
        executePrivilegeCommand(sql, "revoke object privilege");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGrantedSystemPrivileges(String userName) {
        log.info("Fetching granted system privileges for user: {}", userName);
        String sql = """
            SELECT privilege 
            FROM dba_sys_privs 
            WHERE grantee = ?
            ORDER BY privilege
        """;

        return jdbcTemplate.queryForList(
                sql,
                String.class,
                userName.toUpperCase()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGrantedObjectPrivileges(String userName) {
        log.info("Fetching granted object privileges for user: {}", userName);
        String sql = """
            SELECT privilege || ' ON ' || owner || '.' || table_name as privilege_desc 
            FROM dba_tab_privs 
            WHERE grantee = ?
            ORDER BY owner, table_name, privilege
        """;

        return jdbcTemplate.queryForList(
                sql,
                String.class,
                userName.toUpperCase()
        );
    }

    private boolean validatePrivilegeExists(String privilegeName) {
        log.debug("Validating privilege existence: {}", privilegeName);
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT privilege FROM dba_sys_privs WHERE privilege = ?
                UNION
                SELECT privilege FROM dba_tab_privs WHERE privilege = ?
            )
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                privilegeName.toUpperCase(),
                privilegeName.toUpperCase()
        );

        return count != null && count > 0;
    }

    private boolean validateSystemPrivilegeExists(String privilegeName) {
        log.debug("Validating system privilege existence: {}", privilegeName);
        String sql = "SELECT COUNT(*) FROM system_privilege_map WHERE name = ?";

        try {
            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    privilegeName.toUpperCase()
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Error checking system privilege existence: {}", e.getMessage());
            // If we can't verify (e.g., insufficient permissions), we'll assume it exists
            return true;
        }
    }

    private void executePrivilegeCommand(String sql, String operation) {
        try {
            log.debug("Executing privilege command: {}", sql);
            jdbcTemplate.execute(sql);
            log.info("Successfully executed {}", operation);
        } catch (DataAccessException e) {
            log.error("Failed to execute {}: {}", operation, e.getMessage(), e);
            throw new PrivilegeOperationException(
                    String.format("Failed to %s: %s", operation, e.getMessage()),
                    e
            );
        }
    }

    private String sanitizeIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new PrivilegeOperationException("Identifier cannot be null or empty");
        }
        // Only quote identifiers if they contain lowercase letters or special characters
        if (identifier.matches("^[A-Z0-9_]+$")) {
            return identifier;
        } else {
            return "\"" + identifier.replace("\"", "\"\"") + "\"";
        }
    }

    private boolean isValidUser(String userName) {
        log.debug("Validating user existence: {}", userName);
        String sql = "SELECT COUNT(*) FROM all_users WHERE username = ?";

        try {
            Integer count = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    userName.toUpperCase()
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Error checking user existence: {}", e.getMessage());
            return false;
        }
    }
}

