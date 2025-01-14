package ma.fstt.springoracle.service;

import ma.fstt.springoracle.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class OracleHAServiceImpl implements OracleHAService {
    private static final Logger logger = LoggerFactory.getLogger(OracleHAServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public DataGuardStatusDTO getDataGuardStatus() {
        try {
            String sql = """
                SELECT 
                    d.database_role,
                    d.protection_mode,
                    d.protection_level,
                    d.switchover_status,
                    s.status,
                    s.gap_status
                FROM v$database d, v$dataguard_status s
                WHERE ROWNUM = 1
            """;

            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    return DataGuardStatusDTO.builder()
                            .databaseRole(rs.getString("database_role"))
                            .protectionMode(rs.getString("protection_mode"))
                            .protectionLevel(rs.getString("protection_level"))
                            .switchoverStatus(rs.getString("switchover_status"))
                            .status(rs.getString("status"))
                            .gapStatus(rs.getString("gap_status"))
                            .timestamp(new Date())
                            .statusCode("SUCCESS")
                            .build();
                }
                return null;
            });
        } catch (Exception e) {
            logger.error("Error getting Data Guard status: " + e.getMessage(), e);
            return DataGuardStatusDTO.builder()
                    .statusCode("ERROR")
                    .errorMessage(e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public void configureDataGuard(DataGuardConfigDTO config) {
        try {
            validateConfig(config);
            String sql = """
                BEGIN
                    DBMS_DG.INITIATE_FS_FAILOVER(
                        primary_db_name => 'PRIMARY_DB',
                        standby_db_name => 'STANDBY_DB',
                        primary_host => ?,
                        primary_port => ?,
                        standby_host => ?,
                        standby_port => ?
                    );
                END;
            """;

            jdbcTemplate.update(sql,
                    config.getPrimaryHost(),
                    config.getPrimaryPort(),
                    config.getStandbyHost(),
                    config.getStandbyPort());

            logger.info("Data Guard configuration completed successfully");
        } catch (Exception e) {
            logger.error("Error configuring Data Guard: " + e.getMessage(), e);
            throw new RuntimeException("Failed to configure Data Guard: " + e.getMessage());
        }
    }

    @Override
    public HaOperationResponseDTO simulateFailover() {
        try {
            long startTime = System.currentTimeMillis();

            String sql = "BEGIN DBMS_DG.INITIATE_FAILOVER(); END;";
            jdbcTemplate.execute(sql);

            long executionTime = System.currentTimeMillis() - startTime;

            return HaOperationResponseDTO.builder()
                    .success(true)
                    .executionTime(executionTime)
                    .message("Failover simulation completed successfully")
                    .timestamp(new Date())
                    .build();
        } catch (Exception e) {
            logger.error("Failover simulation failed: " + e.getMessage(), e);
            return HaOperationResponseDTO.builder()
                    .success(false)
                    .message("Failover simulation failed: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public HaOperationResponseDTO simulateSwitchback() {
        try {
            long startTime = System.currentTimeMillis();

            String sql = "BEGIN DBMS_DG.INITIATE_SWITCHOVER(); END;";
            jdbcTemplate.execute(sql);

            long executionTime = System.currentTimeMillis() - startTime;

            return HaOperationResponseDTO.builder()
                    .success(true)
                    .executionTime(executionTime)
                    .message("Switchback simulation completed successfully")
                    .timestamp(new Date())
                    .build();
        } catch (Exception e) {
            logger.error("Switchback simulation failed: " + e.getMessage(), e);
            return HaOperationResponseDTO.builder()
                    .success(false)
                    .message("Switchback simulation failed: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public AvailabilityReportDTO generateAvailabilityReport(String startDate, String endDate) {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_simulations,
                    COUNT(CASE WHEN type = 'FAILOVER' AND success = 1 THEN 1 END) as successful_failovers,
                    COUNT(CASE WHEN type = 'SWITCHBACK' AND success = 1 THEN 1 END) as successful_switchbacks,
                    AVG(CASE WHEN type = 'FAILOVER' THEN execution_time END) as avg_failover_time,
                    AVG(CASE WHEN type = 'SWITCHBACK' THEN execution_time END) as avg_switchback_time
                FROM ha_operations_log
                WHERE operation_date BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD')
            """;

            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> {
                        int total = rs.getInt("total_simulations");
                        int successFailovers = rs.getInt("successful_failovers");
                        int successSwitchbacks = rs.getInt("successful_switchbacks");

                        return AvailabilityReportDTO.builder()
                                .totalSimulations(total)
                                .successfulFailovers(successFailovers)
                                .successfulSwitchbacks(successSwitchbacks)
                                .avgFailoverTimeMs(rs.getDouble("avg_failover_time"))
                                .avgSwitchbackTimeMs(rs.getDouble("avg_switchback_time"))
                                .failoverSuccessRate(calculateRate(successFailovers, total))
                                .switchbackSuccessRate(calculateRate(successSwitchbacks, total))
                                .startDate(startDate)
                                .endDate(endDate)
                                .build();
                    },
                    startDate, endDate);
        } catch (Exception e) {
            logger.error("Error generating report: " + e.getMessage(), e);
            return AvailabilityReportDTO.builder()
                    .error("Failed to generate report: " + e.getMessage())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        }
    }

    private void validateConfig(DataGuardConfigDTO config) {
        if (config.getPrimaryHost() == null || config.getPrimaryHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Primary host cannot be empty");
        }
        if (config.getStandbyHost() == null || config.getStandbyHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Standby host cannot be empty");
        }
        if (config.getPrimaryPort() <= 0 || config.getPrimaryPort() > 65535) {
            throw new IllegalArgumentException("Invalid primary port number");
        }
        if (config.getStandbyPort() <= 0 || config.getStandbyPort() > 65535) {
            throw new IllegalArgumentException("Invalid standby port number");
        }
    }

    private double calculateRate(int success, int total) {
        return total > 0 ? (success * 100.0 / total) : 0.0;
    }
}