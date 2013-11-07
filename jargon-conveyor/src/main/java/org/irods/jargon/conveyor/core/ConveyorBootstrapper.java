package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

public interface ConveyorBootstrapper {

	public abstract ConveyorService bootstrap(
			IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws BootstrapperException;

}