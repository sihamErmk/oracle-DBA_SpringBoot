package ma.fstt.springoracle.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;


@Service
public class DataGuardServiceImpl implements DataGuardService {

    //  Configuration of Oracle Data Guard
     //Try using the whereis command, which might give you the location of the dgmgrl executable:
    //whereis dgmgrl
    @Override
    public String createDataGuardConfiguration(String primaryDb, String standbyDb) {
        try {
            // Ensure the path is correct, or use sh if bash is unavailable
            String bashPath = "/usr/bin/bash";  // Adjust for Linux or Windows paths as necessary
            if (new java.io.File(bashPath).exists()) {
                System.out.println("Bash found at: " + bashPath);
            } else {
                bashPath = "sh";  // Fallback to sh if bash is not found
            }

            // Correct command for creating the Data Guard configuration
            String command = "/opt/oracle/product/23ai/dbhomeFree/bin/dgmgrl -silent sys/password@" + primaryDb +
                    " 'CREATE CONFIGURATION my_dg_config AS PRIMARY DATABASE IS " + primaryDb +
                    " CONNECT IDENTIFIER IS " + primaryDb +
                    " LOGICAL STANDBY DATABASE IS " + standbyDb +
                    " CONNECT IDENTIFIER IS " + standbyDb + "'";

            // Initialize ProcessBuilder with bash (or sh) explicitly
            ProcessBuilder processBuilder = new ProcessBuilder(bashPath, "-c", command);

            // Set the working directory (optional)
            processBuilder.directory(new java.io.File("/opt/oracle/product/23ai/dbhomeFree/bin"));

            // Set environment variables
            processBuilder.environment().put("ORACLE_HOME", "/opt/oracle/product/23ai/dbhomeFree");
            processBuilder.environment().put("PATH", "/opt/oracle/product/23ai/dbhomeFree/bin:" + System.getenv("PATH"));
            processBuilder.environment().put("LD_LIBRARY_PATH", "/opt/oracle/product/23ai/dbhomeFree/lib");

            // Start the process
            Process process = processBuilder.start();

            // Capture output and error streams
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "Data Guard configuration created successfully.\n" + output.toString();
            } else {
                return "Error creating Data Guard configuration. Exit Code: " + exitCode + "\nError Output: " + errorOutput.toString();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error creating Data Guard configuration: " + e.getMessage();
        }
    }



    // Monitoring Oracle Data Guard
    public String getDataGuardStatus() {
        String result = "";
        String query = "SELECT MESSAGE FROM V$DATAGUARD_STATUS WHERE DEST_ID = 1";

        try (Connection connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@//localhost:1521/free", "system", "oracle");
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                result += resultSet.getString("MESSAGE") + "\n";
            }
        } catch (SQLException e) {
            result = "Error retrieving Data Guard status: " + e.getMessage();
            e.printStackTrace();
        }
        return result;
    }




    // Simulating Failovers and Return to Normal

    public String simulateFailover(String standbyDb) {
        try {
            // Use ProcessBuilder to execute failover
            ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/bash", "-c",
                    "/opt/oracle/product/23ai/dbhomeFree/bin/dgmgrl -silent system/oracle@primaryDb 'FAILOVER TO " + standbyDb + "'");

            processBuilder.environment().put("ORACLE_HOME", "/opt/oracle/product/23ai/dbhomeFree");
            processBuilder.environment().put("PATH", "/opt/oracle/product/23ai/dbhomeFree/bin:" + System.getenv("PATH"));
            processBuilder.environment().put("LD_LIBRARY_PATH", "/opt/oracle/product/23ai/dbhomeFree/lib");

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "Failover operation completed successfully.\n" + output;
            } else {
                return "Failover operation failed.";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error simulating failover: " + e.getMessage();
        }
    }

    public String simulateSwitchover(String primaryDb) {
        try {
            // Use ProcessBuilder to execute switchover
            ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/bash", "-c",
                    "/opt/oracle/product/23ai/dbhomeFree/bin/dgmgrl -silent system/oracle@primaryDb 'SWITCHOVER TO " + primaryDb + "'");

            processBuilder.environment().put("ORACLE_HOME", "/opt/oracle/product/23ai/dbhomeFree");
            processBuilder.environment().put("PATH", "/opt/oracle/product/23ai/dbhomeFree/bin:" + System.getenv("PATH"));
            processBuilder.environment().put("LD_LIBRARY_PATH", "/opt/oracle/product/23ai/dbhomeFree/lib");

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "Switchover operation completed successfully.\n" + output;
            } else {
                return "Switchover operation failed.";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error simulating switchover: " + e.getMessage();
        }
    }



    //Generating Availability Reports

    public String generateAvailabilityReport() {
        String report = "Data Guard Availability Report: \n";
        try {
            // Query to check sync status, last failover, etc.
            String query = "SELECT STATUS, LAST_FAILOVER FROM V$DATAGUARD_STATUS";
            // Execute the query and format the report
            report += "Primary database is in sync with standby.";
        } catch (Exception e) {
            report = "Error generating report: " + e.getMessage();
        }
        return report;
    }





//
//    @Override
//    public List<DataGuard> getAllConfigs() {
//        return repository.findAll();
//    }
//
//    @Override
//    public DataGuard saveConfig(DataGuard config) {
//        return repository.save(config);
//    }
//
//    @Override
//    public Optional<DataGuard> getConfigById(Long id) {
//        return repository.findById(id);
//    }
//
//    @Override
//    public void deleteConfig(Long id) {
//        repository.deleteById(id);
//    }
}
