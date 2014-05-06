package org.irods.jargon.conveyor.flowmanager.microservice;

import org.irods.jargon.core.transfer.TransferStatus;

/**
 * 
 */

/**
 * Abstract base class for client side microservice. Somewhat analogous to an
 * iRODS microservice, this is an executable unit of code that performs a
 * specific function, and is meant to be chained together into a larger
 * workflow.
 * <p/>
 * This class can be extended by clients to create new microservices.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class Microservice {

	/**
	 * Enumeration of the results of an exec step, as returned by the execute()
	 * method
	 * 
	 */
	public enum ExecResult {
		CONTINUE, SKIP_THIS_CHAIN, SKIP_THIS_FILE, CANCEL_OPERATION, ABORT_AND_TRIGGER_ANY_ERROR_HANDLER, TERMINATE_FLOW_FAIL_PRECONDITION
	}

	/**
	 * Injected reference to the global environment for the flow manager
	 * container. This provides access to the conveyor service, which can be
	 * used to query and manipulate the transfer queue
	 */
	private ContainerEnvironment containerEnvironment;

	/**
	 * Injected reference to the current context (operation) that triggered this
	 * invocation, along with a simple store to pass values between
	 * microservices
	 */
	private InvocationContext invocationContext;

	public Microservice() {
	}

	/**
	 * Gets a reference to the global environment in which this microservice is
	 * running
	 * 
	 * @return {@link ContainerEnvironment}
	 */
	public ContainerEnvironment getContainerEnvironment() {
		return containerEnvironment;
	}

	/**
	 * Sets a reference to the global environment in which this microservice is
	 * running
	 * 
	 * @param containerEnvironment
	 *            {@link ContainerEnvironment}
	 */
	public void setContainerEnvironment(
			final ContainerEnvironment containerEnvironment) {
		this.containerEnvironment = containerEnvironment;
	}

	/**
	 * Primary implementation hook. Implementor simply does what they need to in
	 * the execute() method and returns an ExecResult, or an exception if an
	 * error occurred.
	 * <p/>
	 * 
	 * @param transferStatus
	 *            {@link TransferStatus} that initiates this microservice
	 * @return {@link ExecResult} enumeration value that signals handling of
	 *         further microservices
	 * @throws MicroserviceException
	 */
	public ExecResult execute(final TransferStatus transferStatus)
			throws MicroserviceException {

		if (transferStatus == null) {
			throw new IllegalArgumentException("null transferStatus");
		}

		return ExecResult.CONTINUE;
	}

	/**
	 * @return the {@link InvocationContext}
	 */
	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

	/**
	 * @param invocationContext
	 *            the {@link InvocationContext} to set
	 */
	public void setInvocationContext(final InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

	/**
	 * Handy method that will evaluate whether the microservice has been
	 * correctly provisioned. This is validated when the flow manager executes a
	 * microservice as a nice sanity check.
	 */
	public void evaluateContext() {
		if (getContainerEnvironment() == null) {
			throw new IllegalStateException("null container environment");
		}

		if (getContainerEnvironment().getConveyorService() == null) {
			throw new IllegalStateException(
					"null conveyor service in container environment");
		}

		if (getContainerEnvironment().getGlobalConfigurationProperties() == null) {
			throw new IllegalStateException(
					"null globalConfigurationProperties in container environment");
		}

		if (getInvocationContext() == null) {
			throw new IllegalStateException("null invocation context");
		}

		if (getInvocationContext().getIrodsAccount() == null) {
			throw new IllegalStateException(
					"null irodsAccount in invocation context");
		}

		if (getInvocationContext().getSharedProperties() == null) {
			throw new IllegalStateException(
					"null shared properties in invocation context");
		}

		if (getInvocationContext().getTransferAttempt() == null) {
			throw new IllegalStateException(
					"null transferAttempt in invocation context");
		}

		if (getInvocationContext().getTransferControlBlock() == null) {
			throw new IllegalStateException(
					"null transferControlBlock in invocation context");
		}

	}

}
