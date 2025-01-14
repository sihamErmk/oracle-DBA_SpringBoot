package ma.fstt.springoracle.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
@Service
public class PerformanceOptimizationServiceImpl implements PerformanceOptimizationService  {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Get slow queries from the system
    public List<Map<String, Object>> getSlowQueries() {
        String sql = """
            SELECT sql_id, 
                   sql_text,
                   elapsed_time/1000000 as elapsed_time_secs,
                   executions,
                   elapsed_time/DECODE(executions,0,1,executions)/1000000 as avg_elapsed_secs,
                   buffer_gets,
                   disk_reads,
                   rows_processed
            FROM v$sqlarea
            WHERE elapsed_time/DECODE(executions,0,1,executions) > 1000000  -- queries taking more than 1 second
            AND executions > 0
            ORDER BY elapsed_time/executions DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // Get SQL Tuning recommendations
//    public Map<String, Object> getTuningRecommendations(String sqlId) {
//        try {
//            // Create SQL Tuning task
//            String createTask = """
//                BEGIN
//                    DBMS_SQLTUNE.CREATE_TUNING_TASK(
//                        sql_id => ?,
//                        task_name => 'TUNE_' || ?,
//                        time_limit => 3600
//                    );
//                END;
//            """;
//            jdbcTemplate.update(createTask, sqlId, sqlId);
//
//            // Execute the tuning task
//            String executeTask = """
//                BEGIN
//                    DBMS_SQLTUNE.EXECUTE_TUNING_TASK(task_name => 'TUNE_' || ?);
//                END;
//            """;
//            jdbcTemplate.update(executeTask, sqlId);
//
//            // Get the recommendations
//            String getRecommendations = """
//                SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK('TUNE_' || ?) AS recommendations FROM dual
//            """;
//            return jdbcTemplate.queryForMap(getRecommendations, sqlId);
//
//        } finally {
//            // Cleanup
//            try {
//                String dropTask = """
//                    BEGIN
//                        DBMS_SQLTUNE.DROP_TUNING_TASK('TUNE_' || ?);
//                    END;
//                """;
//                jdbcTemplate.update(dropTask, sqlId);
//            } catch (Exception e) {
//                log.error("Error dropping tuning task", e);
//            }
//        }
//    }
    @Override
    public Map<String, Object> getTuningRecommendations(String sqlId) {
        String taskName = "TUNE_" + sqlId;
        try {
            // Create tuning task with proper bind variables
            String createTask = """
                DECLARE
                  v_sql_id VARCHAR2(13);
                  v_task_name VARCHAR2(30);
                BEGIN
                  v_sql_id := ?;
                  v_task_name := ?;
                  DBMS_SQLTUNE.CREATE_TUNING_TASK(
                    sql_id => v_sql_id,
                    task_name => v_task_name,
                    time_limit => 3600
                  );
                END;
            """;
            jdbcTemplate.update(createTask, sqlId, taskName);

            // Execute the tuning task
            String executeTask = """
                BEGIN
                  DBMS_SQLTUNE.EXECUTE_TUNING_TASK(task_name => ?);
                END;
            """;
            jdbcTemplate.update(executeTask, taskName);

            // Get recommendations
            String getRecommendations = """
                SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(?) AS recommendations FROM dual
            """;
            return jdbcTemplate.queryForMap(getRecommendations, taskName);

        } catch (Exception e) {
            log.error("Error during SQL tuning for SQL_ID: " + sqlId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to get tuning recommendations: " + e.getMessage());
            return errorResult;
        } finally {
            try {
                // Clean up
                String dropTask = """
                    BEGIN
                      DBMS_SQLTUNE.DROP_TUNING_TASK(?);
                    END;
                """;
                jdbcTemplate.update(dropTask, taskName);
            } catch (Exception e) {
                log.error("Error dropping tuning task: " + taskName, e);
            }
        }
    }

    // Gather table statistics
    public void gatherTableStats(String schemaName, String tableName) {
        String sql = """
            BEGIN
                DBMS_STATS.GATHER_TABLE_STATS(
                    ownname => ?,
                    tabname => ?,
                    estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
                    method_opt => 'FOR ALL COLUMNS SIZE AUTO',
                    cascade => TRUE
                );
            END;
        """;
        jdbcTemplate.update(sql, schemaName, tableName);
    }

    // Schedule statistics gathering job
    public void scheduleStatsGathering(String schemaName) {
        String sql = """
            BEGIN
                DBMS_SCHEDULER.CREATE_JOB(
                    job_name => 'GATHER_STATS_' || ?,
                    job_type => 'PLSQL_BLOCK',
                    job_action => 'BEGIN DBMS_STATS.GATHER_SCHEMA_STATS(''' || ? || '''); END;',
                    repeat_interval => 'FREQ=DAILY;BYHOUR=2',  -- Run daily at 2 AM
                    enabled => TRUE
                );
            END;
        """;
        jdbcTemplate.update(sql, schemaName, schemaName);
    }
}