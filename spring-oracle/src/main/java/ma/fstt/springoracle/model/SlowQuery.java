package ma.fstt.springoracle.model;



import java.sql.Timestamp;

public class SlowQuery {
    private String sqlId;
    private String sqlText;
    private double avgExecutionTime;
    private int executions;
    private Timestamp lastActiveTime;

    // Getters et setters
    public String getSqlId() { return sqlId; }
    public void setSqlId(String sqlId) { this.sqlId = sqlId; }

    public String getSqlText() { return sqlText; }
    public void setSqlText(String sqlText) { this.sqlText = sqlText; }

    public double getAvgExecutionTime() { return avgExecutionTime; }
    public void setAvgExecutionTime(double avgExecutionTime) { this.avgExecutionTime = avgExecutionTime; }

    public int getExecutions() { return executions; }
    public void setExecutions(int executions) { this.executions = executions; }

    public Timestamp getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(Timestamp lastActiveTime) { this.lastActiveTime = lastActiveTime; }
}