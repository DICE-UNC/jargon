/**
 * 
 */
package org.irods.jargon.core.pub.apiplugin.atomicmetadata;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Operation item for atomic metadata requests
 * 
 * @author conwaymc
 *
 */

public class AtomicMetadataOperation {

	private String operation = "";
	private String attribute = "";
	private String value = "";
	private String units = "";

	public AtomicMetadataOperation() {

	}

	@JsonGetter("operation")
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@JsonGetter("attribute")
	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	@JsonGetter("value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@JsonGetter("unit")
	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Operation [");
		if (operation != null) {
			builder.append("operation=").append(operation).append(", ");
		}
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value).append(", ");
		}
		if (units != null) {
			builder.append("units=").append(units);
		}
		builder.append("]");
		return builder.toString();
	}
}
