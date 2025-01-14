package ma.fstt.springoracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityReportDTO {
    private int totalSimulations;
    private double avgFailoverTimeMs;
    private double avgSwitchbackTimeMs;
    private int successfulFailovers;
    private int successfulSwitchbacks;
    private double failoverSuccessRate;
    private double switchbackSuccessRate;
    private String startDate;
    private String endDate;
    private String error;
}
