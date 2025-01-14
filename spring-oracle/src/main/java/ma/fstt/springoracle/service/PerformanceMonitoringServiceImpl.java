//package ma.fstt.springoracle.service;
//
//import ma.fstt.springoracle.model.PerformanceData;
//import ma.fstt.springoracle.repository.PerformanceMetricsRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class PerformanceMonitoringServiceImpl implements PerformanceMonitoringService {
//
//    @Autowired
//    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private PerformanceMetricsRepository metricsRepository;
//
//    @Override
//    public List<Map<String, Object>> getAshData() {
//        String sql = """
//            SELECT SAMPLE_ID AS "Sample ID", SAMPLE_TIME AS "Sample Time", SESSION_ID AS "Session ID",
//                   SESSION_TYPE AS "Session Type", SQL_ID AS "SQL ID", SQL_OPNAME AS "SQL Operation",
//                   EVENT AS "Wait Event", P1TEXT AS "Parameter 1", P2TEXT AS "Parameter 2", P3TEXT AS "Parameter 3"
//            FROM V$ACTIVE_SESSION_HISTORY
//            WHERE SAMPLE_TIME >= SYSDATE - INTERVAL '1' HOUR
//              AND SAMPLE_TIME <= SYSDATE
//            ORDER BY SAMPLE_TIME DESC
//        """;
//
//        try {
//            return namedParameterJdbcTemplate.getJdbcTemplate().queryForList(sql);
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching ASH data", e);
//        }
//    }
//
//    @Override
//    public List<Map<String, Object>> getAwrData() {
//        // Query to fetch the most recent snapshot IDs
//        String snapshotSql = """
//            SELECT MIN(SNAP_ID) AS startSnapId, MAX(SNAP_ID) AS endSnapId
//            FROM DBA_HIST_SNAPSHOT
//            WHERE BEGIN_INTERVAL_TIME >= SYSDATE - INTERVAL '1' DAY
//        """;
//
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//
//        Map<String, Object> snapshotRange;
//        try {
//            snapshotRange = namedParameterJdbcTemplate.queryForMap(snapshotSql, parameters);
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching snapshot range for AWR data", e);
//        }
//
//        if (snapshotRange.get("startSnapId") == null || snapshotRange.get("endSnapId") == null) {
//            throw new RuntimeException("No snapshots available in the specified range.");
//        }
//
//        // Convert BigDecimal to int
//        int startSnapId = ((BigDecimal) snapshotRange.get("startSnapId")).intValue();
//        int endSnapId = ((BigDecimal) snapshotRange.get("endSnapId")).intValue();
//
//        // Now query the AWR data
//        String sql = """
//            SELECT SNAP_ID, STAT_NAME, VALUE
//            FROM DBA_HIST_SYSSTAT
//            WHERE SNAP_ID BETWEEN :startSnapId AND :endSnapId
//            ORDER BY SNAP_ID, STAT_NAME
//        """;
//
//        parameters.addValue("startSnapId", startSnapId);
//        parameters.addValue("endSnapId", endSnapId);
//
//        try {
//            return namedParameterJdbcTemplate.queryForList(sql, parameters);
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching AWR data", e);
//        }
//    }
//    // Get Real-time Metrics
//    public PerformanceData getRealTimeMetrics() {
//        PerformanceData metrics = new PerformanceData();
//        try {
//            // CPU Usage as percentage
//            String cpuSql = """
//        SELECT ROUND(
//            (SELECT value FROM v$sysstat
//             WHERE name = 'CPU used by this session') /
//            (SELECT value * 100 FROM v$parameter
//             WHERE name = 'cpu_count'),
//            2) as cpu_percentage
//        FROM dual
//        """;
//            Double cpuUsage = jdbcTemplate.queryForObject(cpuSql, Double.class);
//            metrics.setCpuUsagePercent(cpuUsage);
//
//            // Memory Usage (SGA + PGA)
//            String sgaSql = """
//        SELECT ROUND(SUM(bytes)/(1024*1024), 2) as sga_mb
//        FROM v$sgastat
//        """;
//            Double sgaUsage = jdbcTemplate.queryForObject(sgaSql, Double.class);
//
//            String pgaSql = """
//        SELECT ROUND(value/(1024*1024), 2) as pga_mb
//        FROM v$pgastat
//        WHERE name = 'total PGA allocated'
//        AND ROWNUM = 1
//        """;
//            Double pgaUsage = jdbcTemplate.queryForObject(pgaSql, Double.class);
//
//            metrics.setMemoryUsageMB(sgaUsage);
//            metrics.setPgaUsageMB(pgaUsage);
//
//            // Buffer Cache Hit Ratio
//            String bufferCacheSql = """
//        SELECT ROUND(
//            (1 - (phy.value / (cur.value + con.value))) * 100,
//            2) as buffer_cache_hit_ratio
//        FROM v$sysstat cur, v$sysstat con, v$sysstat phy
//        WHERE cur.name = 'db block gets'
//        AND con.name = 'consistent gets'
//        AND phy.name = 'physical reads'
//        """;
//            Double bufferCacheHitRatio = jdbcTemplate.queryForObject(bufferCacheSql, Double.class);
//            metrics.setBufferCacheHitRatio(bufferCacheHitRatio);
//
//            // IO Operations per Second
//            String ioSql = """
//        SELECT ROUND(
//            value /
//            (SYSDATE - startup_time) * 86400,
//            2) as io_per_second
//        FROM v$sysstat, v$instance
//        WHERE name = 'physical reads'
//        """;
//            Double ioRate = jdbcTemplate.queryForObject(ioSql, Double.class);
//            metrics.setIoOperationsPerSecond(ioRate);
//
//            return metrics;
//
//        } catch (Exception e) {
//            System.out.println(e);
//            throw new RuntimeException("Failed to collect performance metrics", e);
//        }
//    }
//
//
//}
