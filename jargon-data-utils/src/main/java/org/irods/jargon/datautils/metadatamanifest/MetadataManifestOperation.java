package org.irods.jargon.datautils.metadatamanifest;

import org.irods.jargon.datautils.metadatamanifest.MetadataManifest.Action;

/**
 * Nested individual operation
 *
 * @author mcc
 *
 */
public class MetadataManifestOperation {

	private String attribute = "";
	private String value = "";
	private String unit = "";
	private String irodsPath = "";
	private Action action = Action.ADD;

	/**
	 * Create an operation
	 *
	 * @param attribute
	 *            <code>String</code> with the avu attribute
	 * @param value
	 *            <code>String</code> with the avu value
	 * @param unit
	 *            <code>String</code> with the avu unit
	 * @param irodsPath
	 *            <code>String</code> with the target iRODS path. If the containing
	 *            {@link MetadataManifest} supplies a
	 *            <code>parentIrodsTargetPath</code> then this path should be
	 *            relative with no leading / character
	 * @param action
	 * @link Action} enum value
	 */
	public MetadataManifestOperation(final String attribute, final String value, final String unit,
			final String irodsPath, final Action action) {
		super();
		this.attribute = attribute;
		this.value = value;
		this.unit = unit;
		this.irodsPath = irodsPath;
		this.action = action;
	}

	public MetadataManifestOperation() {

	}

	public String getIrodsPath() {
		return irodsPath;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}

	public String getUnit() {
		return unit;
	}

	public Action getAction() {
		return action;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MetadataManifestOperation [");
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value).append(", ");
		}
		if (unit != null) {
			builder.append("unit=").append(unit).append(", ");
		}
		if (irodsPath != null) {
			builder.append("irodsPath=").append(irodsPath).append(", ");
		}
		if (action != null) {
			builder.append("action=").append(action);
		}
		builder.append("]");
		return builder.toString();
	}

	public void setAttribute(final String attribute) {
		this.attribute = attribute;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public void setIrodsPath(final String irodsPath) {
		this.irodsPath = irodsPath;
	}

	public void setAction(final Action action) {
		this.action = action;
	}

}
