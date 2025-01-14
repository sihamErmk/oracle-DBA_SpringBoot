package ma.fstt.springoracle.service;

import java.util.List;
import java.util.Map;

public interface PerformanceOptimizationService {

    public List<Map<String, Object>> getSlowQueries() ;

    public Map<String, Object> getTuningRecommendations(String sqlId) ;

    public void gatherTableStats(String schemaName, String tableName) ;

    public void scheduleStatsGathering(String schemaName) ;

}
