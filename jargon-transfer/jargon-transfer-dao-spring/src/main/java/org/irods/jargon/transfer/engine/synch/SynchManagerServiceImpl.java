package org.irods.jargon.transfer.engine.synch;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.dao.domain.TransferState;
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

	private final Logger log = LoggerFactory
			.getLogger(SynchManagerServiceImpl.class);

	private SynchronizationDAO synchronizationDAO;

	public void setSynchronizationDAO(
			final SynchronizationDAO synchConfigurationDAO) {
		this.synchronizationDAO = synchConfigurationDAO;
	}

	public SynchronizationDAO getSynchronizationDAO() {
		return synchronizationDAO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#
	 * listAllSynchronizations()
	 */
	@Override
	@Transactional
	public List<Synchronization> listAllSynchronizations()
			throws SynchException {
		try {
			return synchronizationDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("dao exception finding synch", e);
			throw new SynchException("exception finding synchronizations", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.synch.SynchManagerService#isSynchRunning
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	@Transactional
	public boolean isSynchRunning(final Synchronization synchronization)
			throws SynchException {

		log.info("is SynchRunning()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		if (synchronization.getId() == null) {
			log.warn("given synch is not persisted, return false");
			return false;
		}

		log.info("synchronization:{}", synchronization);
		boolean isRunning = false;

		try {
			Synchronization latestSynchronization = synchronizationDAO
					.findById(synchronization.getId());

			if (latestSynchronization.getLocalIRODSTransfers() != null
					&& !latestSynchronization.getLocalIRODSTransfers()
							.isEmpty()) {
				for (LocalIRODSTransfer localIRODSTransfer : latestSynchronization
						.getLocalIRODSTransfers()) {
					if (localIRODSTransfer.getTransferState() == TransferState.ENQUEUED
							|| localIRODSTransfer.getTransferState() == TransferState.PROCESSING) {
						log.info("synch says it is running:{}",
								localIRODSTransfer);
						isRunning = true;
						break;
					}
				}
			}

			return isRunning;

		} catch (TransferDAOException e) {
			log.error("dao exception", e);
			throw new SynchException(
					"exception occurred checking if synch running", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.synch.SynchManagerService#findByName
	 * (java.lang.String)
	 */
	@Override
	@Transactional
	public Synchronization findByName(final String name) throws SynchException {
		try {
			return synchronizationDAO.findByName(name);
		} catch (TransferDAOException e) {
			log.error("dao exception finding synch", e);
			throw new SynchException("exception finding synchronizations", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.synch.SynchManagerService#findById(java
	 * .lang.Long)
	 */
	@Override
	@Transactional
	public Synchronization findById(final Long id) throws SynchException {
		log.info("findById()");
		try {
			return synchronizationDAO.findById(id);
		} catch (TransferDAOException e) {

			log.error("dao exception finding synch by name", e);
			throw new SynchException("exception finding synch by name", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#
	 * createNewSynchConfiguration
	 * (org.irods.jargon.transfer.dao.domain.SynchConfiguration)
	 */
	@Override
	@Transactional
	public void createNewSynchConfiguration(
			final Synchronization synchConfiguration)
			throws ConflictingSynchException, SynchException {
		if (synchConfiguration == null) {
			throw new IllegalArgumentException("null synchConfiguration");
		}
		log.info("createNewSynchConfiguration with config: {}",
				synchConfiguration);

		/*
		 * review this synch configuration against existing ones, there should
		 * not be a lot, so just list them and analyze
		 */

		validateSynch(synchConfiguration);

		// review pases, go ahead and add
		try {
			synchronizationDAO.save(synchConfiguration);
		} catch (TransferDAOException e) {
			log.error("synchConfiguration create failed with exception", e);
			throw new SynchException(e);
		}

		// TODO: per # 227, update irods synch folder
		/*
		 * txfr engine has the synch device database, AVU data is also kept in
		 * iRODS that has similar information. We'll need to keep these updated.
		 * See data-utils in jargon, SynchPropertiesService. The basic are
		 * already there, we might need a few extra methods. The iRODS side is
		 * AVU metadata.
		 * 
		 * check for already in use (is device name already used? Is this a
		 * re-configuration of the same device? (allow) etc There are methods in
		 * data-utils SynchConfigurationService. We might need to add a
		 * 'getAllDevicesForUser', etc.
		 * 
		 * mkirs if necessary
		 * 
		 * update dbase
		 * 
		 * add synch config entry as avu via SynchPropertiesService
		 */

		log.info("synch created");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#
	 * updateSynchConfiguration
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	@Transactional
	public void updateSynchConfiguration(final Synchronization synchronization)
			throws ConflictingSynchException, SynchException {
		if (synchronization == null) {
			throw new IllegalArgumentException("null synchConfiguration");
		}
		log.info("updateSynchConfiguration with config: {}", synchronization);

		/*
		 * review this synch configuration against existing ones, there should
		 * not be a lot, so just list them and analyze
		 */

		validateSynch(synchronization);

		// review pases, go ahead and update
		try {
			// ensure that no enqueued or processing transfers are in the queue
			/*
			 * if (isSynchRunning(synchronization)) { log.warn(
			 * "cannot update synch with enqueued or processing transfer:{}",
			 * synchronization); throw new ConflictingSynchException(
			 * "cannot update the synchronization, queue jobs need to be purged first"
			 * ); }
			 */

			synchronizationDAO.save(synchronization);
		} catch (TransferDAOException e) {
			log.error("synchConfiguration update failed with exception", e);
			if (e.getMessage().indexOf("duplicate key value") > -1) {
				throw new ConflictingSynchException(e.getMessage(), e);
			} else {
				throw new SynchException(e);
			}
		}

		// TODO: irods-side updates?

		log.info("synch updated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.synch.SynchManagerService#
	 * deleteSynchronization
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	@Transactional
	public void deleteSynchronization(Synchronization synchronization)
			throws SynchException {
		log.info("delete synchronization()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		// make sure synch is attached
		try {
			synchronization = synchronizationDAO.findById(synchronization
					.getId());
		} catch (TransferDAOException e) {
			log.error("error looking up synchronization", e);
			throw new SynchException("error deleting synchronization", e);
		}

		if (synchronization == null) {
			log.warn("did not find synchronization, ignore and return");
			return;
		}

		// ensure that no enqueued or processing transfers are in the queue
		for (LocalIRODSTransfer localIRODSTransfer : synchronization
				.getLocalIRODSTransfers()) {
			if (localIRODSTransfer.getTransferState() == TransferState.ENQUEUED
					|| localIRODSTransfer.getTransferState() == TransferState.PROCESSING) {
				log.warn(
						"cannot delete synch with enqueued or processing transfer:{}",
						localIRODSTransfer);
				throw new ConflictingSynchException(
						"cannot delete the synchronization, queue jobs need to be purged first");
			}
		}

		try {
			// synchronization.getLocalIRODSTransfers().clear();
			synchronizationDAO.delete(synchronization);
		} catch (TransferDAOException e) {
			log.error("synchConfiguration delete failed with exception", e);
			throw new SynchException("exception during delete", e);
		}
		log.info("synchronization deleted");
	}

	/**
	 * @param synchConfiguration
	 * @throws SynchException
	 */
	private void validateSynch(final Synchronization synchConfiguration)
			throws ConflictingSynchException, SynchException {

		log.info("validateSynch()");

		log.info("synch:{}", synchConfiguration);


		if (synchConfiguration.getIrodsSynchDirectory() == null
				|| synchConfiguration.getIrodsSynchDirectory().isEmpty()) {
			throw new SynchException("null or empty irodsSynchDirectory");
		}

		if (synchConfiguration.getLocalSynchDirectory() == null
				|| synchConfiguration.getLocalSynchDirectory().isEmpty()) {
			throw new SynchException("null or empty localSynchDirectory");
		}

		List<Synchronization> synchronizations;
		try {
			synchronizations = synchronizationDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("error listing existing synchronizations", e);
			boolean causeIsConstraint = e.getCause() instanceof ConstraintViolationException;
			if (causeIsConstraint) {
				throw new ConflictingSynchException(
						"duplciate data in synchronization", e);
			} else {
				throw new SynchException(
						"unable to find existing synchronizations", e);
			}
		}

		log.debug("existing synchs:{}", synchronizations);
		for (Synchronization existingSynchronization : synchronizations) {
			log.info("analyizing existing synchronization:{}",
					existingSynchronization);

			if (synchConfiguration.getId() != null
					&& existingSynchronization.getId().equals(
							synchConfiguration.getId())) {
				log.info("ids match, ignore");
				continue;
			}

			if (synchConfiguration.getName().equalsIgnoreCase(
					existingSynchronization.getName())) {
				log.error("a synch already exists with the name:{}",
						synchConfiguration.getName());
				throw new ConflictingSynchException(
						"a synchronization already exists with the same name");
			}

			if (synchConfiguration.getGridAccount().equals(
					existingSynchronization.getGridAccount())) {
				log.debug("grid account match");
			} else {
				log.debug("this config is not for the same host/zone, so through evaluating");
				continue;
			}

			log.debug("have an existing config for same host/zone, evaluate for overlap/conflict");
			if (synchConfiguration.getIrodsSynchDirectory().equals(
					existingSynchronization.getIrodsSynchDirectory())) {
				log.error(
						"an existing synchronization is already using the desired iRODS target collection:{}:",
						synchConfiguration);
				throw new ConflictingSynchException(
						"syncronization has duplicate iRODS target collection");
			}

			if (synchConfiguration.getLocalSynchDirectory().equals(
					existingSynchronization.getLocalSynchDirectory())) {
				log.error(
						"an existing synchronization is already using the desired local collection:{}:",
						synchConfiguration);
				throw new ConflictingSynchException(
						"syncronization has duplicate local collection");
			}

		}
	}

}
