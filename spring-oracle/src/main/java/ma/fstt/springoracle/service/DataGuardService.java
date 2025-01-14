package ma.fstt.springoracle.service;

import ma.fstt.springoracle.model.DataGuard;

import java.util.List;
import java.util.Optional;

public interface DataGuardService {
//    List<DataGuard> getAllConfigs();
//    DataGuard saveConfig(DataGuard config);
//    Optional<DataGuard> getConfigById(Long id);
//    void deleteConfig(Long id);
        String createDataGuardConfiguration(String primaryDb, String standbyDb);
        String getDataGuardStatus();
        String simulateFailover(String standbyDb);
        String simulateSwitchover(String primaryDb);
        String generateAvailabilityReport();

}
