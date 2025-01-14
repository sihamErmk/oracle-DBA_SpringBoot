package ma.fstt.springoracle.controller;

import ma.fstt.springoracle.model.BackupHistory;
import ma.fstt.springoracle.service.RmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rman")
public class RmanController {
    @Autowired
    private RmanService rmanService;

    @PostMapping("/backup/full")
    public ResponseEntity<?> fullBackup() {
        rmanService.performFullBackup();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/incremental-backup/{level}")
    public ResponseEntity<?> performIncrementalBackup(@PathVariable int level) {
        if (level != 0 && level != 1) {
            return ResponseEntity.badRequest().body("Invalid backup level. Only 0 and 1 are supported.");
        }
        try {
            rmanService.performIncrementalBackup(level);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during incremental backup: " + e.getMessage());
        }
    }

    @GetMapping("/backups")
    public ResponseEntity<List<BackupHistory>> getBackups() {
        List<BackupHistory> history = rmanService.listBackups();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/restore")
    public ResponseEntity<?> performRestore() {
        try {
            String result = rmanService.performRestore();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during restore: " + e.getMessage());
        }
    }
}
