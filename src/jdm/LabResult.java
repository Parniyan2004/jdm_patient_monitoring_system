package jdm;

/**
 * Stores the definition of one lab test (its name, group, and unit).
 * Does NOT contain the measured value - that is in Measurement.
 */
public class LabResult {

    private String labResultId;
    private String groupId;
    private String resultNameEn;
    private String unit;

    public LabResult(String labResultId, String groupId, String resultNameEn, String unit) {
        this.labResultId = labResultId;
        this.groupId = groupId;
        this.resultNameEn = resultNameEn;
        this.unit = unit;
    }

    public String getLabResultId() {
        return labResultId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getResultNameEn() {
        return resultNameEn;
    }

    public String getUnit() {
        return unit;
    }
}
