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
	 * Enumeration of the type of chunk. Depending on the listing style, it may
	 * be all collections, all data objects, or represent a mix at the point of
	 * transition. For continuous paging it may make no such distinction. This
	 * is contingent on the virtual collection, and up to the renderer to decide
	 * how to handle. NONE indicates that no effort is made to characterize the
	 * chunks.
	 * 
	 * @author Mike Conway - DICE
	 *
	 */
	public enum ChunkType {
		NONE, COLLECTION, DATA_OBJECT, MIXED, CONTINUOUS
	}

	/**
	 * 
	 * If you think of the chunk as a hint at desigining navigation through
	 * links or buttons, this enumeration describes the type of link or button.
	 * Chunks are laid out:
	 * 
	 * FIRST-SKIP_BACKWARD-NUMBERED-NUMBERED-CURRENT-NUMBERED-NUMBERED-SKIP_FORWARD-LAST
	 * 
	 * Where the current page is in the center, and some number of 'numbered'
	 * chunks bracket it. Then there are 'elipses' or similar that move the
	 * center node. Additional chunks on the shoulder of the center chunk are
	 * dictated by the maxChunks specified when building. That maxChunks is also
	 * the distance in chunks that is moved when skipping.
	 *
	 */
	public enum ChunkPosition {
		FIRST, SKIP_BACKWARD, NUMBERED, CURRENT, SKIP_FORWARD, LAST
	}

	private int chunkNumber = 0;
	private int offset = 0;
	private String link = "";
	private ChunkType chunkType = ChunkType.NONE;
	private ChunkPosition chunkPosition = ChunkPosition.NUMBERED;

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
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return the chunkType
	 */
	public ChunkType getChunkType() {
		return chunkType;
	}

	/**
	 * @param chunkType
	 *            the chunkType to set
	 */
	public void setChunkType(ChunkType chunkType) {
		this.chunkType = chunkType;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PagingChunk [chunkNumber=").append(chunkNumber).append(", offset=").append(offset).append(", ");
		if (link != null) {
			builder.append("link=").append(link).append(", ");
		}
		if (chunkType != null) {
			builder.append("chunkType=").append(chunkType).append(", ");
		}
		if (chunkPosition != null) {
			builder.append("chunkPosition=").append(chunkPosition);
		}
		builder.append("]");
		return builder.toString();
	}

}
