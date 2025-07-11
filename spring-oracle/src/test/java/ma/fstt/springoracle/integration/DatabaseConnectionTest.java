package ma.fstt.springoracle.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnection() {
        String result = jdbcTemplate.queryForObject("SELECT 'Connected' FROM DUAL", String.class);
        assertEquals("Connected", result);
    }

    @Test
    void testOracleVersion() {
        String version = jdbcTemplate.queryForObject(
            "SELECT BANNER FROM V$VERSION WHERE ROWNUM = 1", String.class);
        assertNotNull(version);
        assertTrue(version.contains("Oracle"));
    }
}