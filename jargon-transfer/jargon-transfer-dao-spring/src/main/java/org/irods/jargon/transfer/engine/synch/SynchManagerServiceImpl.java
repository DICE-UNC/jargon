package org.irods.jargon.transfer.engine.synch;

import org.irods.jargon.transfer.dao.SynchConfigurationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.SynchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage storage and processing of synch information
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchManagerServiceImpl implements SynchManagerService {
	
	private final Logger log = LoggerFactory
	.getLogger(SynchManagerServiceImpl.class);

	
	private SynchConfigurationDAO synchConfigurationDAO;

	public void setSynchConfigurationDAO(SynchConfigurationDAO synchConfigurationDAO) {
		this.synchConfigurationDAO = synchConfigurationDAO;
	}

	public SynchConfigurationDAO getSynchConfigurationDAO() {
		return synchConfigurationDAO;
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#createNewSynchConfiguration(org.irods.jargon.transfer.dao.domain.SynchConfiguration)
	 */
	@Override
	@Transactional
	public void createNewSynchConfiguration(final SynchConfiguration synchConfiguration) throws SynchException {
		if (synchConfiguration == null) {
			throw new IllegalArgumentException("null synchConfiguration");
		}
		log.info("createNewSynchConfiguration with config: {}", synchConfiguration);
		try {
			synchConfigurationDAO.save(synchConfiguration);
		} catch (TransferDAOException e) {
			log.error("synchConfiguration create failed with exception", e);
			throw new SynchException(e);
		}
		
		log.info("synch created");
	}

}
