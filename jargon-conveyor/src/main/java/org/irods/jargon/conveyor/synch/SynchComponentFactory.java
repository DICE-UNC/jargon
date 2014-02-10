package org.irods.jargon.conveyor.synch;

import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.Synchronization;

public interface SynchComponentFactory {

	/**
	 * Get an instance of the component that can create an appropriate diff
	 * model
	 * 
	 * @param synchronization
	 *            {@link Synchronization} that describes the type of diff
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that can signal cancels, among
	 *            other things
	 * @return
	 */
	public abstract AbstractSynchronizingDiffCreator instanceDiffCreator(
			Synchronization synchronization,
			final TransferControlBlock transferControlBlock);

	/**
	 * Get an instance of the component that can create an appropriate diff
	 * model
	 * 
	 * @param synchronization
	 *            {@link Synchronization} that describes the type of diff
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that can signal cancels, among
	 *            other things
	 * @return
	 */
	public abstract AbstractSynchronizingDiffProcessor instanceDiffProcessor(
			Synchronization synchronization,
			final TransferControlBlock transferControlBlock);

}