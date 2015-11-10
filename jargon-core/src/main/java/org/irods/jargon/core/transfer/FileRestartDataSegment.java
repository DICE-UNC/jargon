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
	/**
	 * Current lenght of the restart
	 */
	private long length = 0L;
	/**
	 * Current offset of the restart
	 */
	private long offset = 0L;
	/**
	 * thread number associated with this thread
	 */
	private final int threadNumber;

	public FileRestartDataSegment(final int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public long getLength() {
		return length;
	}

	public void setLength(final long length) {
		this.length = length;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(final long offset) {
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

	/**
	 * @return the threadNumber
	 */
	public int getThreadNumber() {
		return threadNumber;
	}

}
