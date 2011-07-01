package org.irods.jargon.datautils.synchproperties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

public interface SynchPropertiesService {

    public static final String USER_SYNCH_DIR_TAG = "iRODSSynch:userSynchDir";

    public static final String SEPARATOR = "~";

    /**
     * Get the information regarding the state of synchronization between iRODS and a local file system for a given user
     * and device.
     * 
     * @param userName
     *            <code>String</code> with the iRODS user name for the synch
     * @param deviceName
     *            <code>String</code> with the given name for the device (laptop, desktop, etc)
     * @param irodsAbsolutePath
     *            <code>String</code> with the absolute path to the iRODS collection that is the root of the
     *            synchronization
     * @return {@link UserSynchTarget} describing the synchronization status for the user/target name/iRODS absolute
     *         path
     * @throws DataNotFoundException
     * @throws JargonException
     */
    UserSynchTarget getUserSynchTargetForUserAndAbsolutePath(final String userName, final String deviceName,
            final String irodsAbsolutePath) throws DataNotFoundException, JargonException;

    /**
     * Given the device name and other information, set up a synchronizing device on iRODS.
     * 
     * @param userName
     *            <code>String</code> with the user name that will synch for the given device
     * @param deviceName
     *            <code>String</code> with a device name that represents the local file system that will synchronize to
     *            iRODS
     * @param irodsAbsolutePath
     *            <code>String</code> with the absolute path to an iRODS collection that will be synchronized with the
     *            given local collection
     * @param localAbsolutePath
     *            <code>String</code> with the absolute path to the local file system directory that will be synched to
     *            iRODS
     * @throws DuplicateDataException
     *             <code>String</code> that indicates that this synch has already been configured
     * @throws JargonException
     */
    void addSynchDeviceForUserAndIrodsAbsolutePath(final String userName, final String deviceName,
            final String irodsAbsolutePath, final String localAbsolutePath) throws DuplicateDataException,
            JargonException;

    /**
     * 
     * @param userName
     * @param deviceName
     * @param irodsAbsolutePath
     * @throws JargonException
     */
    void removeSynchDevice(final String userName, final String deviceName, final String irodsAbsolutePath)
            throws JargonException;

    /**
     * 
     * @param userName
     * @param deviceName
     * @param irodsAbsolutePath
     * @throws JargonException
     */
    boolean synchDeviceExists(final String userName, final String deviceName, final String irodsAbsolutePath)
            throws JargonException;

    /**
     * 
     * @param irodsAccount
     */
    void setIrodsAccount(final IRODSAccount irodsAccount);

    IRODSAccount getIrodsAccount();

    void setIrodsAccessObjectFactory(final IRODSAccessObjectFactory irodsAccessObjectFactory);

    IRODSAccessObjectFactory getIrodsAccessObjectFactory();

    /**
     * Get the local and iRODS timetamps that reflect the current system time on each
     * 
     * @return {@link SynchTimestamps} value with the local and iRODS current time since epoch
     * @throws JargonException
     */
    SynchTimestamps getSynchTimestamps() throws JargonException;

    /**
     * Update the iRODS metadata about this synch to record the now time locally and on iRODS as the last synch time
     * 
     * @param userName
     *            <code>String</code> with the user name that will synch for the given device
     * @param deviceName
     *            <code>String</code> with a device name that represents the local file system that will synchronize to
     *            iRODS
     * @param irodsAbsolutePath
     *            <code>String</code> with the absolute path to an iRODS collection that will be synchronized with the
     *            given local collection
     * @throws JargonException
     */
    void updateTimestampsToCurrent(String userName, String deviceName, String irodsAbsolutePath) throws JargonException;

}