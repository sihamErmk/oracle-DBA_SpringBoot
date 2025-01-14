package ma.fstt.springoracle.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RoleDTO {
    private String name;
    private String description;
    private Set<String> privileges;
}