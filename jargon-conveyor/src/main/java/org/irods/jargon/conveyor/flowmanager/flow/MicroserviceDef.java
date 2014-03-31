/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

/**
 * Definition of a microservice in the flow, which will map to a class name, and
 * will be loaded for this given name
 * 
 * @author Mike Conway - DICE
 * 
 */
public class MicroserviceDef {

	/**
	 * Represents a spec of a microservice fully qualified class name
	 */
	private String microserviceClass;

	/**
	 * @return the microserviceClass
	 */
	public String getMicroserviceClass() {
		return microserviceClass;
	}

	/**
	 * @param microserviceClass
	 *            the microserviceClass to set
	 */
	public void setMicroserviceClass(String microserviceClass) {
		this.microserviceClass = microserviceClass;
	}

}
