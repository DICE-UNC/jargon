/**
 * 
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Representation of an iRODS DataObjInfo tag
 * 
 * @author conwaymc
 *
 */
public class DataObjInfo extends AbstractIRODSPackingInstruction {

	public static final int INFO_AN = 630;
	public static final String PI_TAG = "DataObjInfo_PI";

	private String objPath = "";
	private String rescName = "";
	private String rescHier = "";
	private String dataType = "";
	private double dataSize = 0d;
	private String checksum = "";
	private String version = "";
	private String filePath = "";
	private String dataOwnerName = "";
	private String dataOwnerZone = "";
	private int replNum = 0;
	private int replStatus = 0;
	private String statusString = "";
	private int dataId = 0;
	private int collId = 0;
	private int dataMapId = 0;
	private int flags = 0;
	private String dataComments = "";
	private String dataMode = "";
	private String dataExpiry = "";
	private String dataCreate = "";
	private String dataModify = "";
	private String dataAccess = "";
	private int dataAccessInx = 0;
	private int writeFlag = 0;
	private String destRescName = "";
	private String backupRescName = "";
	private String subPath = "";
	private int specColl = 0;
	private int regUid = 0;
	private int otherFlags = 0;
	private String inPdmo = "";
	private int next = 0;
	private int rescId = 0;

