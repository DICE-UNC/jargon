package org.irods.jargon.conveyor.core;


public interface ConveyorBootstrapper {

	public abstract ConveyorService bootstrap() throws BootstrapperException;

}