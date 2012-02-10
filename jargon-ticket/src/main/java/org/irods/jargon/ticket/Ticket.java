package org.irods.jargon.ticket;

import java.util.Date;
import java.util.List;

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
