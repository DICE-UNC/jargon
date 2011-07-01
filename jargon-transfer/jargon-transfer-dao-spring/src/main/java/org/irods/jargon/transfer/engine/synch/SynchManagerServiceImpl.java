package org.irods.jargon.transfer.engine.synch;

import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage storage and processing of synch information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchManagerServiceImpl implements SynchManagerService {

    private final Logger log = LoggerFactory.getLogger(SynchManagerServiceImpl.class);

    private SynchronizationDAO synchronizationDAO;

    public void setSynchronizationDAO(final SynchronizationDAO synchConfigurationDAO) {
        this.synchronizationDAO = synchConfigurationDAO;
    }

    public SynchronizationDAO getSynchronizationDAO() {
        return synchronizationDAO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.engine.synch.SynchManagerService# createNewSynchConfiguration
     * (org.irods.jargon.transfer.dao.domain.SynchConfiguration)
     */
    @Override
    @Transactional
    public void createNewSynchConfiguration(final Synchronization synchConfiguration) throws SynchException {
        if (synchConfiguration == null) {
            throw new IllegalArgumentException("null synchConfiguration");
        }
        log.info("createNewSynchConfiguration with config: {}", synchConfiguration);
        try {
            synchronizationDAO.save(synchConfiguration);
        } catch (TransferDAOException e) {
            log.error("synchConfiguration create failed with exception", e);
            throw new SynchException(e);
        }

        // TODO: per # 227, update irods synch folder
        /*
         * txfr engine has the synch device database, AVU data is also kept in iRODS that has similar information. We'll
         * need to keep these updated. See data-utils in jargon, SynchPropertiesService. The basic are already there, we
         * might need a few extra methods. The iRODS side is AVU metadata.
         * 
         * check for already in use (is device name already used? Is this a re-configuration of the same device? (allow)
         * etc There are methods in data-utils SynchConfigurationService. We might need to add a 'getAllDevicesForUser',
         * etc.
         * 
         * mkirs if necessary
         * 
         * update dbase
         * 
         * add synch config entry as avu via SynchPropertiesService
         */

        log.info("synch created");
    }

}
