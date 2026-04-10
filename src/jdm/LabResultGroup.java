package jdm;

/**
 * Stores the name of a lab result group (e.g. Blood Chemistry, Hematology).
 */
public class LabResultGroup {

    private String groupId;
    private String groupName;

    public LabResultGroup(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }
}
