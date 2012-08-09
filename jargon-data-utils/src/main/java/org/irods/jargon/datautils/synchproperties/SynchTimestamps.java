package org.irods.jargon.datautils.synchproperties;

/**
 * Local and iRODS timestamps at a certain synch point
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class SynchTimestamps {

	private final long localSynchTimestamp;

	private final long irodsSynchTimestamp;

	public SynchTimestamps(final long localSynchTimestamp,
			final long irodsSynchTimestamp) {

		if (localSynchTimestamp < 0) {
			throw new IllegalArgumentException(
					"localSynchTimestamp less than zero");
		}

		if (irodsSynchTimestamp < 0) {
			throw new IllegalArgumentException(
					"irodsSynchTimestamp less than zero");
		}

		this.localSynchTimestamp = localSynchTimestamp;
		this.irodsSynchTimestamp = irodsSynchTimestamp;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SynchTimestamps");
		sb.append("\n   local:");
		sb.append(localSynchTimestamp);
		sb.append("\n   irods:");
		sb.append(irodsSynchTimestamp);
		return sb.toString();
	}

	public long getLocalSynchTimestamp() {
		return localSynchTimestamp;
	}

	public long getIrodsSynchTimestamp() {
		return irodsSynchTimestamp;
	}

}
