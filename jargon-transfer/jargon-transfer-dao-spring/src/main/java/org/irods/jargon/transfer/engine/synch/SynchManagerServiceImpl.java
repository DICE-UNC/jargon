package org.irods.jargon.transfer.engine.synch;

import java.util.List;

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
    
    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#listAllSynchronizations()
     */
    @Override
    @Transactional
    public List<Synchronization> listAllSynchronizations() throws SynchException {
    	try {
			return synchronizationDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("dao exception listing synchs", e);
			throw new SynchException("exception listing all synchronizations", e);
		}
    }
    
    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#findById(java.lang.Long)
     */
    @Override
    @Transactional
    public Synchronization findById(Long id) throws SynchException {
    	log.info("findById()");
    	try {
			return synchronizationDAO.findById(id);
		} catch (TransferDAOException e) {
			log.error("dao exception finding synch", e);
			throw new SynchException("exception finding synchronizations", e);
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.engine.synch.SynchManagerService# createNewSynchConfiguration
     * (org.irods.jargon.transfer.dao.domain.SynchConfiguration)
     */
    @Override
    @Transactional
    public void createNewSynchConfiguration(final Synchronization synchConfiguration) throws ConflictingSynchException, SynchException {
        if (synchConfiguration == null) {
            throw new IllegalArgumentException("null synchConfiguration");
        }
        log.info("createNewSynchConfiguration with config: {}", synchConfiguration);
        
       /*
        * review this synch configuration against existing ones, there should not be a lot, so just list them and analyze
        */
        
        evaluateSynchAgainstExistingForConflicts(synchConfiguration);
        
        // review pases, go ahead and add
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

	/**
	 * @param synchConfiguration
	 * @throws SynchException
	 */
	private void evaluateSynchAgainstExistingForConflicts(
			final Synchronization synchConfiguration) throws ConflictingSynchException, SynchException {
		List<Synchronization> synchronizations;
        try {
			synchronizations = synchronizationDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("error listing existing synchronizations", e);
			throw new SynchException("unable to find existing synchronizations", e);
		}
		
		log.debug("existing synchs:{}", synchronizations);
		for (Synchronization existingSynchronization : synchronizations) {
			log.info("analyizing existing synchronization:{}", existingSynchronization);
			if (synchConfiguration.getName().equalsIgnoreCase(existingSynchronization.getName())) {
				log.error("a synch already exists with the name:{}", synchConfiguration.getName());
				throw new ConflictingSynchException("a synchronization already exists with the same name");
			}
			
			if (synchConfiguration.getIrodsHostName().equals(existingSynchronization.getIrodsHostName()) && 
					synchConfiguration.getIrodsZone().equals(existingSynchronization.getIrodsZone())) {
				log.debug("host/zone match");
			} else {
				log.debug("this config is not for the same host/zone, so through evaluating");
				continue;
			}
			
			log.debug("have an existing config for same host/zone, evaluate for overlap/conflict");
			if (synchConfiguration.getIrodsSynchDirectory().equals(existingSynchronization.getIrodsSynchDirectory())) {
				log.error("an existing synchronization is already using the desired iRODS target collection:{}:", synchConfiguration);
				throw new ConflictingSynchException("syncronization has duplicate iRODS target collection");
			}
			
			if (synchConfiguration.getLocalSynchDirectory().equals(existingSynchronization.getLocalSynchDirectory())) {
				log.error("an existing synchronization is already using the desired local collection:{}:", synchConfiguration);
				throw new ConflictingSynchException("syncronization has duplicate local collection");
			}
			
			
		}
	}

}
