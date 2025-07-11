package ma.fstt.springoracle.integration;

import ma.fstt.springoracle.dto.UserDTO;
import ma.fstt.springoracle.model.OracleUser;
import ma.fstt.springoracle.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateAndRetrieveUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("TestPass123!");
        userDTO.setDefaultTablespace("USERS");
        userDTO.setTemporaryTablespace("TEMP");
        userDTO.setQuotaLimit("100M");

        OracleUser createdUser = userService.createUser(userDTO);
        
        assertNotNull(createdUser);
        assertEquals("TESTUSER", createdUser.getUsername());
        
        assertTrue(userService.getUser("TESTUSER").isPresent());
    }

    @Test
    void testLockUnlockUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("locktest");
        userDTO.setPassword("TestPass123!");
        userDTO.setDefaultTablespace("USERS");
        userDTO.setTemporaryTablespace("TEMP");

        userService.createUser(userDTO);
        
        assertDoesNotThrow(() -> userService.lockAccount("LOCKTEST"));
        assertDoesNotThrow(() -> userService.unlockAccount("LOCKTEST"));
    }
}