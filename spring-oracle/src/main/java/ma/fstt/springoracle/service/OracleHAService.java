package ma.fstt.springoracle.service;

import ma.fstt.springoracle.dto.*;

public interface OracleHAService {
    public DataGuardStatusDTO getDataGuardStatus();

    public void configureDataGuard(DataGuardConfigDTO config);

    public HaOperationResponseDTO simulateFailover();

    public HaOperationResponseDTO simulateSwitchback();

    public AvailabilityReportDTO generateAvailabilityReport(String startDate, String endDate);
}