package ma.fstt.springoracle.controller;


import lombok.RequiredArgsConstructor;
import ma.fstt.springoracle.service.PerformanceOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import com.oracle.admin.service.PerformanceOptimizationService;
import java.util.*;

@RestController
@RequestMapping("/api/performance/optimization")
@RequiredArgsConstructor
public class PerformanceOptimizationController {

    @Autowired
    private final PerformanceOptimizationService performanceOptimizationService;

    @GetMapping("/slow-queries")
    public ResponseEntity<List<Map<String, Object>>> getSlowQueries() {
        return ResponseEntity.ok(performanceOptimizationService.getSlowQueries());
    }

    @GetMapping("/tuning-recommendations/{sqlId}")
    public ResponseEntity<Map<String, Object>> getTuningRecommendations(
            @PathVariable String sqlId) {
        return ResponseEntity.ok(performanceOptimizationService.getTuningRecommendations(sqlId));
    }

    @PostMapping("/gather-stats")
    public ResponseEntity<Void> gatherTableStats(
            @RequestParam String schemaName,
            @RequestParam String tableName) {
        performanceOptimizationService.gatherTableStats(schemaName, tableName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedule-stats")
    public ResponseEntity<Void> scheduleStatsGathering(
            @RequestParam String schemaName) {
        performanceOptimizationService.scheduleStatsGathering(schemaName);
        return ResponseEntity.ok().build();
    }
}
