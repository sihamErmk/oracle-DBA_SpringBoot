package ma.fstt.springoracle.service;

import ma.fstt.springoracle.dto.PrivilegeDTO;
import ma.fstt.springoracle.model.Privilege;
import java.util.List;
import java.util.Optional;

public interface PrivilegeService {

    Privilege createPrivilege(PrivilegeDTO privilegeDTO);
    Optional<Privilege> getPrivilege(String name);
    List<Privilege> getAllPrivileges();
    void deletePrivilege(String name);


    void grantSystemPrivilege(String privilegeName, String userName, boolean withAdminOption);
    void grantObjectPrivilege(String privilegeName, String objectName, String userName);

    void revokeSystemPrivilege(String privilegeName, String userName);
    void revokeObjectPrivilege(String privilegeName, String objectName, String userName);

    List<String> getGrantedSystemPrivileges(String userName);
    List<String> getGrantedObjectPrivileges(String userName);
}