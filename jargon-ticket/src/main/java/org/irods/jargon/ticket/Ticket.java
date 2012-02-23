package org.irods.jargon.ticket;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

public class Ticket {

	String ticketId;
	String ticketString;
	TicketCreateModeEnum type;
	String objectType; // data or collection??
	String ownerName;
	String ownerZone;
	Integer usesCount;
	Integer usesLimit;
	Integer writeFileCount;
	Integer writeFileLimit;
	Integer writeByteCount;
	Integer writeByteLimit;
	Date expireTime;
	String dataObjectName;
	String dataObjectCollection;
	List<String> userRestrictions;
	List<String> groupRestrictions;
	List<String> hostRestrictions;
	
	public Ticket() {
		this.expireTime = null;
	}
	
	public Ticket(IRODSQueryResultRow row) throws JargonException {
		DateFormat dateFormat = DateFormat.getInstance();
		this.expireTime = null;
		String date = "";

		try {
			setTicketId(row.getColumn(RodsGenQueryEnum.COL_TICKET_ID.getName()));
			setTicketString(row.getColumn(RodsGenQueryEnum.COL_TICKET_STRING.getName()));
			setType(TicketCreateModeEnum.findTypeByString(row.getColumn(RodsGenQueryEnum.COL_TICKET_TYPE.getName())));
			setObjectType(row.getColumn(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName()));
			setOwnerName(row.getColumn(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName()));
			setOwnerZone(row.getColumn(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName()));
			setUsesCount(Integer.valueOf(row.getColumn(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName())));
			setUsesLimit(Integer.valueOf(row.getColumn(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName())));
			setWriteFileCount(Integer.valueOf(row.getColumn(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT.getName())));
			setWriteFileLimit(Integer.valueOf(row.getColumn(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT.getName())));
			setWriteByteCount(Integer.valueOf(row.getColumn(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT.getName())));
			setWriteByteLimit(Integer.valueOf(row.getColumn(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT.getName())));
			date = row.getColumn(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName());
			if((date != null) && (!date.isEmpty())) {
				setExpireTime(dateFormat.parse(row.getColumn(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName())));
			}
			setDataObjectName(row.getColumn(RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName()));
			setDataCollection(row.getColumn(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName()));
//			+ RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName()
//			+ RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName()
// TODO: not sure to ask for these
//			+ ", "
//			+ RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME.getName()
//			+ ", "
//			+ RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME.getName()
//			+ ", "
//			+ RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST.getName()

		} catch (ParseException e) {
			this.expireTime = null;
		}
	}
	
	public String getTicketId() {
		return ticketId;
	}
	
	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}
	
	public String getTicketString() {
		return ticketString;
	}
	
	public void setTicketString(String ticketString) {
		this.ticketString = ticketString;
	}
	
	public TicketCreateModeEnum getType() {
		return type;
	}
	
	public void setType(TicketCreateModeEnum type) {
		this.type = type;
	}
	
	public String getObjectType() {
		return objectType;
	}
	
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public String getOwnerZone() {
		return ownerZone;
	}
	
	public void setOwnerZone(String ownerZone) {
		this.ownerZone = ownerZone;
	}
	
	public Integer getUsesCount() {
		return usesCount;
	}

	public void setUsesCount(Integer usesCount) {
		this.usesCount = usesCount;
	}

	public Integer getUsesLimit() {
		return usesLimit;
	}

	public void setUsesLimit(Integer usesLimit) {
		this.usesLimit = usesLimit;
	}

	public Integer getWriteFileCount() {
		return writeFileCount;
	}

	public void setWriteFileCount(Integer writeFileCount) {
		this.writeFileCount = writeFileCount;
	}

	public Integer getWriteFileLimit() {
		return writeFileLimit;
	}

	public void setWriteFileLimit(Integer writeFileLimit) {
		this.writeFileLimit = writeFileLimit;
	}

	public Integer getWriteByteCount() {
		return writeByteCount;
	}

	public void setWriteByteCount(Integer writeByteCount) {
		this.writeByteCount = writeByteCount;
	}

	public Integer getWriteByteLimit() {
		return writeByteLimit;
	}

	public void setWriteByteLimit(Integer writeByteLimit) {
		this.writeByteLimit = writeByteLimit;
	}
	
	public Date getExpireTime() {
		return expireTime;
	}
	
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	public String getDataObjectName() {
		return dataObjectName;
	}
	
	public void setDataObjectName(String dataObjectName) {
		this.dataObjectName = dataObjectName;
	}
	
	public String getDataCollection() {
		return dataObjectCollection;
	}
	
	public void setDataCollection(String dataCollection) {
		this.dataObjectCollection = dataCollection;
	}
	
	public List<String> getUserRestrictions() {
		return userRestrictions;
	}

	public void setUserRestrictions(List<String> userRestrictions) {
		this.userRestrictions = userRestrictions;
	}
	
	public List<String> getGroupRestrictions() {
		return groupRestrictions;
	}
	
	public void setGroupRestrictions(List<String> groupRestrictions) {
		this.groupRestrictions = groupRestrictions;
	}
	
	public List<String> getHostRestrictions() {
		return hostRestrictions;
	}
	
	public void setHostRestrictions(List<String> hostRestrictions) {
		this.hostRestrictions = hostRestrictions;
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
		sb.append("\n    data object name:");
		sb.append(dataObjectName);
		sb.append("\n   data object collection:");
		sb.append(dataObjectCollection);
		
		if (userRestrictions.isEmpty()) {
			sb.append("\n	no user restrictions");
		}
		else {
			for (String user : userRestrictions) {
				sb.append("\nrestricted-to user:");
				sb.append(user);
			}
		}
		
		if (groupRestrictions.isEmpty()) {
			sb.append("\n	no group restrictions");
		}
		else {
			for (String group : groupRestrictions) {
				sb.append("\nrestricted-to group:");
				sb.append(group);
			}
		}
		if (hostRestrictions.isEmpty()) {
			sb.append("\n	no host restrictions");
		}
		else {
			for (String host : hostRestrictions) {
				sb.append("\nrestricted-to host:");
				sb.append(host);
			}
		}
		return sb.toString();
	}
}
