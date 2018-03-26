/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.packinstr.CollInpForEmptyTrash.TrashOperationMode;

/**
 * Options for handling trash, passed in requests for detailed control of
 * emptying or restoring files in trash
 * 
 * @author conwaymc
 *
 */
public class TrashOptions {

	/**
	 * Indicates user versus admin function
	 */
	private TrashOperationMode trashOperationMode = TrashOperationMode.USER;

	/**
	 * remove the whole subtree; the collection, all data-objects in the collection,
	 * and any subcollections and sub-data-objects in the collection.
	 */
	private boolean recursive = true;
	/**
	 * Empty trash older than the given threshold in minutes. A -1 or 0 indicates
	 * that all trash is removed regardless of age
	 */
	private int ageInMinutes = -1;

	public TrashOptions() {
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public int getAgeInMinutes() {
		return ageInMinutes;
	}

	public void setAgeInMinutes(int ageInMinutes) {
		this.ageInMinutes = ageInMinutes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrashOptions [");
		if (trashOperationMode != null) {
			builder.append("trashOperationMode=").append(trashOperationMode).append(", ");
		}
		builder.append("recursive=").append(recursive).append(", ageInMinutes=").append(ageInMinutes).append("]");
		return builder.toString();
	}

	public TrashOperationMode getTrashOperationMode() {
		return trashOperationMode;
	}

	public void setTrashOperationMode(TrashOperationMode trashOperationMode) {
		this.trashOperationMode = trashOperationMode;
	}

}
