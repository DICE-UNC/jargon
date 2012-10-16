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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Domain object that represents a transfer activity between the local host and
 * an iRODS server.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Entity
@Table(name = "local_irods_transfer")
public class LocalIRODSTransfer implements Serializable {

	private static final long serialVersionUID = -6714116121965036534L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "transfer_state")
	@Enumerated(EnumType.STRING)
	private TransferState transferState;

	@Column(name = "transfer_status")
	@Enumerated(EnumType.STRING)
	private TransferStatus transferStatus;

	@Column(name = "transfer_type")
	@Enumerated(EnumType.STRING)
	private TransferType transferType;

	/**
	 * Overall synchronization configuration that is being processed by this
	 * transfer
	 */
	@ManyToOne(targetEntity = Synchronization.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "synchronization_id", nullable = true)
	private Synchronization synchronization;

	@Column(name = "tranfer_start")
	private Date transferStart;

	@Column(name = "tranfer_end")
	private Date transferEnd;

	@Column(name = "local_absolute_path", length = 32672)
	private String localAbsolutePath = "";

	@Column(name = "irods_absolute_path", length = 32672)
	private String irodsAbsolutePath = "";

	@OneToMany(mappedBy = "localIRODSTransfer", targetEntity = LocalIRODSTransferItem.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@OrderBy("transferredAt")
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<LocalIRODSTransferItem> localIRODSTransferItems = new HashSet<LocalIRODSTransferItem>();

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

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

	/**
	 * Join to table that contain the grid login information
	 */
	@ManyToOne(targetEntity = GridAccount.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "grid_account_id", nullable = false)
	private GridAccount gridAccount;

	public LocalIRODSTransfer() {
		super();
	}

	public TransferState getTransferState() {
		return transferState;
	}

	public void setTransferState(final TransferState transferState) {
		this.transferState = transferState;
	}

	public TransferType getTransferType() {
		return transferType;
	}

	public void setTransferType(final TransferType transferType) {
		this.transferType = transferType;
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

	public TransferStatus getTransferStatus() {
		return transferStatus;
	}

	public void setTransferStatus(final TransferStatus transferStatus) {
		this.transferStatus = transferStatus;
	}

	public Set<LocalIRODSTransferItem> getLocalIRODSTransferItems() {
		return localIRODSTransferItems;
	}

	public void setLocalIRODSTransferItems(
			final Set<LocalIRODSTransferItem> localIRODSTransferItems) {
		this.localIRODSTransferItems = localIRODSTransferItems;
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

	public Synchronization getSynchronization() {
		return synchronization;
	}

	public void setSynchronization(final Synchronization synchronization) {
		this.synchronization = synchronization;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LocalIRODSTransfer");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   transferState:");
		sb.append(transferState);
		sb.append("\n   transferStatus:");
		sb.append(transferStatus);
		sb.append("\n   transferType:");
		sb.append(transferType);
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

	/**
	 * @return the gridAccount
	 */
	public GridAccount getGridAccount() {
		return gridAccount;
	}

	/**
	 * @param gridAccount
	 *            the gridAccount to set
	 */
	public void setGridAccount(GridAccount gridAccount) {
		this.gridAccount = gridAccount;
	}

}
