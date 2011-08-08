package org.irods.jargon.transfer.dao.domain;

public enum FrequencyType {
	
	EVERY_TWO_MINUTES("Every 2 minutes"),

    EVERY_FIFTEEN_MINUTES("Every 15 minutes"),

    EVERY_HOUR("Every hour"),

    EVERY_DAY("Every day"),

    EVERY_WEEK("Every week");
    
    

    private String readableName;

    private FrequencyType(String readableName) {
        this.readableName = readableName;
    }

    /**
     * @return the readableName
     */
    public String getReadableName() {
        return readableName;
    }

    /**
     * @param readableName
     *            the readableName to set
     */
    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

}
