package ApplicationTier.Model;

import java.util.Date;

public class TestResult {
    private int resultId;
    private int orderId;
    private int testTypeId;
    private int technicianId;
    private String value;
    private String interpretation;
    private boolean isValidated;
    private Date resultDate;
    private String testName;

    public TestResult() {}

    public TestResult(int orderId, int testTypeId, int technicianId) {
        this.orderId = orderId;
        this.testTypeId = testTypeId;
        this.technicianId = technicianId;
        this.isValidated = false;
    }

    public int getResultId() { return resultId; }
    public void setResultId(int id) { this.resultId = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int id) { this.orderId = id; }

    public int getTestTypeId() { return testTypeId; }
    public void setTestTypeId(int id) { this.testTypeId = id; }

    public int getTechnicianId() { return technicianId; }
    public void setTechnicianId(int id) { this.technicianId = id; }

    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String i) { this.interpretation = i; }

    public boolean isValidated() { return isValidated; }
    public void setValidated(boolean v) { this.isValidated = v; }

    public Date getResultDate() { return resultDate; }
    public void setResultDate(Date d) { this.resultDate = d; }

    public String getTestName() { return testName; }
    public void setTestName(String n) { this.testName = n; }
}
