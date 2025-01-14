package ma.fstt.springoracle.controller;

import ma.fstt.springoracle.dto.*;
import ma.fstt.springoracle.service.OracleHAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ha")
public class OracleHAController {

    @Autowired
    private OracleHAService oracleHAService;

    @GetMapping("/status")
    public ResponseEntity<DataGuardStatusDTO> getDataGuardStatus() {
        return ResponseEntity.ok(oracleHAService.getDataGuardStatus());
    }

    @PostMapping("/configure")
    public ResponseEntity<?> configureDataGuard(@RequestBody DataGuardConfigDTO config) {
        try {
            oracleHAService.configureDataGuard(config);
            return ResponseEntity.ok("Data Guard configured successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Configuration failed: " + e.getMessage());
        }
    }

    @PostMapping("/simulate/failover")
    public ResponseEntity<HaOperationResponseDTO> simulateFailover() {
        return ResponseEntity.ok(oracleHAService.simulateFailover());
    }

    @PostMapping("/simulate/switchback")
    public ResponseEntity<HaOperationResponseDTO> simulateSwitchback() {
        return ResponseEntity.ok(oracleHAService.simulateSwitchback());
    }

    @GetMapping("/report")
    public ResponseEntity<AvailabilityReportDTO> getAvailabilityReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(
                oracleHAService.generateAvailabilityReport(startDate, endDate)
        );
    }
}
