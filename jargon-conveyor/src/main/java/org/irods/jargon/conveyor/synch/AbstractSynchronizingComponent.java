package org.irods.jargon.conveyor.synch;

import org.irods.jargon.conveyor.core.ConveyorService;

public class AbstractSynchronizingComponent {
	public ConveyorService conveyorService;

	public AbstractSynchronizingComponent(final ConveyorService conveyorService) {
		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}
		this.conveyorService = conveyorService;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @param conveyorService
	 *            the conveyorService to set
	 */
	public void setConveyorService(ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}
}