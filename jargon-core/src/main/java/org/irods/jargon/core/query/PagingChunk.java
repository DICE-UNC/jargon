/**
 * 
 */
package org.irods.jargon.core.query;

/**
 * Represents a page that can be navigated to in a
 * {@link PagingAwareCollectionListingDescriptor}
 * 
 * @author Mike Conway - DICE
 *
 */
public class PagingChunk {

	/**
	 * 
	 * If you think of the chunk as a hint at displaying navigation through links or
	 * buttons, this enumeration describes the type of link or button.
	 * 
	 */
	public enum ChunkPosition {
		NORMAL, CURRENT
	}

	/**
	 * sequential index of the chunk
	 */
	private int chunkNumber = 0;

	/**
	 * Offset into the total number of records for the chunk (0 based)
	 */
	private int offset = 0;

	/**
	 * Enum describing the type of chunk, which maps conceptually to radio and
	 * transport buttons like first, prev, next, last or numeric position buttons
	 */
	private ChunkPosition chunkPosition = ChunkPosition.NORMAL;

	/**
	 * Whether the chunk is conceptually enabled in the current display, e.g. there
	 * may be a PREVIOUS chunk, but in the current display of the first page it
	 * would not conceptually be enabled
	 */
	private boolean enabled = true;

	/**
	 * @return the chunkNumber
	 */
	public int getChunkNumber() {
		return chunkNumber;
	}

	/**
	 * @param chunkNumber
	 *            the chunkNumber to set
	 */
	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * @return the chunkPosition
	 */
	public ChunkPosition getChunkPosition() {
		return chunkPosition;
	}

	/**
	 * @param chunkPosition
	 *            the chunkPosition to set
	 */
	public void setChunkPosition(ChunkPosition chunkPosition) {
		this.chunkPosition = chunkPosition;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PagingChunk [chunkNumber=").append(chunkNumber).append(", offset=").append(offset).append(", ");
		if (chunkPosition != null) {
			builder.append("chunkPosition=").append(chunkPosition).append(", ");
		}
		builder.append("enabled=").append(enabled).append("]");
		return builder.toString();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
