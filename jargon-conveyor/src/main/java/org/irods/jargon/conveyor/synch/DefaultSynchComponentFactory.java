/**
 * 
 */
package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a factory to create various components used to process
 * synchronizations. This allows pluggable processors to create and resolve the
 * diffs that drive synchronization.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultSynchComponentFactory implements SynchComponentFactory {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultSynchComponentFactory.class);

	/**
	 * Injected dependency
	 */
	private ConveyorService conveyorService;

	/**
	 * 
	 */
	public DefaultSynchComponentFactory() {
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * Create an instance with an initialized reference to the conveyor service
	 * 
	 * @param conveyorService
	 *            {@link ConveyorService} reference
	 */
	public void setConveyorService(final ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.synch.SynchComponentFactory#instanceDiffCreator
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	public AbstractSynchronizingDiffCreator instanceDiffCreator(
			final Synchronization synchronization,
			final TransferControlBlock transferControlBlock) {

		log.info("instanceDiffCreator()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}
		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		switch (synchronization.getSynchronizationMode()) {
		case ONE_WAY_LOCAL_TO_IRODS:
			return new DefaultDiffCreator(getConveyorService(),
					transferControlBlock);
		case ONE_WAY_IRODS_TO_LOCAL:
			throw new UnsupportedOperationException("unsupported synch type");
		case BI_DIRECTIONAL:
			throw new UnsupportedOperationException("unsupported synch type");
		default:
			throw new UnsupportedOperationException("unsupported synch type");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.synch.SynchComponentFactory#instanceDiffProcessor
	 * (org.irods.jargon.transfer.dao.domain.Synchronization)
	 */
	@Override
	public AbstractSynchronizingDiffProcessor instanceDiffProcessor(
			final Synchronization synchronization,
			final TransferControlBlock transferControlBlock) {

		log.info("instanceDiffProcessor()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		switch (synchronization.getSynchronizationMode()) {
		case ONE_WAY_LOCAL_TO_IRODS:
			return new LocalToIRODSDiffProcessor(getConveyorService(),
					transferControlBlock);
		case ONE_WAY_IRODS_TO_LOCAL:
			throw new UnsupportedOperationException("unsupported synch type");
		case BI_DIRECTIONAL:
			throw new UnsupportedOperationException("unsupported synch type");
		default:
			throw new UnsupportedOperationException("unsupported synch type");
		}

	}
}
