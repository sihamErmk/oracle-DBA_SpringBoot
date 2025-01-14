package ma.fstt.springoracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HaOperationResponseDTO {
    private boolean success;
    private long executionTime;
    private String message;
    private Date timestamp;
}