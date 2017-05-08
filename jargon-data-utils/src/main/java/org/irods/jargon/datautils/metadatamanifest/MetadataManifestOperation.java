package org.irods.jargon.datautils.metadatamanifest;

import org.irods.jargon.datautils.metadatamanifest.MetadataManifest.Action;

/**
 * Nested individual operation
 * @author mcc
 *
 */
public class MetadataManifestOperation {
	
	private final String attribute;
	private final String value;
	private final String unit;
	private final String irodsPath;
	private final Action action;
	
	/**
	 * Create an operation 
	 * @param attribute <code>String</code> with the avu attribute
	 * @param value <code>String</code> with the avu value
	 * @param unit <code>String</code> with the avu unit
	 * @param irodsPath <code>String</code> with the target iRODS path. If the containing {@link MetadataManifest} supplies a <code>parentIrodsTargetPath</code>
	 * then this path should be relative with no leading / character
	 * @param action @link Action} enum value
	 */
	public MetadataManifestOperation(String attribute, String value, String unit, String irodsPath, Action action) {
		super();
		this.attribute = attribute;
		this.value = value;
		this.unit = unit;
		this.irodsPath = irodsPath;
		this.action = action;
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
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataManifestOperation [");
		if (attribute != null)
			builder.append("attribute=").append(attribute).append(", ");
		if (value != null)
			builder.append("value=").append(value).append(", ");
		if (unit != null)
			builder.append("unit=").append(unit).append(", ");
		if (irodsPath != null)
			builder.append("irodsPath=").append(irodsPath).append(", ");
		if (action != null)
			builder.append("action=").append(action);
		builder.append("]");
		return builder.toString();
	}
	
}
