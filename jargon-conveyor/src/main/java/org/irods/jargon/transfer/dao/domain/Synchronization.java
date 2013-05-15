package org.irods.jargon.transfer.dao.domain;

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

/**
 * Represents the specification of a synchronization relationship between a
 * local file system and an iRODS file system
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Entity
@Table(name = "synchronization")
public class Synchronization {

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "frequency_type")
	@Enumerated(EnumType.STRING)
	private FrequencyType frequencyType;

	/**
	 * Directory on local file system where synchronization will take place
	 */
	@Column(name = "local_synch_directory", length = 32672, nullable = false)
	private String localSynchDirectory;

	/**
	 * Directory in iRODS where synchronization will take place
	 */
	@Column(name = "irods_synch_directory", length = 32672, nullable = false)
	private String irodsSynchDirectory;

	/**
	 * Join to table that contain the grid login information
	 */
	@ManyToOne(targetEntity = GridAccount.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "grid_account_id", nullable = false)
	private GridAccount gridAccount;

	@OneToMany(mappedBy = "synchronization", targetEntity = Transfer.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@org.hibernate.annotations.Cascade({
			org.hibernate.annotations.CascadeType.SAVE_UPDATE,
			org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
	@OrderBy("createdAt")
	private Set<Transfer> transfers = new HashSet<Transfer>();

	/**
	 * Time stamp of the last synchronization attempt
	 */
	@Column(name = "last_synchronized")
	private Date lastSynchronized;

	/**
	 * Enumerated status of the last synchronization attempt
	 */
	@Column(name = "last_synchronization_status")
	@Enumerated(EnumType.STRING)
	private TransferStatusEnum lastSynchronizationStatus;

	/**
	 * Message associated with the last synchronization attempt
	 */
	@Column(name = "last_synchronization_message", length = 32672)
	private String lastSynchronizationMessage;

	/**
	 * Enumerated mode of the synchronization (direction of synch)
	 */
	@Column(name = "synchronization_mode", nullable = false)
	@Enumerated(EnumType.STRING)
	private SynchronizationType synchronizationMode;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the localSynchDirectory
	 */
	public String getLocalSynchDirectory() {
		return localSynchDirectory;
	}

	/**
	 * @param localSynchDirectory
	 *            the localSynchDirectory to set
	 */
	public void setLocalSynchDirectory(final String localSynchDirectory) {
		this.localSynchDirectory = localSynchDirectory;
	}

	/**
	 * @return the irodsSynchDirectory
	 */
	public String getIrodsSynchDirectory() {
		return irodsSynchDirectory;
	}

	/**
	 * @param irodsSynchDirectory
	 *            the irodsSynchDirectory to set
	 */
	public void setIrodsSynchDirectory(final String irodsSynchDirectory) {
		this.irodsSynchDirectory = irodsSynchDirectory;
	}

	/**
	 * @return the lastSynchronized
	 */
	public Date getLastSynchronized() {
		return lastSynchronized;
	}

	/**
	 * @param lastSynchronized
	 *            the lastSynchronized to set
	 */
	public void setLastSynchronized(final Date lastSynchronized) {
		this.lastSynchronized = lastSynchronized;
	}

	/**
	 * @return the lastSynchronizationStatus
	 */
	public TransferStatusEnum getLastSynchronizationStatus() {
		return lastSynchronizationStatus;
	}

	/**
	 * @param lastSynchronizationStatus
	 *            the lastSynchronizationStatus to set
	 */
	public void setLastSynchronizationStatus(
			final TransferStatusEnum lastSynchronizationStatus) {
		this.lastSynchronizationStatus = lastSynchronizationStatus;
	}

	/**
	 * @return the lastSynchronizationMessage
	 */
	public String getLastSynchronizationMessage() {
		return lastSynchronizationMessage;
	}

	/**
	 * @param lastSynchronizationMessage
	 *            the lastSynchronizationMessage to set
	 */
	public void setLastSynchronizationMessage(
			final String lastSynchronizationMessage) {
		this.lastSynchronizationMessage = lastSynchronizationMessage;
	}

	/**
	 * @return the synchronizationMode
	 */
	public SynchronizationType getSynchronizationMode() {
		return synchronizationMode;
	}

	/**
	 * @param synchronizationMode
	 *            the synchronizationMode to set
	 */
	public void setSynchronizationMode(
			final SynchronizationType synchronizationMode) {
		this.synchronizationMode = synchronizationMode;
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
	 * @return the frequencyType
	 */
	public FrequencyType getFrequencyType() {
		return frequencyType;
	}

	/**
	 * @param frequencyType
	 *            the frequencyType to set
	 */
	public void setFrequencyType(final FrequencyType frequencyType) {
		this.frequencyType = frequencyType;
	}

	/**
	 * @param transfers
	 *            the transfers to set
	 */
	public void setTransfers(
			final Set<Transfer> transfers) {
		this.transfers = transfers;
	}

	/**
	 * @return the transfers
	 */
	public Set<Transfer> getTransfers() {
		return transfers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("synchronization");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   name:");
		sb.append(name);
		sb.append("\n   localSynchDirectory:");
		sb.append(localSynchDirectory);
		sb.append("\n   irodsSynchDirectory:");
		sb.append(irodsSynchDirectory);
		sb.append("\n   frequencyType:");
		sb.append(frequencyType);
		sb.append("\n   synchronizationMode:");
		sb.append(synchronizationMode);
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
