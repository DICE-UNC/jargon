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
import org.irods.jargon.core.transfer.TransferStatus;
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

	private static final Logger log = LoggerFactory
			.getLogger(FlowCoProcessor.class);

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

	/**
	 * Run the pre-op chain. This can result in (based on the responses from the
	 * microservices in ExecResult) a cancellation, or an abort of the transfer
	 * with an error
	 * 
	 * @param flowSpec
	 *            {@link FlowSpec} that was selected
	 * @param transferStatus
	 *            {@link TransferStatus} that triggers this call
	 * @return {@link ExecResult} from the microservices
	 * @throws ConveyorExecutionException
	 */
	ExecResult executePreOperationChain(final FlowSpec flowSpec,
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {

		log.info("executePreOperationChain()");

		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		Microservice microservice;
		ExecResult overallExecResult = ExecResult.CONTINUE;
		for (String microserviceFqcn : flowSpec.getPreOperationChain()) {

			log.info("pre-op chain microservice:{}", microserviceFqcn);
			microservice = createAndProvisionChainMicroservice(microserviceFqcn);
			log.info("invoking next in chain...");
			try {
				overallExecResult = microservice.execute(transferStatus);
			} catch (MicroserviceException e) {
				/*
				 * Errors that occur are treated as system or program erros, not
				 * as recoverable errors that should be reflected in the
				 * ExecResult
				 */
				log.error("error ocurred running a microservice", e);
				throw new ConveyorExecutionException(
						"error running microservice", e);
			}

			if (overallExecResult == ExecResult.CANCEL_OPERATION) {
				log.info("transfer is being cancelled");
				callable.getTransferControlBlock().setCancelled(true);
				break;
			} else if (overallExecResult == ExecResult.SKIP_THIS_CHAIN) {
				log.info("skipping rest of chain");
				break;
			} else if (overallExecResult == ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER) {
				log.error("abort of operation by execResult of microservice");
				executeAnyFailureMicroservice(flowSpec);
				throw new ConveyorExecutionException(
						"Aborting operation through failure of microservice");
			} else if (overallExecResult != ExecResult.CONTINUE) {
				log.error("unexpected exec result for a preop chain:{}",
						overallExecResult);
				throw new ConveyorExecutionException("unexpected exec result");
			}
		}

		return overallExecResult;

	}

	/**
	 * Run the pre-file operation chain. This can result in (based on the
	 * responses from the microservices in ExecResult) a cancellation, or an
	 * abort of the transfer with an error, or ask that this file be excluded
	 * from the transfer
	 * 
	 * @param flowSpec
	 *            {@link FlowSpec} that was selected
	 * @param transferStatus
	 *            {@link TransferStatus} that triggers this call
	 * @return {@link ExecResult} from the microservices
	 * @throws ConveyorExecutionException
	 */
	ExecResult executePreFileChain(final FlowSpec flowSpec,
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {

		log.info("executePreFileChain()");

		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		Microservice microservice;
		ExecResult overallExecResult = ExecResult.CONTINUE;
		for (String microserviceFqcn : flowSpec.getPreFileChain()) {

			log.info("pre-file chain microservice:{}", microserviceFqcn);
			microservice = createAndProvisionChainMicroservice(microserviceFqcn);
			log.info("invoking next in chain...");
			try {
				overallExecResult = microservice.execute(transferStatus);
			} catch (MicroserviceException e) {
				/*
				 * Errors that occur are treated as system or program erros, not
				 * as recoverable errors that should be reflected in the
				 * ExecResult
				 */
				log.error("error ocurred running a microservice", e);
				throw new ConveyorExecutionException(
						"error running microservice", e);
			}

			if (overallExecResult == ExecResult.CANCEL_OPERATION) {
				log.info("transfer is being cancelled");
				callable.getTransferControlBlock().setCancelled(true);
				break;
			} else if (overallExecResult == ExecResult.SKIP_THIS_CHAIN) {
				log.info("skipping rest of chain");
				break;
			} else if (overallExecResult == ExecResult.SKIP_THIS_FILE) {
				log.info("skipping file, and rest of chain");
				break;
			} else if (overallExecResult == ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER) {
				log.error("abort of operation by execResult of microservice");
				executeAnyFailureMicroservice(flowSpec);
				throw new ConveyorExecutionException(
						"Aborting operation through failure of microservice");
			} else if (overallExecResult != ExecResult.CONTINUE) {
				log.error("unexpected exec result for a preop chain:{}",
						overallExecResult);
				throw new ConveyorExecutionException("unexpected exec result");
			}
		}

		return overallExecResult;

	}

	/**
	 * Run the post-file operation chain. This can result in (based on the
	 * responses from the microservices in ExecResult) a cancellation, or an
	 * abort of the transfer with an error
	 * 
	 * @param flowSpec
	 *            {@link FlowSpec} that was selected
	 * @param transferStatus
	 *            {@link TransferStatus} that triggers this call
	 * @return {@link ExecResult} from the microservices
	 * @throws ConveyorExecutionException
	 */
	ExecResult executePostFileChain(final FlowSpec flowSpec,
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {

		log.info("executePostFileChain()");

		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		Microservice microservice;
		ExecResult overallExecResult = ExecResult.CONTINUE;
		for (String microserviceFqcn : flowSpec.getPostFileChain()) {

			log.info("post-file chain microservice:{}", microserviceFqcn);
			microservice = createAndProvisionChainMicroservice(microserviceFqcn);
			log.info("invoking next in chain...");
			try {
				overallExecResult = microservice.execute(transferStatus);
			} catch (MicroserviceException e) {
				/*
				 * Errors that occur are treated as system or program erros, not
				 * as recoverable errors that should be reflected in the
				 * ExecResult
				 */
				log.error("error ocurred running a microservice", e);
				throw new ConveyorExecutionException(
						"error running microservice", e);
			}

			if (overallExecResult == ExecResult.CANCEL_OPERATION) {
				log.info("transfer is being cancelled");
				callable.getTransferControlBlock().setCancelled(true);
				break;
			} else if (overallExecResult == ExecResult.SKIP_THIS_CHAIN) {
				log.info("skipping rest of chain");
				break;
			} else if (overallExecResult == ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER) {
				log.error("abort of operation by execResult of microservice");
				executeAnyFailureMicroservice(flowSpec);
				throw new ConveyorExecutionException(
						"Aborting operation through failure of microservice");
			} else if (overallExecResult != ExecResult.CONTINUE) {
				log.error("unexpected exec result for a post file chain:{}",
						overallExecResult);
				throw new ConveyorExecutionException("unexpected exec result");
			}
		}

		return overallExecResult;

	}

	void executeAnyFailureMicroservice(FlowSpec flowSpec) {

		log.error("failure stuff no implemented yet");
		throw new UnsupportedOperationException("implement me!!! please?");

	}

	/**
	 * Create a microservice from a fully qualified class name and provision it
	 * with the various context objects
	 * 
	 * @param microserviceFqcn
	 * @return
	 */
	private Microservice createAndProvisionChainMicroservice(
			String microserviceFqcn) {
		Microservice microservice = this
				.createMicroserviceInstance(microserviceFqcn);
		this.provisionMicroservice(microservice);
		return microservice;
	}

	/**
	 * Given a flow, execute any condition microservice and decide whether to
	 * run this flow
	 * 
	 * @param flowSpec
	 *            {@link FlowSpec} that will be evaluated
	 * @param transferStatus
	 *            {@link TransferStatus} that triggers this call
	 * 
	 * @return <code>boolean</code> that will indicate whether the given flow
	 * 
	 *         should run
	 * @throws ConveyorExecutionException
	 */
	boolean evaluateCondition(final FlowSpec flowSpec,
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		log.info("evaluateCondition()");

		if (flowSpec == null) {
			throw new IllegalArgumentException("null flowSpec");
		}

		if (flowSpec.getCondition().isEmpty()) {
			log.info("no condition specified, select this one");
			return true;
		}

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
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
			result = microservice.execute(transferStatus);
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

	/**
	 * @return the invocationContext
	 */
	InvocationContext getInvocationContext() {
		return invocationContext;
	}

}
