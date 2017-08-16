package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitorInvoker.VisitorDesiredAction;

/**
 * Generic visitor to be implemented by developers wishing to have a process
 * invoked based on visiting/iterating an iRODS collection or set of query
 * results.
 * <p>
 * E = type of data sent to the invoke method, R = return type from the complete
 * method back to the caller
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class AbstractIRODSVisitor<E> {

	public AbstractIRODSVisitor() {
	}

	/**
	 * Invoke an action based on processing an individual element in a
	 * collection. This is equivalent to iterating over a collection or query
	 * result
	 *
	 * @param visited
	 *            object of type <E> that is provided for a single instance of
	 *            the given collection of data to be processed by the visitor
	 * @param abstractIRODSVisitorInvoker
	 *            {@link AbstractIRODSVisitorInvoker} implementation that is
	 *            providing this event. This object has references to the
	 *            {@link IRODSAccount} and {@link IRODSAccessObjectFactory}
	 *            objects that may be used to further interact with iRODS. Note
	 *            that such interaction should be done on a separate thread so
	 *            as to segregate connections.
	 * @return a {@code VisitorDesiredAction} enum value that can signal
	 *         the client's desire to continue, halt, or take other actions
	 * @throws JargonException
	 */
	public abstract VisitorDesiredAction invoke(final E visited,
			final AbstractIRODSVisitorInvoker<E> abstractIRODSVisitorInvoker)
					throws JargonException;

	/**
	 * Final call from invoker when all items have been processed, signalling a
	 * successful (non cancelled) completion of processing for the given
	 * collection of data
	 *
	 * @throws JargonException
	 */
	public abstract void complete() throws JargonException;

}