	/**
	 * 
	 */
	public DataObjInfo() {
		super();
		setApiNumber(INFO_AN);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(DataObjInfo.PI_TAG,
				new Tag[] { new Tag("objPath", objPath), new Tag("rescName", rescName), new Tag("rescHier", rescHier),
						new Tag("dataType", dataType), new Tag("dataSize", dataSize), new Tag("chksum", checksum),
						new Tag("version", version), new Tag("filePath", filePath),
						new Tag("dataOwnerName", dataOwnerName), new Tag("dataOwnerZone", dataOwnerZone),
						new Tag("replNum", replNum), new Tag("replStatus", replStatus),
						new Tag("statusString", statusString), new Tag("dataId", dataId), new Tag("collId", collId),
						new Tag("dataMapId", dataMapId), new Tag("flags", flags), new Tag("dataComments", dataComments),
						new Tag("dataMode", dataMode), new Tag("dataExpiry", dataExpiry),
						new Tag("dataCreate", dataCreate), new Tag("dataModify", dataModify),
						new Tag("dataAccess", dataAccess), new Tag("dataAccessInx", dataAccessInx),
						new Tag("writeFlag", writeFlag), new Tag("destRescName", destRescName),
						new Tag("backupRescName", backupRescName), new Tag("subPath", subPath),
						new Tag("specColl", specColl), new Tag("regUid", regUid), new Tag("otherFlags", otherFlags) });
		// KVPS here
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		message.addTag(super.createKeyValueTag(kvps));
		message.addTag(new Tag("in_pdmo", inPdmo));
		message.addTag(new Tag("next", next));
		message.addTag(new Tag("rescId", rescId));
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataObjInfo [");
		if (objPath != null) {
			builder.append("objPath=").append(objPath).append(", ");
		}
		if (rescName != null) {
			builder.append("rescName=").append(rescName).append(", ");
		}
		if (rescHier != null) {
			builder.append("rescHier=").append(rescHier).append(", ");
		}
		if (dataType != null) {
			builder.append("dataType=").append(dataType).append(", ");
		}
		builder.append("dataSize=").append(dataSize).append(", ");
		if (checksum != null) {
			builder.append("checksum=").append(checksum).append(", ");
		}
		if (version != null) {
			builder.append("version=").append(version).append(", ");
		}
		if (filePath != null) {
			builder.append("filePath=").append(filePath).append(", ");
		}
		if (dataOwnerName != null) {
			builder.append("dataOwnerName=").append(dataOwnerName).append(", ");
		}
		if (dataOwnerZone != null) {
			builder.append("dataOwnerZone=").append(dataOwnerZone).append(", ");
		}
		builder.append("replNum=").append(replNum).append(", replStatus=").append(replStatus).append(", ");
		if (statusString != null) {
			builder.append("statusString=").append(statusString).append(", ");
		}
		builder.append("dataId=").append(dataId).append(", collId=").append(collId).append(", dataMapId=")
				.append(dataMapId).append(", flags=").append(flags).append(", ");
		if (dataComments != null) {
			builder.append("dataComments=").append(dataComments).append(", ");
		}
		if (dataMode != null) {
			builder.append("dataMode=").append(dataMode).append(", ");
		}
		if (dataExpiry != null) {
			builder.append("dataExpiry=").append(dataExpiry).append(", ");
		}
		if (dataCreate != null) {
			builder.append("dataCreate=").append(dataCreate).append(", ");
		}
		if (dataModify != null) {
			builder.append("dataModify=").append(dataModify).append(", ");
		}
		if (dataAccess != null) {
			builder.append("dataAccess=").append(dataAccess).append(", ");
		}
		builder.append("dataAccessInx=").append(dataAccessInx).append(", writeFlag=").append(writeFlag).append(", ");
		if (destRescName != null) {
			builder.append("destRescName=").append(destRescName).append(", ");
		}
		if (backupRescName != null) {
			builder.append("backupRescName=").append(backupRescName).append(", ");
		}
		if (subPath != null) {
			builder.append("subPath=").append(subPath).append(", ");
		}
		builder.append("specColl=").append(specColl).append(", regUid=").append(regUid).append(", otherFlags=")
				.append(otherFlags).append(", ");
		if (inPdmo != null) {
			builder.append("inPdmo=").append(inPdmo).append(", ");
		}
		builder.append("next=").append(next).append(", rescId=").append(rescId).append(", ");

		builder.append("]");
		return builder.toString();
	}

	public String getObjPath() {
		return objPath;
	}

	public void setObjPath(String objPath) {
		this.objPath = objPath;
	}

	public String getRescName() {
		return rescName;
	}

	public void setRescName(String rescName) {
		this.rescName = rescName;
	}

	public String getRescHier() {
		return rescHier;
	}

	public void setRescHier(String rescHier) {
		this.rescHier = rescHier;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public double getDataSize() {
		return dataSize;
	}

	public void setDataSize(double dataSize) {
		this.dataSize = dataSize;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDataOwnerName() {
		return dataOwnerName;
	}

	public void setDataOwnerName(String dataOwnerName) {
		this.dataOwnerName = dataOwnerName;
	}

	public String getDataOwnerZone() {
		return dataOwnerZone;
	}

	public void setDataOwnerZone(String dataOwnerZone) {
		this.dataOwnerZone = dataOwnerZone;
	}

	public int getReplNum() {
		return replNum;
	}

	public void setReplNum(int replNum) {
		this.replNum = replNum;
	}

	public int getReplStatus() {
		return replStatus;
	}

	public void setReplStatus(int replStatus) {
		this.replStatus = replStatus;
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public int getDataId() {
		return dataId;
	}

	public void setDataId(int dataId) {
		this.dataId = dataId;
	}

	public int getCollId() {
		return collId;
	}

	public void setCollId(int collId) {
		this.collId = collId;
	}

	public int getDataMapId() {
		return dataMapId;
	}

	public void setDataMapId(int dataMapId) {
		this.dataMapId = dataMapId;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public String getDataComments() {
		return dataComments;
	}

	public void setDataComments(String dataComments) {
		this.dataComments = dataComments;
	}

	public String getDataMode() {
		return dataMode;
	}

	public void setDataMode(String dataMode) {
		this.dataMode = dataMode;
	}

	public String getDataExpiry() {
		return dataExpiry;
	}

	public void setDataExpiry(String dataExpiry) {
		this.dataExpiry = dataExpiry;
	}

	public String getDataCreate() {
		return dataCreate;
	}

	public void setDataCreate(String dataCreate) {
		this.dataCreate = dataCreate;
	}

	public String getDataModify() {
		return dataModify;
	}

	public void setDataModify(String dataModify) {
		this.dataModify = dataModify;
	}

	public String getDataAccess() {
		return dataAccess;
	}

	public void setDataAccess(String dataAccess) {
		this.dataAccess = dataAccess;
	}

	public int getDataAccessInx() {
		return dataAccessInx;
	}

	public void setDataAccessInx(int dataAccessInx) {
		this.dataAccessInx = dataAccessInx;
	}

	public int getWriteFlag() {
		return writeFlag;
	}

	public void setWriteFlag(int writeFlag) {
		this.writeFlag = writeFlag;
	}

	public String getDestRescName() {
		return destRescName;
	}

	public void setDestRescName(String destRescName) {
		this.destRescName = destRescName;
	}

	public String getBackupRescName() {
		return backupRescName;
	}

	public void setBackupRescName(String backupRescName) {
		this.backupRescName = backupRescName;
	}

	public String getSubPath() {
		return subPath;
	}

	public void setSubPath(String subPath) {
		this.subPath = subPath;
	}

	public int getSpecColl() {
		return specColl;
	}

	public void setSpecColl(int specColl) {
		this.specColl = specColl;
	}

	public int getRegUid() {
		return regUid;
	}

	public void setRegUid(int regUid) {
		this.regUid = regUid;
	}

	public int getOtherFlags() {
		return otherFlags;
	}

	public void setOtherFlags(int otherFlags) {
		this.otherFlags = otherFlags;
	}

	public String getInPdmo() {
		return inPdmo;
	}

	public void setInPdmo(String inPdmo) {
		this.inPdmo = inPdmo;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public double getRescId() {
		return rescId;
	}

	public void setRescId(int rescId) {
		this.rescId = rescId;
	}

}
