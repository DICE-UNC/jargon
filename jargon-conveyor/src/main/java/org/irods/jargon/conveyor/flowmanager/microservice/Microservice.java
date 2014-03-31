package org.irods.jargon.conveyor.flowmanager.microservice;

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
public abstract class Microservice {

	/**
	 * Enumeration of the results of an exec step, as returned by the execute()
	 * method
	 * 
	 */
	public enum ExecResult {
		CONTINUE, SKIP_THIS_INVOCATION, SKIP_REMAINING_INVOCATIONS, ABORT
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
			ContainerEnvironment containerEnvironment) {
		this.containerEnvironment = containerEnvironment;
	}

	/**
	 * Primary implementation hook. Implementor simply does what they need to in
	 * the execute() method and returns an ExecResult, or an exception if an
	 * error occurred.
	 * <p/>
	 * 
	 * @return {@link ExecResult} enumeration value that signals handling of
	 *         further microservices
	 * @throws MicroserviceException
	 */
	public abstract ExecResult execute() throws MicroserviceException;

	/**
	 * method that runs after an error occurs. By default, this method will
	 * return an <code>ExecResult.ABORT</code> status, so if this behavior is
	 * not desired in your microservice, it can be altered in the subclass
	 * 
	 * @param triggeringException
	 *            <code>Exception</code> that may have occurred,triggering this
	 *            handler. This may be null
	 * @param resultFromExecute
	 *            {2link ExecResult} received from execute method that may be
	 *            modified by this error handler to derive the final result
	 * 
	 * @return {@link ExecResult} after error evaluation and recovery
	 * @throws MicroserviceException
	 */
	public ExecResult errorHandler(final Exception triggeringException,
			final ExecResult resultFromExecute) throws MicroserviceException {
		return ExecResult.ABORT;
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
	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

}
