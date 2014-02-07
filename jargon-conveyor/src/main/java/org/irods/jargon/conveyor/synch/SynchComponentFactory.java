package org.irods.jargon.conveyor.synch;

import org.irods.jargon.transfer.dao.domain.Synchronization;

public interface SynchComponentFactory {

	/**
	 * Get an instance of the component that can create an appropriate diff
	 * model
	 * 
	 * @param synchronization
	 *            {@link Synchronization} that describes the type of diff
	 * @return
	 */
	public abstract AbstractSynchronizingDiffCreator instanceDiffCreator(
			Synchronization synchronization);

	/**
	 * Get an instance of the component that can create an appropriate diff
	 * model
	 * 
	 * @param synchronization
	 *            {@link Synchronization} that generates the diff to be
	 *            processed
	 * @return
	 */
	public abstract AbstractSynchronizingDiffProcessor instanceDiffProcessor(
			Synchronization synchronization);

}