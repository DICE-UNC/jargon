/**
 * 
 */
package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Represents an individual synchronization operation.
 * 
 * @author mikeconway
 * 
 */
@Entity
@Table(name = "synch_process")
public class SynchProcess implements Serializable {

	private static final long serialVersionUID = -5632425983548779867L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	/**
	 * Component transfer for this synchronization. in transfers, a diff is
	 * created, and appropriate transfers are scheduled to resolve differences.
	 * This links these individual transfers (which are puts or gets) to the
	 * synchronization activity that initiated them.
	 */

	@OneToMany(mappedBy = "synchProcess", targetEntity = LocalIRODSTransfer.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@OrderBy("createdAt")
	private Set<LocalIRODSTransfer> localIRODSTransfers = new HashSet<LocalIRODSTransfer>();

	/**
	 * Describes the current known state of the transfer, based on callbacks as
	 * various transfers that make up the synchronization complete
	 */
	@Column(name = "transfer_state")
	@Enumerated(EnumType.STRING)
	private TransferState transferState;

	/**
	 * Describes the current known state of the transfer (whether there are
	 * errors or warnings), based on callbacks as various transfers that make up
	 * the synchronization complete.
	 */
	@Column(name = "transfer_status")
	@Enumerated(EnumType.STRING)
	private TransferStatus transferStatus;

	/**
	 * Indicates the start of the synchronization process, this is the time that
	 * the process of starting a diff is begun.
	 */
	@Column(name = "synch_start")
	private Date synchStart;

	/**
	 * Indicates the end of a synchronization process, after the last transfer
	 * and possible indexing of the local file system occurs
	 */
	@Column(name = "synch_end")
	private Date synchEnd;

	@Column(name = "global_exception", length = 32672)
	private String globalException = "";

	@Column(name = "global_exception_stack_trace", length = 32672)
	private String globalExceptionStackTrace = "";

	@Column(name = "last_successful_path", length = 32672)
	private String lastSuccessfulPath = "";

	/**
	 * Creation time
	 */
	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	/**
	 * Last updated time
	 */
	@Column(name = "updated_at")
	private Date updatedAt;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("synchProcess:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   transferState:");
		sb.append(transferState);
		sb.append("\n   transferStatus:");
		sb.append(transferStatus);
		sb.append("\n   synchStart:");
		sb.append(synchStart);
		sb.append("\n   synchEnd:");
		sb.append(synchEnd);
		sb.append("\n   globalException:");
		sb.append(globalException);
		sb.append("\n   lastSuccessfulPath:");
		sb.append(lastSuccessfulPath);
		sb.append("\n   createdAt:");
		sb.append(createdAt);
		sb.append("\n   updatedAt:");
		sb.append(updatedAt);
		return sb.toString();

	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
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
	public void setCreatedAt(final Date createdAt) {
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
	public void setUpdatedAt(final Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * @param localIRODSTransfers
	 *            the localIRODSTransfers to set
	 */
	/*
	 * public void setLocalIRODSTransfers(Set<LocalIRODSTransfer>
	 * localIRODSTransfers) { this.localIRODSTransfers = localIRODSTransfers; }
	 */

	/**
	 * @return the localIRODSTransfers
	 */
	/*
	 * public Set<LocalIRODSTransfer> getLocalIRODSTransfers() { return
	 * localIRODSTransfers; }
	 */

	/**
	 * @param transferState
	 *            the transferState to set
	 */

	public void setTransferState(final TransferState transferState) {
		this.transferState = transferState;
	}

	/**
	 * @return the transferState
	 */
	public TransferState getTransferState() {
		return transferState;
	}

	/**
	 * @param transferStatus
	 *            the transferStatus to set
	 */
	public void setTransferStatus(final TransferStatus transferStatus) {
		this.transferStatus = transferStatus;
	}

	/**
	 * @return the transferStatus
	 */
	public TransferStatus getTransferStatus() {
		return transferStatus;
	}

	/**
	 * @param synchStart
	 *            the synchStart to set
	 */
	public void setSynchStart(final Date synchStart) {
		this.synchStart = synchStart;
	}

	/**
	 * @return the synchStart
	 */
	public Date getSynchStart() {
		return synchStart;
	}

	/**
	 * @param synchEnd
	 *            the synchEnd to set
	 */
	public void setSynchEnd(final Date synchEnd) {
		this.synchEnd = synchEnd;
	}

	/**
	 * @return the synchEnd
	 */
	public Date getSynchEnd() {
		return synchEnd;
	}

	/**
	 * @param globalException
	 *            the globalException to set
	 */
	public void setGlobalException(final String globalException) {
		this.globalException = globalException;
	}

	/**
	 * @return the globalException
	 */
	public String getGlobalException() {
		return globalException;
	}

	/**
	 * @param globalExceptionStackTrace
	 *            the globalExceptionStackTrace to set
	 */
	public void setGlobalExceptionStackTrace(
			final String globalExceptionStackTrace) {
		this.globalExceptionStackTrace = globalExceptionStackTrace;
	}

	/**
	 * @return the globalExceptionStackTrace
	 */
	public String getGlobalExceptionStackTrace() {
		return globalExceptionStackTrace;
	}

	/**
	 * @param lastSuccessfulPath
	 *            the lastSuccessfulPath to set
	 */
	public void setLastSuccessfulPath(final String lastSuccessfulPath) {
		this.lastSuccessfulPath = lastSuccessfulPath;
	}

	/**
	 * @return the lastSuccessfulPath
	 */
	public String getLastSuccessfulPath() {
		return lastSuccessfulPath;
	}

	/**
	 * @param localIRODSTransfers the localIRODSTransfers to set
	 */
	public void setLocalIRODSTransfers(Set<LocalIRODSTransfer> localIRODSTransfers) {
		this.localIRODSTransfers = localIRODSTransfers;
	}

	/**
	 * @return the localIRODSTransfers
	 */
	public Set<LocalIRODSTransfer> getLocalIRODSTransfers() {
		return localIRODSTransfers;
	}

}
