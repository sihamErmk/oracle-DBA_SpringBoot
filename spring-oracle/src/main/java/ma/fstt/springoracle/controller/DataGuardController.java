package ma.fstt.springoracle.controller;

import ma.fstt.springoracle.model.DataGuard;
import ma.fstt.springoracle.service.DataGuardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data-guard")
public class DataGuardController {
    private final DataGuardService service;

    public DataGuardController(DataGuardService service) {
        this.service = service;
    }

    @PostMapping("/create-configuration")
    public ResponseEntity<String> createConfiguration(@RequestBody DataGuard configRequest) {
        try {
            String result = service.createDataGuardConfiguration(configRequest.getPrimaryDb(), configRequest.getStandbyDb());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }



    @GetMapping("/status")
    public String getDataGuardStatus() {
        return service.getDataGuardStatus();
    }


    @PostMapping("/failover")
    public String simulateFailover(@RequestParam String standbyDb) {
        return service.simulateFailover(standbyDb);
    }

    @PostMapping("/switchover")
    public String simulateSwitchover(@RequestParam String primaryDb) {
        return service.simulateSwitchover(primaryDb);
    }


    @GetMapping("/report")
    public String generateAvailabilityReport() {
        return service.generateAvailabilityReport();
    }
}
