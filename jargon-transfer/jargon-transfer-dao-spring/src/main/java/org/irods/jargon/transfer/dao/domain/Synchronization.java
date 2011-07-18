package org.irods.jargon.transfer.dao.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents the specification of a synchronization relationship between a local file system and an iRODS file system
 * 
 * @author Mike Con way - DICE (www.irods.org)
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
     * Host name of the iRODS server that synchronizes with this directory
     */
    @Column(name = "irods_host_name", nullable = false)
    private String irodsHostName;

    /**
     * Port for the iRODS server that synchronizes with this directory
     */
    @Column(name = "irods_port", nullable = false)
    private int irodsPort;

    /**
     * User name that will be used for this synchronization
     */
    @Column(name = "irods_user_name", nullable = false)
    private String irodsUserName;

    /**
     * Password that will be used for this synchronization (this is encrypted when stored)
     */
    @Column(name = "irods_password", nullable = false)
    private String irodsPassword;

    /**
     * iRODS zone name for synchronization
     */
    @Column(name = "irods_zone", nullable = false)
    private String irodsZone;

    /**
     * Default resource name for synchronization (can be left blank if default resources are selected via a policy
     */
    @Column(name = "default_resource_name")
    private String defaultResourceName;

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
    private TransferStatus lastSynchronizationStatus;

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
    public void setName(String name) {
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
     * @return the irodsHostName
     */
    public String getIrodsHostName() {
        return irodsHostName;
    }

    /**
     * @param irodsHostName
     *            the irodsHostName to set
     */
    public void setIrodsHostName(final String irodsHostName) {
        this.irodsHostName = irodsHostName;
    }

    /**
     * @return the irodsPort
     */
    public int getIrodsPort() {
        return irodsPort;
    }

    /**
     * @param irodsPort
     *            the irodsPort to set
     */
    public void setIrodsPort(final int irodsPort) {
        this.irodsPort = irodsPort;
    }

    /**
     * @return the irodsUserName
     */
    public String getIrodsUserName() {
        return irodsUserName;
    }

    /**
     * @param irodsUserName
     *            the irodsUserName to set
     */
    public void setIrodsUserName(final String irodsUserName) {
        this.irodsUserName = irodsUserName;
    }

    /**
     * @return the irodsPassword
     */
    public String getIrodsPassword() {
        return irodsPassword;
    }

    /**
     * @param irodsPassword
     *            the irodsPassword to set
     */
    public void setIrodsPassword(final String irodsPassword) {
        this.irodsPassword = irodsPassword;
    }

    /**
     * @return the irodsZone
     */
    public String getIrodsZone() {
        return irodsZone;
    }

    /**
     * @param irodsZone
     *            the irodsZone to set
     */
    public void setIrodsZone(final String irodsZone) {
        this.irodsZone = irodsZone;
    }

    /**
     * @return the defaultResourceName
     */
    public String getDefaultResourceName() {
        return defaultResourceName;
    }

    /**
     * @param defaultResourceName
     *            the defaultResourceName to set
     */
    public void setDefaultResourceName(final String defaultResourceName) {
        this.defaultResourceName = defaultResourceName;
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
    public TransferStatus getLastSynchronizationStatus() {
        return lastSynchronizationStatus;
    }

    /**
     * @param lastSynchronizationStatus
     *            the lastSynchronizationStatus to set
     */
    public void setLastSynchronizationStatus(final TransferStatus lastSynchronizationStatus) {
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
    public void setLastSynchronizationMessage(final String lastSynchronizationMessage) {
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
    public void setSynchronizationMode(final SynchronizationType synchronizationMode) {
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
    public void setFrequencyType(FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

}
