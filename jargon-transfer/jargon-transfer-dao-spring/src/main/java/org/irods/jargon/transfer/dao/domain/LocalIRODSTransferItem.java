package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * For a <code>LocalIRODSTransfer</code>, this is an individual operation within
 * the transfer. This item would be a directory or file that was moved during
 * the transfer.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Entity
@Table(name = "local_irods_transfer_item")
public class LocalIRODSTransferItem implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@ManyToOne(targetEntity = LocalIRODSTransfer.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "local_irods_transfer_id", nullable = false)
	private LocalIRODSTransfer localIRODSTransfer;

	@Column(name = "source_file_absolute_path", length = 32672)
	private String sourceFileAbsolutePath;

	@Column(name = "target_file_absolute_path", length = 32672)
	private String targetFileAbsolutePath;

	@Column(name = "transfer_type")
	@Enumerated(EnumType.STRING)
	private TransferType transferType;

	@Column(name = "is_file")
	private boolean file;

	@Column(name = "is_error")
	private boolean error;

	@Column(name = "length_in_bytes")
	private long lengthInBytes = 0L;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "error_stack_trace", length = 32672)
	private String errorStackTrace;

	@Column(name = "transferred_at")
	private Date transferredAt;

	public LocalIRODSTransferItem() {
		super();
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

	/**
	 * @param transferType
	 *            the transferType to set
	 */
	public void setTransferType(final TransferType transferType) {
		this.transferType = transferType;
	}

	/**
	 * @return the transferType
	 */
	public TransferType getTransferType() {
		return transferType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LocalIRODSTransferItem:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   transferType:");
		sb.append(transferType);
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

}
