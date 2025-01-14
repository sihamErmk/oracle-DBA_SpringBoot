package ma.fstt.springoracle.dto;

import java.util.Date;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataGuardStatusDTO {
    private String databaseRole;
    private String protectionMode;
    private String protectionLevel;
    private String switchoverStatus;
    private String status;
    private String gapStatus;
    private Date timestamp;
    private String statusCode;
    private String errorMessage;
}