/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

/**
 * Superclass for a DSL element that deals with microservices. It can do things
 * like validate the microservice
 * 
 * @author Mike Conway - DICE
 *
 */
public class FlowSpecDslMicroserviceElement extends FlowSpecDslElement {

	/**
	 * Create an instance of the microservice. Note that this instance has not
	 * yet been injected with the necessary context information, this will be
	 * done later
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	protected Microservice createMicroserviceInstance(
			final String microserviceFullyQualifiedClassName) {

		if (microserviceFullyQualifiedClassName == null
				|| microserviceFullyQualifiedClassName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty microserviceFullyQualifiedClassName");
		}

		try {
			return (Microservice) Class.forName(
					microserviceFullyQualifiedClassName).newInstance();
		} catch (InstantiationException e) {
			throw new FlowSpecificationException(
					"instantation exception creating microservice", e);
		} catch (IllegalAccessException e) {
			throw new FlowSpecificationException(
					"illegalAccessException creating microservice", e);
		} catch (ClassNotFoundException e) {
			throw new FlowSpecificationException(
					"class not found exception creating microservice", e);
		}

	}

	/**
	 * Check to see if the microservice can be created
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	protected boolean checkMicroservice(
			final String microserviceFullyQualifiedClassName) {

		if (microserviceFullyQualifiedClassName == null
				|| microserviceFullyQualifiedClassName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty microserviceFullyQualifiedClassName");
		}

		try {
			createMicroserviceInstance(microserviceFullyQualifiedClassName);
			return true;
		} catch (FlowSpecificationException e) {
			return false;
		}

	}

	/**
	 * @param flowSpec
	 */
	public FlowSpecDslMicroserviceElement(FlowSpec flowSpec) {
		super(flowSpec);
	}

}
