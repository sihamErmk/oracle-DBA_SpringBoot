package ma.fstt.springoracle.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.dto.RoleDTO;
import ma.fstt.springoracle.model.Privilege;
import ma.fstt.springoracle.model.Role;
import ma.fstt.springoracle.repository.PrivilegeRepository;
import ma.fstt.springoracle.repository.RoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Role createRole(RoleDTO roleDTO) {
        if (!roleRepository.existsByName(roleDTO.getName())) {
        jdbcTemplate.execute("CREATE ROLE " + roleDTO.getName());
    }


        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());

        if (roleDTO.getPrivileges() != null && !roleDTO.getPrivileges().isEmpty()) {
            Set<String> validPrivileges = validateAndGetOraclePrivileges(roleDTO.getPrivileges());

            validPrivileges.forEach(privilege ->
                    jdbcTemplate.execute("GRANT " + privilege + " TO " + roleDTO.getName())
            );

            // Store only validated privileges in the database
            Set<Privilege> privileges = validPrivileges.stream()
                    .map(this::getOrCreatePrivilege)
                    .collect(Collectors.toSet());
            role.setPrivileges(privileges);
        }

        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> getRole(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public void deleteRole(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));

        jdbcTemplate.execute("DROP ROLE " + name);
        roleRepository.delete(role);
    }

    @Override
    public void grantPrivilege(String roleName, String privilegeName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // Validate if the privilege exists in Oracle
        validateAndGetOraclePrivileges(Collections.singleton(privilegeName));

        jdbcTemplate.execute("GRANT " + privilegeName + " TO " + roleName);

        // Update the role's privileges in the database
        Privilege privilege = getOrCreatePrivilege(privilegeName);
        role.getPrivileges().add(privilege);
        roleRepository.save(role);
    }

    @Override
    public void grantPrivileges(String roleName, Set<String> privilegeNames) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        Set<String> validPrivileges = validateAndGetOraclePrivileges(privilegeNames);

        validPrivileges.forEach(privilegeName ->
                jdbcTemplate.execute("GRANT " + privilegeName + " TO " + roleName)
        );

        // Update the role's privileges in the database
        Set<Privilege> privileges = validPrivileges.stream()
                .map(this::getOrCreatePrivilege)
                .collect(Collectors.toSet());
        role.getPrivileges().addAll(privileges);
        roleRepository.save(role);
    }

    @Override
    public void revokePrivilege(String roleName, String privilegeName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        jdbcTemplate.execute("REVOKE " + privilegeName + " FROM " + roleName);

        role.getPrivileges().removeIf(privilege -> privilege.getName().equals(privilegeName));
        roleRepository.save(role);
    }

    @Override
    public Set<String> getRolePrivileges(String roleName) {
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT PRIVILEGE FROM DBA_SYS_PRIVS WHERE GRANTEE = ?",
                String.class,
                roleName
        ));
    }

    @Override
    public boolean hasPrivilege(String roleName, String privilegeName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DBA_SYS_PRIVS WHERE GRANTEE = ? AND PRIVILEGE = ?",
                Integer.class,
                roleName,
                privilegeName
        );
        return count != null && count > 0;
    }

    private Set<String> validateAndGetOraclePrivileges(Set<String> privilegeNames) {
        // Query Oracle system privileges
        List<String> validOraclePrivileges = jdbcTemplate.queryForList(
                "SELECT DISTINCT PRIVILEGE FROM DBA_SYS_PRIVS",
                String.class
        );

        // Validate privileges
        Set<String> invalidPrivileges = privilegeNames.stream()
                .filter(privilege -> !validOraclePrivileges.contains(privilege))
                .collect(Collectors.toSet());

        if (!invalidPrivileges.isEmpty()) {
            throw new RuntimeException("Invalid Oracle privileges: " + String.join(", ", invalidPrivileges));
        }

        return privilegeNames;
    }

    private Privilege getOrCreatePrivilege(String privilegeName) {
        return privilegeRepository.findByName(privilegeName)
                .orElseGet(() -> {
                    Privilege newPrivilege = new Privilege();
                    newPrivilege.setName(privilegeName);
                    return privilegeRepository.save(newPrivilege);
                });
    }
    @Override
    public List<String> getAvailablePrivileges() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT PRIVILEGE FROM DBA_SYS_PRIVS",
                String.class
        );
    }
}