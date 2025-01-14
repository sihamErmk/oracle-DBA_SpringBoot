package ma.fstt.springoracle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public String testConnection() {
        String result = jdbcTemplate.queryForObject(
                "SELECT sys_context('USERENV', 'SESSION_USER') FROM dual",
                String.class
        );
        return "Connecté en tant que: " + result;
    }
}
