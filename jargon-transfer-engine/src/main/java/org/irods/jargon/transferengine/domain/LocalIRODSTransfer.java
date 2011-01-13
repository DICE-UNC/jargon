/**
 * 
 */
package org.irods.jargon.transferengine.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain object that represents a transfer activity between the local host and
 * an iRODS server.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalIRODSTransfer {

	public static final String TRANSFER_STATE_PROCESSING = "PROCESSING";
	public static final String TRANSFER_STATE_PAUSED = "PAUSED";
	public static final String TRANSFER_STATE_CANCELLED = "CANCELLED";
	public static final String TRANSFER_STATE_COMPLETE = "COMPLETE";
	public static final String TRANSFER_STATE_ENQUEUED = "ENQUEUED";

	public static final String TRANSFER_STATUS_ERROR = "ERROR";
	public static final String TRANSFER_STATUS_WARNING = "WARNING";
	public static final String TRANSFER_STATUS_OK = "OK";

	public static final String TRANSFER_TYPE_PUT = "PUT";
	public static final String TRANSFER_TYPE_GET = "GET";
	public static final String TRANSFER_TYPE_REPLICATE = "REPLICATE";

	private Long id;
	private String transferState;
	private String transferErrorStatus;
	private String transferType;
	private String transferHost = "";
	private int transferPort = 0;
	private String transferZone = "";
	private String transferResource = "";
	private String transferUserName = "";
	private String transferPassword = "";
	private Date transferStart;
	private Date transferEnd;
	private String localAbsolutePath = "";
	private String irodsAbsolutePath = "";
	private Set<LocalIRODSTransferItem> localIRODSTransferItems = new HashSet<LocalIRODSTransferItem>();
	private Date createdAt;
	private Date updatedAt;
	private String globalException = "";
	private String globalExceptionStackTrace = "";
	private String lastSuccessfulPath = "";
	private int totalFilesCount = 0;
	private int totalFilesTransferredSoFar = 0;

	public LocalIRODSTransfer() {

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LocalIRODSTransfer");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   transferState:");
		sb.append(transferState);
		sb.append("\n   transferErrorStatus:");
		sb.append(transferErrorStatus);
		sb.append("\n   globalException:");
		sb.append(globalException);
		sb.append("\n   lastSuccessfulPath:");
		sb.append(lastSuccessfulPath);
		sb.append("\n   totalFilesCount:");
		sb.append(totalFilesCount);
		sb.append("\n   totalFilesTransferredSoFar:");
		sb.append(totalFilesTransferredSoFar);
		sb.append("\n   transferType:");
		sb.append(transferType);
		sb.append("\n   transferHost:");
		sb.append(transferHost);
		sb.append("\n   transferResource:");
		sb.append(transferResource);
		sb.append("\n   transferUserName:");
		sb.append(transferUserName);
		sb.append("\n   transferStart:");
		sb.append(transferStart);
		sb.append("\n   transferEnd:");
		sb.append(transferEnd);
		sb.append("\n   localAbsolutePath:");
		sb.append(localAbsolutePath);
		sb.append("\n   irodsAbsolutePath:");
		sb.append(irodsAbsolutePath);
		sb.append("\n   createdAt:");
		sb.append(createdAt);
		sb.append("\n   updatedAt:");
		sb.append(updatedAt);
		return sb.toString();
	}

	public String getTransferState() {
		return transferState;
	}

	public void setTransferState(final String transferState) {
		this.transferState = transferState;
	}

	public String getTransferType() {
		return transferType;
	}

	public void setTransferType(final String transferType) {
		this.transferType = transferType;
	}

	public String getTransferHost() {
		return transferHost;
	}

	public void setTransferHost(final String transferHost) {
		this.transferHost = transferHost;
	}

	public int getTransferPort() {
		return transferPort;
	}

	public void setTransferPort(final int transferPort) {
		this.transferPort = transferPort;
	}

	public String getTransferZone() {
		return transferZone;
	}

	public void setTransferZone(final String transferZone) {
		this.transferZone = transferZone;
	}

	public String getTransferResource() {
		return transferResource;
	}

	public void setTransferResource(final String transferResource) {
		this.transferResource = transferResource;
	}

	public String getTransferUserName() {
		return transferUserName;
	}

	public void setTransferUserName(final String transferUserName) {
		this.transferUserName = transferUserName;
	}

	public Date getTransferStart() {
		return transferStart;
	}

	public void setTransferStart(final Date transferStart) {
		this.transferStart = transferStart;
	}

	public Date getTransferEnd() {
		return transferEnd;
	}

	public void setTransferEnd(final Date transferEnd) {
		this.transferEnd = transferEnd;
	}

	public String getLocalAbsolutePath() {
		return localAbsolutePath;
	}

	public void setLocalAbsolutePath(final String localAbsolutePath) {
		this.localAbsolutePath = localAbsolutePath;
	}

	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	public void setIrodsAbsolutePath(final String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getTransferErrorStatus() {
		return transferErrorStatus;
	}

	public void setTransferErrorStatus(final String transferErrorStatus) {
		this.transferErrorStatus = transferErrorStatus;
	}

	public Set<LocalIRODSTransferItem> getLocalIRODSTransferItems() {
		return localIRODSTransferItems;
	}

	public void setLocalIRODSTransferItems(
			final Set<LocalIRODSTransferItem> localIRODSTransferItems) {
		this.localIRODSTransferItems = localIRODSTransferItems;
	}

	public String getTransferPassword() {
		return transferPassword;
	}

	public void setTransferPassword(final String transferPassword) {
		this.transferPassword = transferPassword;
	}

	public String getGlobalException() {
		return globalException;
	}

	public void setGlobalException(final String globalException) {
		this.globalException = globalException;
	}

	public String getLastSuccessfulPath() {
		return lastSuccessfulPath;
	}

	public void setLastSuccessfulPath(final String lastSuccessfulPath) {
		this.lastSuccessfulPath = lastSuccessfulPath;
	}

	public int getTotalFilesCount() {
		return totalFilesCount;
	}

	public void setTotalFilesCount(final int totalFilesCount) {
		this.totalFilesCount = totalFilesCount;
	}

	public int getTotalFilesTransferredSoFar() {
		return totalFilesTransferredSoFar;
	}

	public void setTotalFilesTransferredSoFar(
			final int totalFilesTransferredSoFar) {
		this.totalFilesTransferredSoFar = totalFilesTransferredSoFar;
	}

	public String getGlobalExceptionStackTrace() {
		return globalExceptionStackTrace;
	}

	public void setGlobalExceptionStackTrace(
			final String globalExceptionStackTrace) {
		this.globalExceptionStackTrace = globalExceptionStackTrace;
	}

}
