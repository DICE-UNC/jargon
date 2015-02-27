/**
 * 
 */
package org.irods.jargon.core.transfer;

/**
 * Data segment in iRODS file, analagous to dataSeg_t in iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public class FileRestartDataSegment {
	private long length = 0L;
	private long offset = 0L;

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileRestartDataSegment [length=");
		builder.append(length);
		builder.append(", offset=");
		builder.append(offset);
		builder.append("]");
		return builder.toString();
	}
}
