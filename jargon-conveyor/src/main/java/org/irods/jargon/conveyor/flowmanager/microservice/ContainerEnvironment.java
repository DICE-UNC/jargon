/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.microservice;

import java.util.Properties;

import org.irods.jargon.conveyor.core.ConveyorService;

/**
 * Represents the container that will be running microservices, providing
 * something like a runtime environment, giving references to the flow manager
 * that is running the rules, as well as hooks to talk with iRODS. This
 * represents the stable, global state of the flow manager.
 * <p/>
 * This is used on concert with the <code>InvocationContext</code> that
 * represents the single-invocation environment, such as the current operating
 * transfer, the curent account used, and a <code>Map</code> of properties that
 * may be used to pass information between microservices.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ContainerEnvironment {

	/**
	 * Required dependency on the conveyor service that represents the transfer
	 * manager and orchestration layer
	 */
	private ConveyorService conveyorService;

	/**
	 * Optional properties that may be configured globally
	 */
	private Properties globalConfigurationProperties = new Properties();

	/**
	 * Get a reference to the main conveyor service, which represents the
	 * transfer queue and all recorded state
	 * 
	 * @return {@link ConveyorService}
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * Set a reference to the main conveyor service
	 * 
	 * @param conveyorService
	 *            {@link ConveyorService}
	 */
	public void setConveyorService(final ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	public Properties getGlobalConfigurationProperties() {
		return globalConfigurationProperties;
	}

	public void setGlobalConfigurationProperties(
			final Properties globalConfigurationProperties) {
		this.globalConfigurationProperties = globalConfigurationProperties;
	}

}
