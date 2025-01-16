package ma.fstt.springoracle.controller;

import ma.fstt.springoracle.model.SlowQuery;
import ma.fstt.springoracle.service.SlowQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/slow-queries")
public class SlowQueryController {

    @Autowired
    private SlowQueryService slowQueryService;

    // Afficher les requêtes lentes
    @GetMapping
    public ResponseEntity<?> getSlowQueries() {
        try {
            List<SlowQuery> slowQueries = slowQueryService.getSlowQueries();
            return ResponseEntity.ok(slowQueries);
        } catch (Exception e) {
            // Retourner une chaîne d'erreur sans ajouter de classe supplémentaire
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving slow queries: " + e.getMessage());
        }
    }

    // Optimiser une requête SQL
    @PostMapping("/optimize/{sqlId}")
    public ResponseEntity<?> optimizeQuery(@PathVariable String sqlId) {
        try {
            String tuningReport = slowQueryService.optimizeQuery(sqlId);
            return ResponseEntity.ok(tuningReport);
        } catch (Exception e) {
            // Retourner une chaîne d'erreur sans ajouter de classe supplémentaire
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during query optimization: " + e.getMessage());
        }
    }
}