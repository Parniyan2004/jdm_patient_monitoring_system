package jdm;

/**
 * Stores one recorded measurement: which test, when, and what value.
 * The value is a String because some results are text, not numbers.
 */
public class Measurement {

    private String measurementId;
    private String labResultId;
    private String dateTime;
    private String value;

    public Measurement(String measurementId, String labResultId,
                       String dateTime, String value) {
        this.measurementId = measurementId;
        this.labResultId = labResultId;
        this.dateTime = dateTime;
        this.value = value;
    }

    public String getMeasurementId() {
        return measurementId;
    }

    public String getLabResultId() {
        return labResultId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getValue() {
        return value;
    }
}
