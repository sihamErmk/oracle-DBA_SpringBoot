package ma.fstt.springoracle.controller;

import ma.fstt.springoracle.service.PerformanceMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
public class PerformanceMonitoringController {

    @Autowired
    private PerformanceMonitoringService monitoringService;

    // Endpoint for AWR report download
    @GetMapping("/awrReport")
    public ResponseEntity<byte[]> getAwrReport() throws IOException {
        File awrReport = monitoringService.generateAwrReport();
        return downloadFile(awrReport);
    }

    // Endpoint for ASH report download
    @GetMapping("/ashReport")
    public ResponseEntity<byte[]> getAshReport() throws IOException {
        File ashReport = monitoringService.generateAshReport();
        return downloadFile(ashReport);
    }

    // Endpoint for real-time stats in JSON format
    @GetMapping("/realtime")
    public Map<String, Object> getRealTimeStats() {
        return monitoringService.getRealTimeStats();
    }

    // Helper method to download the file
    private ResponseEntity<byte[]> downloadFile(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        byte[] fileBytes = inputStream.readAllBytes();

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .body(fileBytes);
    }
}