package org.irods.jargon.ticket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

/**
 * Represents a ticket (temporary access to a file or collection) in iRODS.
 * 
 * @author Lisa Stillwell (RENCI)
 * 
 */
public class Ticket extends IRODSDomainObject {

	public enum TicketObjectType {
		DATA_OBJECT, COLLECTION
	}

	private String ticketId;
	private String ticketString;
	private TicketCreateModeEnum type;
	private TicketObjectType objectType;
	private String ownerName;
	private String ownerZone;
	private int usesCount;
	private int usesLimit;
	private int writeFileCount;
	private int writeFileLimit;
	private long writeByteCount;
	private long writeByteLimit;
	private Date expireTime;
	private String irodsAbsolutePath;

	/*
	 * Default (no values) constructor
	 */
	public Ticket() {
	}


	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Ticket:");
		sb.append("\n   id:");
		sb.append(ticketId);
		sb.append("\n   string:");
		sb.append(ticketString);
		sb.append("\n   type:");
		sb.append(type);
		sb.append("\n   object type:");
		sb.append(objectType);
		sb.append("\n   owner name:");
		sb.append(ownerName);
		sb.append("\n   owner zone:");
		sb.append(ownerZone);
		sb.append("\n   uses count:");
		sb.append(usesCount);
		sb.append("\n   uses limit:");
		sb.append(usesLimit);
		sb.append("\n   write file count:");
		sb.append(writeFileCount);
		sb.append("\n   write file limit:");
		sb.append(writeFileLimit);
		sb.append("\n   write byte count:");
		sb.append(writeByteCount);
		sb.append("\n   write byte limit:");
		sb.append(writeByteLimit);
		sb.append("\n   expire time:");
		sb.append(expireTime);
		sb.append("\n    irodsAbsolutePath:");
		sb.append(irodsAbsolutePath);

		return sb.toString();
	}

	/**
	 * @return the ticketId
	 */
	public String getTicketId() {
		return ticketId;
	}

	/**
	 * @param ticketId
	 *            the ticketId to set
	 */
	public void setTicketId(final String ticketId) {
		this.ticketId = ticketId;
	}

	/**
	 * @return the ticketString
	 */
	public String getTicketString() {
		return ticketString;
	}

	/**
	 * @param ticketString
	 *            the ticketString to set
	 */
	public void setTicketString(final String ticketString) {
		this.ticketString = ticketString;
	}

	/**
	 * @return the type
	 */
	public TicketCreateModeEnum getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(final TicketCreateModeEnum type) {
		this.type = type;
	}

	/**
	 * @return the objectType
	 */
	public TicketObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(final TicketObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return ownerName;
	}

	/**
	 * @param ownerName
	 *            the ownerName to set
	 */
	public void setOwnerName(final String ownerName) {
		this.ownerName = ownerName;
	}

	/**
	 * @return the ownerZone
	 */
	public String getOwnerZone() {
		return ownerZone;
	}

	/**
	 * @param ownerZone
	 *            the ownerZone to set
	 */
	public void setOwnerZone(final String ownerZone) {
		this.ownerZone = ownerZone;
	}

	/**
	 * @return the usesCount
	 */
	public int getUsesCount() {
		return usesCount;
	}

	/**
	 * @param usesCount
	 *            the usesCount to set
	 */
	public void setUsesCount(final int usesCount) {
		this.usesCount = usesCount;
	}

	/**
	 * @return the usesLimit
	 */
	public int getUsesLimit() {
		return usesLimit;
	}

	/**
	 * @param usesLimit
	 *            the usesLimit to set
	 */
	public void setUsesLimit(final int usesLimit) {
		this.usesLimit = usesLimit;
	}

	/**
	 * @return the writeFileCount
	 */
	public int getWriteFileCount() {
		return writeFileCount;
	}

	/**
	 * @param writeFileCount
	 *            the writeFileCount to set
	 */
	public void setWriteFileCount(final int writeFileCount) {
		this.writeFileCount = writeFileCount;
	}

	/**
	 * @return the writeFileLimit
	 */
	public int getWriteFileLimit() {
		return writeFileLimit;
	}

	/**
	 * @param writeFileLimit
	 *            the writeFileLimit to set
	 */
	public void setWriteFileLimit(final int writeFileLimit) {
		this.writeFileLimit = writeFileLimit;
	}

	/**
	 * @return the writeByteCount
	 */
	public long getWriteByteCount() {
		return writeByteCount;
	}

	/**
	 * @param writeByteCount
	 *            the writeByteCount to set
	 */
	public void setWriteByteCount(final long writeByteCount) {
		this.writeByteCount = writeByteCount;
	}

	/**
	 * @return the writeByteLimit
	 */
	public long getWriteByteLimit() {
		return writeByteLimit;
	}

	/**
	 * @param writeByteLimit
	 *            the writeByteLimit to set
	 */
	public void setWriteByteLimit(final long writeByteLimit) {
		this.writeByteLimit = writeByteLimit;
	}

	/**
	 * @return the expireTime
	 */
	public Date getExpireTime() {
		return expireTime;
	}
	
	/**
	 * @return formatted date string - like one displayed with iticket
	 */
	public String getFormattedExpireTime() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
		String formattedDate = df.format(expireTime.getTime());
		return formattedDate;
	}

	/**
	 * @param expireTime
	 *            the expireTime to set
	 */
	public void setExpireTime(final Date expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * @return the irodsAbsolutePath
	 */
	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	/**
	 * @param irodsAbsolutePath
	 *            the irodsAbsolutePath to set
	 */
	public void setIrodsAbsolutePath(final String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}
}
