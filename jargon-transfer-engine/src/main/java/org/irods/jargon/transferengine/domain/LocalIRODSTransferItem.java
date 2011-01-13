/**
 * 
 */
package org.irods.jargon.transferengine.domain;

import java.util.Date;

/**
 * For a <code>LocalIRODSTransfer</code>, this is an individual operation within
 * the transfer. This item would be a directory or file that was moved during
 * the transfer.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalIRODSTransferItem {

	private Long id;
	private LocalIRODSTransfer localIRODSTransfer;
	private String sourceFileAbsolutePath;
	private String targetFileAbsolutePath;
	private boolean file;
	private boolean error;
	private long lengthInBytes = 0L;
	private String errorMessage;
	private String errorStackTrace;
	private Date transferredAt;

	public LocalIRODSTransferItem() {

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LocalIRODSTransferItem:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   sourceFileAbsolutePath:");
		sb.append(sourceFileAbsolutePath);
		sb.append("\n   targetFileAbsolutePath:");
		sb.append(targetFileAbsolutePath);
		sb.append("\n   isFile:");
		sb.append(file);
		sb.append("\n   lengthInBytes:");
		sb.append(lengthInBytes);
		sb.append("\n   isError:");
		sb.append(error);
		sb.append("\n   errorMessage:");
		sb.append(errorMessage);
		sb.append("\n   transferredAt:");
		sb.append(transferredAt);
		return sb.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getSourceFileAbsolutePath() {
		return sourceFileAbsolutePath;
	}

	public void setSourceFileAbsolutePath(final String sourceFileAbsolutePath) {
		this.sourceFileAbsolutePath = sourceFileAbsolutePath;
	}

	public String getTargetFileAbsolutePath() {
		return targetFileAbsolutePath;
	}

	public void setTargetFileAbsolutePath(final String targetFileAbsolutePath) {
		this.targetFileAbsolutePath = targetFileAbsolutePath;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(final boolean file) {
		this.file = file;
	}

	public boolean isError() {
		return error;
	}

	public void setError(final boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Date getTransferredAt() {
		return transferredAt;
	}

	public void setTransferredAt(final Date transferredAt) {
		this.transferredAt = transferredAt;
	}

	public LocalIRODSTransfer getLocalIRODSTransfer() {
		return localIRODSTransfer;
	}

	public void setLocalIRODSTransfer(
			final LocalIRODSTransfer localIRODSTransfer) {
		this.localIRODSTransfer = localIRODSTransfer;
	}

	public long getLengthInBytes() {
		return lengthInBytes;
	}

	public void setLengthInBytes(final long lengthInBytes) {
		this.lengthInBytes = lengthInBytes;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public void setErrorStackTrace(final String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}
}
