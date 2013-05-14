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
import javax.persistence.Temporal;

/**
 * Domain object that represents a transfer activity between the local host and
 * an iRODS server.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Entity
@Table(name = "transfer")
public class Transfer implements Serializable {

	private static final long serialVersionUID = -6714116121965036534L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "transfer_state")
	@Enumerated(EnumType.STRING)
	private TransferState transferState;

	@Column(name = "last_transfer_status")
	@Enumerated(EnumType.STRING)
	private TransferStatus lastTransferStatus;

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

	@Column(name = "local_absolute_path", length = 32672)
	private String localAbsolutePath = "";

	@Column(name = "irods_absolute_path", length = 32672)
	private String irodsAbsolutePath = "";

	@OneToMany(mappedBy = "transfer", targetEntity = TransferAttempt.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@OrderBy("attemptStart")
	private Set<TransferAttempt> transferAttempts = new HashSet<TransferAttempt>();

	@Column(name = "created_at")
	@Temporal(javax.persistence.TemporalType.DATE)
	private Date createdAt;

	@Column(name = "updated_at")
	@Temporal(javax.persistence.TemporalType.DATE)
	private Date updatedAt;

	/**
	 * Join to table that contain the grid login information
	 */
	@ManyToOne(targetEntity = GridAccount.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "grid_account_id", nullable = false)
	private GridAccount gridAccount;

	public Transfer() {
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

	public TransferStatus getLastTransferStatus() {
		return lastTransferStatus;
	}

	public void setLastTransferStatus(final TransferStatus lastTransferStatus) {
		this.lastTransferStatus = lastTransferStatus;
	}

	public Set<TransferAttempt> getTransferAttempts() {
		return transferAttempts;
	}

	public void setTransferAttempts(final Set<TransferAttempt> transferAttempts) {
		this.transferAttempts = transferAttempts;
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
		sb.append("Transfer");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   transferState:");
		sb.append(transferState);
		sb.append("\n   transferStatus:");
		sb.append(lastTransferStatus);
		sb.append("\n   transferType:");
		sb.append(transferType);
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
