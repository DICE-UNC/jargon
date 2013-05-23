package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * For a <code>TransferAttempt</code>, this is an individual transfer attempt
 * within the transfer
 * 
 * @author lisa
 */
@Entity
@Table(name = "transfer_attempt")
public class TransferAttempt implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "sequence_number", nullable = false)
	private long sequenceNumber;

	@ManyToOne(targetEntity = Transfer.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "transfer_id", nullable = false)
	private Transfer transfer;

	@Column(name = "transfer_attempt_start")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date attemptStart;

	@Column(name = "transfer_attempt_end")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date attemptEnd;

	@Column(name = "transfer_attempt_status")
	@Enumerated(EnumType.STRING)
	private TransferStatusEnum attemptStatus;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "global_exception", length = 32672)
	private String globalException = "";

	@Column(name = "global_exception_stack_trace", length = 32672)
	private String globalExceptionStackTrace = "";

	@Column(name = "last_successful_path", length = 32672)
	private String lastSuccessfulPath = "";

	@Column(name = "total_files_count")
	private int totalFilesCount = 0;

	@Column(name = "total_files_transferred_so_far")
	private int totalFilesTransferredSoFar = 0;

	@OneToMany(mappedBy = "transferAttempt", targetEntity = TransferItem.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@OrderBy("sequenceNumber")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private List<TransferItem> transferItems = new ArrayList<TransferItem>();

	@Column(name = "created_at")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date updatedAt;

	public TransferAttempt() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public Transfer getTransfer() {
		return transfer;
	}

	public void setTransfer(final Transfer transfer) {
		this.transfer = transfer;
	}

	public Date getAttemptStart() {
		return attemptStart;
	}

	public void setAttemptStart(final Date attemptStart) {
		this.attemptStart = attemptStart;
	}

	public Date getAttemptEnd() {
		return attemptEnd;
	}

	public void setAttemptEnd(final Date attemptEnd) {
		this.attemptEnd = attemptEnd;
	}

	public TransferStatusEnum getAttemptStatus() {
		return attemptStatus;
	}

	public void setAttemptStatus(final TransferStatusEnum attemptStatus) {
		this.attemptStatus = attemptStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public List<TransferItem> getTransferItems() {
		return transferItems;
	}

	public void setTransferItems(final List<TransferItem> transferItems) {
		this.transferItems = transferItems;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TransferAttempt:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   seq:");
		sb.append(this.sequenceNumber);
		sb.append("\n   attemptStart:");
		sb.append(attemptStart);
		sb.append("\n   attemptEnd:");
		sb.append(attemptEnd);
		sb.append("\n   attemptStatus:");
		sb.append(attemptStatus);
		sb.append("\n   errorMessage:");
		sb.append(errorMessage);
		sb.append("\n   globalException:");
		sb.append(globalException);
		sb.append("\n   lastSuccessfulPath:");
		sb.append(lastSuccessfulPath);
		sb.append("\n   totalFilesCount:");
		sb.append(totalFilesCount);
		sb.append("\n   totalFilesTransferredSoFar:");
		sb.append(totalFilesTransferredSoFar);

		return sb.toString();
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @param updatedAt
	 *            the updatedAt to set
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * @return the sequenceNumber
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(long attemptSequenceNumber) {
		this.sequenceNumber = attemptSequenceNumber;
	}

}
