/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.dsl.FlowSpecificationException;
import org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.ContainerEnvironment;
import org.irods.jargon.conveyor.flowmanager.microservice.InvocationContext;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice.ExecResult;
import org.irods.jargon.conveyor.flowmanager.microservice.MicroserviceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Co-processor for flows, handles manipulations and running of flows associated
 * with a callable operation
 * 
 * @author Mike Conway - DICE
 * 
 */
class FlowCoProcessor {

	private final AbstractConveyorCallable callable;
	private final ContainerEnvironment containerEnvironment = new ContainerEnvironment();
	private final InvocationContext invocationContext = new InvocationContext();

	/**
	 * Constructor for co processor takes the sister
	 * <code>AbstractConveyorCallable</code> that will be doing operations on
	 * flows
	 * 
	 * @param callable
	 *            {@link AbstractConveyorCallable} that will be procssing the
	 *            transfer and running flows
	 * @throws ConveyorExecutionException
	 */
	FlowCoProcessor(AbstractConveyorCallable callable)
			throws ConveyorExecutionException {
		super();
		if (callable == null) {
			throw new IllegalArgumentException("null callable");
		}
		this.callable = callable;
		containerEnvironment.setConveyorService(callable.getConveyorService());
		invocationContext.setIrodsAccount(callable
				.getIRODSAccountForGridAccount(callable.getTransfer()
						.getGridAccount()));
		invocationContext.setTransferAttempt(callable.getTransferAttempt());
		invocationContext.setTransferControlBlock(callable
				.getTransferControlBlock());

	}

	private static final Logger log = LoggerFactory
			.getLogger(FlowCoProcessor.class);

	/**
	 * Given a flow, execute any condition microservice and decide whether to
	 * run this flow
	 * 
	 * @param flowSpec
	 *            {@link FlowSpec} that will be evaluated
	 * @return <code>boolean</code> that will indicate whether the given flow
	 *         should run
	 * @throws ConveyorExecutionException
	 */
	boolean evaluateCondition(final FlowSpec flowSpec)
			throws ConveyorExecutionException {
		log.info("evaluateCondition()");

		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}

		if (flowSpec.getCondition().isEmpty()) {
			log.info("no condition specified, select this one");
			return true;
		}

		log.info("have a condition..evaluate it");

		Microservice microservice = this.createMicroserviceInstance(flowSpec
				.getCondition());

		if (microservice instanceof ConditionMicroservice) {
			log.info("have a condition microservice");
		} else {
			throw new ConveyorExecutionException(
					"condition is not a subclass of a ConditionMicroservice");
		}

		provisionMicroservice(microservice);

		log.info("executing condition....");
		ExecResult result;
		try {
			microservice.evaluateContext();
			result = microservice.execute();
		} catch (MicroserviceException e) {
			log.error("microservice exception executing condition", e);
			throw new ConveyorExecutionException(
					"unable to run condition microservice, this will be an error in the transfer",
					e);
		}

		log.info("evaluation of condition:{}", result);
		if (result == ExecResult.CONTINUE) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Augment the provided microservice with existing context information,
	 * making it ready to use
	 * 
	 * @param microservice
	 */
	private void provisionMicroservice(Microservice microservice) {
		log.info("provision microservice");
		microservice.setContainerEnvironment(containerEnvironment);
		microservice.setInvocationContext(invocationContext);
	}

	/**
	 * Create an instance of the microservice. Note that this instance has not
	 * yet been injected with the necessary context information, this will be
	 * done later
	 * 
	 * @param microserviceFullyQualifiedClassName
	 * @return
	 */
	private Microservice createMicroserviceInstance(
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

}
