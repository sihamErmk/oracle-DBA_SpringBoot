package ma.fstt.springoracle.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.exception.security.AuditConfigurationException;
import ma.fstt.springoracle.exception.security.TDEConfigurationException;
import ma.fstt.springoracle.exception.security.VPDConfigurationException;
import ma.fstt.springoracle.model.AuditConfig;
import ma.fstt.springoracle.model.TDEConfig;
import ma.fstt.springoracle.model.VPDPolicy;
import ma.fstt.springoracle.repository.AuditConfigRepository;
import ma.fstt.springoracle.repository.TDEConfigRepository;
import ma.fstt.springoracle.repository.VPDPolicyRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OracleSecurityServiceImpl implements OracleSecurityService {
    private final TDEConfigRepository tdeConfigRepository;
    private final VPDPolicyRepository vpdPolicyRepository;
    private final AuditConfigRepository auditConfigRepository;
    private final JdbcTemplate jdbcTemplate;

    // TDE Methods
    @Transactional
    public TDEConfig enableColumnEncryption(String tableName, String columnName, String algorithm, String username) {
        if (tdeConfigRepository.existsByTableNameAndColumnName(tableName, columnName)) {
            throw new TDEConfigurationException("Encryption already configured for this column");
        }

        try {
            // First check if wallet is open
            String checkWalletSql = "SELECT STATUS FROM V$ENCRYPTION_WALLET WHERE ROWNUM = 1";

            String walletStatus = jdbcTemplate.queryForObject(checkWalletSql, String.class);

            if (!"OPEN".equals(walletStatus)) {
                throw new TDEConfigurationException("Encryption wallet is not open. Please contact database administrator.");
            }

            String sql = String.format(
                    "ALTER TABLE %s MODIFY %s ENCRYPT USING '%s'",
                    tableName, columnName, algorithm
            );
            jdbcTemplate.execute(sql);

            TDEConfig config = new TDEConfig();
            config.setTableName(tableName);
            config.setColumnName(columnName);
            config.setEncryptionAlgorithm(algorithm);
            config.setCreatedAt(LocalDateTime.now());
            config.setCreatedBy(username);
            config.setActive(true);

            return tdeConfigRepository.save(config);
        } catch (Exception e) {
            throw new TDEConfigurationException("Failed to enable TDE encryption", e);
        }
    }

    @Transactional
    public void disableColumnEncryption(String tableName, String columnName) {
        TDEConfig config = tdeConfigRepository.findByTableNameAndColumnName(tableName, columnName);
        if (config == null) {
            throw new TDEConfigurationException("No TDE configuration found");
        }

        try {
            String sql = String.format("ALTER TABLE %s MODIFY %s DECRYPT", tableName, columnName);
            jdbcTemplate.execute(sql);

            config.setActive(false);
            tdeConfigRepository.save(config);
        } catch (Exception e) {
            throw new TDEConfigurationException("Failed to disable TDE encryption", e);
        }
    }

    public List<TDEConfig> getAllTDEConfigurations() {
        return tdeConfigRepository.findAll();
    }

    // VPD Methods
    @Transactional
    public VPDPolicy createPolicy(VPDPolicy policy, String username) {
        if (vpdPolicyRepository.existsByPolicyName(policy.getPolicyName())) {
            // Drop existing policy first
            try {
                dropPolicy(policy.getPolicyName());
            } catch (Exception e) {
                throw new VPDConfigurationException("Failed to drop existing policy", e);
            }
        }

        try {
            String createFunctionSql = String.format(
                    "CREATE OR REPLACE FUNCTION %s(schema_var IN VARCHAR2, table_var IN VARCHAR2) " +
                            "RETURN VARCHAR2 AS BEGIN %s END;",
                    policy.getFunctionName(),
                    policy.getPolicyFunction()
            );
            jdbcTemplate.execute(createFunctionSql);

            String addPolicySql = String.format(
                    "BEGIN DBMS_RLS.ADD_POLICY(" +
                            "object_schema => USER, " +
                            "object_name => '%s', " +
                            "policy_name => '%s', " +
                            "function_schema => USER, " +
                            "policy_function => '%s', " +
                            "statement_types => '%s'); END;",
                    policy.getTableName(),
                    policy.getPolicyName(),
                    policy.getFunctionName(),
                    policy.getStatementTypes()
            );
            jdbcTemplate.execute(addPolicySql);

            policy.setCreatedAt(LocalDateTime.now());
            policy.setCreatedBy(username);
            policy.setActive(true);

            return vpdPolicyRepository.save(policy);
        } catch (Exception e) {
            throw new VPDConfigurationException("Failed to create VPD policy", e);
        }
    }

    @Transactional
    public void dropPolicy(String policyName) {
        VPDPolicy policy = vpdPolicyRepository.findByPolicyName(policyName);
        if (policy == null) {
            throw new VPDConfigurationException("Policy not found");
        }

        try {
            // First check if the policy exists in the database
            String checkPolicySQL = """
            SELECT COUNT(*) 
            FROM ALL_POLICIES 
            WHERE OBJECT_OWNER = USER 
            AND OBJECT_NAME = ? 
            AND POLICY_NAME = ?
        """;

            int policyCount = jdbcTemplate.queryForObject(
                    checkPolicySQL,
                    Integer.class,
                    policy.getTableName().toUpperCase(),
                    policy.getPolicyName().toUpperCase()
            );

            // Only attempt to drop the policy if it exists in the database
            if (policyCount > 0) {
                String dropPolicySql = """
                BEGIN 
                    DBMS_RLS.DROP_POLICY(
                        object_schema => USER, 
                        object_name => ?, 
                        policy_name => ?
                    ); 
                END;
            """;
                jdbcTemplate.execute(
                        dropPolicySql,
                        (PreparedStatement ps) -> {
                            ps.setString(1, policy.getTableName().toUpperCase());
                            ps.setString(2, policy.getPolicyName().toUpperCase());
                            return ps;
                        }
                );
            }

            // Check if the function exists before trying to drop it
            String checkFunctionSQL = """
            SELECT COUNT(*) 
            FROM USER_PROCEDURES 
            WHERE OBJECT_TYPE = 'FUNCTION' 
            AND OBJECT_NAME = ?
        """;

            int functionCount = jdbcTemplate.queryForObject(
                    checkFunctionSQL,
                    Integer.class,
                    policy.getFunctionName().toUpperCase()
            );

            if (functionCount > 0) {
                String dropFunctionSql = "DROP FUNCTION " + policy.getFunctionName();
                jdbcTemplate.execute(dropFunctionSql);
            }

            // Delete the policy from the application database instead of just marking it inactive
            vpdPolicyRepository.delete(policy);

        } catch (Exception e) {
            throw new VPDConfigurationException("Failed to drop VPD policy: " + e.getMessage(), e);
        }
    }

    public List<VPDPolicy> getAllPolicies() {
        return vpdPolicyRepository.findAll();
    }

    // Audit Methods
    @Transactional
    public AuditConfig enableAuditing(AuditConfig config, String username) {
        if (auditConfigRepository.existsByTableName(config.getTableName())) {
            throw new AuditConfigurationException("Audit already configured for this table");
        }

        try {
            StringBuilder auditSql = new StringBuilder("AUDIT POLICY emp_audit_policy");

            // Ensure you are using unified auditing instead of traditional auditing
            jdbcTemplate.execute(auditSql.toString());

            config.setCreatedAt(LocalDateTime.now());
            config.setCreatedBy(username);
            return auditConfigRepository.save(config);
        } catch (Exception e) {
            throw new AuditConfigurationException("Failed to enable auditing", e);
        }
    }


    @Transactional
    public void disableAuditing(String tableName) {
        AuditConfig config = auditConfigRepository.findByTableName(tableName);
        if (config == null) {
            throw new AuditConfigurationException("No audit configuration found for the specified table");
        }

        try {
            // First, verify the table exists and get its owner
            String checkTableSQL = """
            SELECT OWNER 
            FROM ALL_TABLES 
            WHERE TABLE_NAME = ?
        """;

            String tableOwner = jdbcTemplate.queryForObject(
                    checkTableSQL,
                    String.class,
                    tableName.toUpperCase()
            );

            if (tableOwner == null) {
                throw new AuditConfigurationException("Table " + tableName + " not found");
            }

            // Build the fully qualified table name
            String qualifiedTableName = tableOwner + "." + tableName;

            // Disable different types of auditing based on the configuration
            if (config.isAuditSuccessful()) {
                String noAuditSuccessSQL = """
                BEGIN
                    EXECUTE IMMEDIATE 'NOAUDIT SELECT, INSERT, UPDATE, DELETE ON %s WHENEVER SUCCESSFUL';
                END;
            """.formatted(qualifiedTableName);
                jdbcTemplate.execute(noAuditSuccessSQL);
            }

            if (config.isAuditFailed()) {
                String noAuditFailureSQL = """
                BEGIN
                    EXECUTE IMMEDIATE 'NOAUDIT SELECT, INSERT, UPDATE, DELETE ON %s WHENEVER NOT SUCCESSFUL';
                END;
            """.formatted(qualifiedTableName);
                jdbcTemplate.execute(noAuditFailureSQL);
            }

            // If specific audit level was set, disable those operations
            if (config.getAuditLevel() != null && !config.getAuditLevel().isEmpty()) {
                String[] operations = config.getAuditLevel().split(",");
                for (String operation : operations) {
                    String noAuditOperationSQL = """
                    BEGIN
                        EXECUTE IMMEDIATE 'NOAUDIT %s ON %s';
                    END;
                """.formatted(operation.trim(), qualifiedTableName);
                    jdbcTemplate.execute(noAuditOperationSQL);
                }
            }

            // Clean up the audit configuration from our repository
            auditConfigRepository.delete(config);

        } catch (DataAccessException e) {
            throw new AuditConfigurationException(
                    String.format("Failed to disable auditing on table %s: %s",
                            tableName,
                            e.getMostSpecificCause().getMessage()
                    ),
                    e
            );
        }
    }

    public List<AuditConfig> getAllAuditConfigurations() {
        return auditConfigRepository.findAll();
    }
}