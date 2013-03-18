package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitorInvoker.VisitorDesiredAction;

/**
 * Generic visitor to be implemented by developers wishing to have a process
 * invoked based on visiting/iterating an iRODS collection or set of query
 * results.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractIRODSVisitor<E> {

	public AbstractIRODSVisitor() {
	}

	public abstract VisitorDesiredAction invoke(final E visited,
			final AbstractIRODSVisitorInvoker<E> invoker)
			throws JargonException;

}
