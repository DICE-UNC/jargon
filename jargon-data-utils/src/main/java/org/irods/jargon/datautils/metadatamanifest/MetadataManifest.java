/**
 * 
 */
package org.irods.jargon.datautils.metadatamanifest;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents metadata operations to be applied to files in iRODS
 * 
 * @author mcc
 *
 */
public class MetadataManifest {

	public enum Action {
		ADD
	}

	/**
	 * How to treat failures in each operation
	 * 
	 * @author mcc
	 *
	 */
	public enum FailureMode {
		IGNORE, FAIL_FAST
	}

	/**
	 * a list of operations, which is an AVU, an action, and a path
	 */
	private List<MetadataManifestOperation> operation = new ArrayList<>();

	private FailureMode failureMode = FailureMode.FAIL_FAST;

	/**
	 * a parent path. This should trigger processing of path information for
	 * each operation using the path information in each operation as a relative
	 * path underneath this parent path. Leaving this blank here will treat
	 * operation paths as absolute paths
	 */
	private String parentIrodsTargetPath = "";

	public String getParentIrodsTargetPath() {
		return parentIrodsTargetPath;
	}

	public void setParentIrodsTargetPath(final String parentIrodsTargetPath) {
		this.parentIrodsTargetPath = parentIrodsTargetPath;
	}

	public List<MetadataManifestOperation> getOperation() {
		return operation;
	}

	public void setOperation(final List<MetadataManifestOperation> operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		final StringBuilder builder = new StringBuilder();
		builder.append("MetadataManifest [");
		if (operation != null)
			builder.append("operation=").append(operation.subList(0, Math.min(operation.size(), maxLen))).append(", ");
		if (failureMode != null)
			builder.append("failureMode=").append(failureMode).append(", ");
		if (parentIrodsTargetPath != null)
			builder.append("parentIrodsTargetPath=").append(parentIrodsTargetPath);
		builder.append("]");
		return builder.toString();
	}

	public FailureMode getFailureMode() {
		return failureMode;
	}

	public void setFailureMode(final FailureMode failureMode) {
		this.failureMode = failureMode;
	}

}
