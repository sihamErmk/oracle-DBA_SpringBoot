package ma.fstt.springoracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataGuardConfigDTO {
    private String primaryHost;
    private int primaryPort;
    private String standbyHost;
    private int standbyPort;
    private String sysdbaUsername;
    private String sysdbaPassword;
    private String primaryDbName;   // New field for primary database name
    private String standbyDbName; // New field for standby database name
}