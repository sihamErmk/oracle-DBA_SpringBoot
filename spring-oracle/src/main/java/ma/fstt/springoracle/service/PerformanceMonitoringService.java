package ma.fstt.springoracle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceMonitoringService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    public List<Map<String, Object>> getAwrData() {
//        String sql = "SELECT SNAP_ID AS \"Snapshot ID\", DBID AS \"Database ID\", INSTANCE_NUMBER AS \"Instance Number\", " +
//                "STARTUP_TIME AS \"Startup Time\", BEGIN_INTERVAL_TIME AS \"Start Time\", END_INTERVAL_TIME AS \"End Time\", " +
//                "FLUSH_ELAPSED AS \"Flush Elapsed\", SNAP_LEVEL AS \"Snapshot Level\", ERROR_COUNT AS \"Error Count\", " +
//                "SNAP_FLAG AS \"Snapshot Flag\", SNAP_TIMEZONE AS \"Snapshot Timezone\", CON_ID AS \"Container ID\" " +
//                "FROM DBA_HIST_SNAPSHOT WHERE ROWNUM <= 10";
//        return jdbcTemplate.queryForList(sql);
//    }
//
//
//
//
//
//
//    // Fetch ASH data
//    public List<Map<String, Object>> getAshData() {
//        String sql = "SELECT " +
//                "SAMPLE_ID AS \"Sample ID\", " +
//                "SAMPLE_TIME AS \"Sample Time\", " +
//                "SESSION_ID AS \"Session ID\", " +
//                "SESSION_TYPE AS \"Session Type\", " +
//                "SQL_ID AS \"SQL ID\", " +
//                "SQL_OPNAME AS \"SQL Operation\", " +
//                "EVENT AS \"Wait Event\", " +
//                "P1TEXT AS \"Parameter 1\", " +
//                "P2TEXT AS \"Parameter 2\", " +
//                "P3TEXT AS \"Parameter 3\" " +
//                "FROM V$ACTIVE_SESSION_HISTORY " +
//                "WHERE SAMPLE_TIME >= SYSDATE - INTERVAL '1' HOUR " +
//                "ORDER BY SAMPLE_TIME DESC"; // Order by most recent samples
//        return jdbcTemplate.queryForList(sql);
//    }

    public File generateAwrReport() throws IOException {
        String sql = "SELECT SNAP_ID AS \"Snapshot ID\", DBID AS \"Database ID\", INSTANCE_NUMBER AS \"Instance Number\", " +
                "STARTUP_TIME AS \"Startup Time\", BEGIN_INTERVAL_TIME AS \"Start Time\", END_INTERVAL_TIME AS \"End Time\", " +
                "FLUSH_ELAPSED AS \"Flush Elapsed\", SNAP_LEVEL AS \"Snapshot Level\", ERROR_COUNT AS \"Error Count\", " +
                "SNAP_FLAG AS \"Snapshot Flag\", SNAP_TIMEZONE AS \"Snapshot Timezone\", CON_ID AS \"Container ID\" " +
                "FROM DBA_HIST_SNAPSHOT WHERE ROWNUM <= 10";

        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

        // Create the file to write
        File file = new File("AWR_Report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("AWR Report\n");
            writer.write("===============================\n");

            // Write data rows
            for (Map<String, Object> row : data) {
                row.forEach((key, value) -> {
                    try {
                        writer.write(key + ": " + value + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.write("\n");
            }
        }

        return file;
    }

    public File generateAshReport() throws IOException {
        String sql = "SELECT SAMPLE_ID AS \"Sample ID\", SAMPLE_TIME AS \"Sample Time\", SESSION_ID AS \"Session ID\", " +
                "SESSION_TYPE AS \"Session Type\", SQL_ID AS \"SQL ID\", SQL_OPNAME AS \"SQL Operation\", " +
                "EVENT AS \"Wait Event\", P1TEXT AS \"Parameter 1\", P2TEXT AS \"Parameter 2\", P3TEXT AS \"Parameter 3\" " +
                "FROM V$ACTIVE_SESSION_HISTORY WHERE SAMPLE_TIME >= SYSDATE - INTERVAL '1' HOUR " +
                "ORDER BY SAMPLE_TIME DESC";

        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

        // Create the file to write
        File file = new File("ASH_Report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("ASH Report\n");
            writer.write("===============================\n");

            // Write data rows
            for (Map<String, Object> row : data) {
                row.forEach((key, value) -> {
                    try {
                        writer.write(key + ": " + value + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.write("\n");
            }
        }

        return file;
    }

    // Fetch real-time resource utilization
    public Map<String, Object> getRealTimeStats() {
        String cpuQuery = "SELECT VALUE FROM V$SYSSTAT WHERE NAME = 'CPU used by this session'";
        String ioQuery = "SELECT VALUE FROM V$SYSSTAT WHERE NAME = 'physical reads'";
        String memoryQuery = "SELECT VALUE FROM V$SYSSTAT WHERE NAME = 'session uga memory'";
        Map<String, Object> stats = new HashMap<>();
        stats.put("cpu", jdbcTemplate.queryForObject(cpuQuery, Long.class));
        stats.put("io", jdbcTemplate.queryForObject(ioQuery, Long.class));
        stats.put("memory", jdbcTemplate.queryForObject(memoryQuery, Long.class));
        return stats;
    }
}