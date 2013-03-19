package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract definition of a framework that will invoke a given visitor (not
 * strictly a classic visitor pattern, but somewhere between a visitor and
 * strategy pattern), and allow that visitor to do arbitrary functions as it is
 * invoked. The 'visitor' class will have a reference the object being iterated,
 * as well as a reference to this object, providing the
 * <code>IRODSAccount</code> and <code>IRODSAccessObjectFactory</code>
 * references to do additional operations on the iRODS grid containing the
 * relevant data.
 * <p/>
 * Note that this class takes a generic reference <E> that represents the type
 * of data to be iterated or 'visited', such as a gen query result or a list of
 * Jargon domain objects from some Jargon operation.
 * <p/>
 * Note that the visited object has the ability to return a code to halt
 * operations, and this class has a method to cancel operations too.
 * <p/>
 * NOTE: this is an initial implementation, currently in use for the HIVE
 * project, and it is expected that this will change (and we'll write some nice
 * unit tests under here) as the actual requirements take shape. Use with
 * caution!
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * @param <R>
 * 
 */
public abstract class AbstractIRODSVisitorInvoker<E, R> extends
		AbstractJargonService {

	public enum VisitorDesiredAction {
		HALT, CONTINUE
	}

	private final AbstractIRODSVisitor<E, R> visitor;

	/**
	 * signal to the framework to cancel the iteration/visiting
	 */
	private volatile boolean cancel = false;

	public static final Logger log = LoggerFactory
			.getLogger(AbstractIRODSVisitorInvoker.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public AbstractIRODSVisitorInvoker(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractIRODSVisitor<E, R> visitor) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (visitor == null) {
			throw new IllegalArgumentException("null visitor");
		}

		this.visitor = visitor;
	}

	/**
	 * Access iRODS and derive the data that will be iterated and provided to
	 * the visitor object. The execute method should iterate or process over the
	 * underlying data and make invocations to the <code>visit()</code> method.
	 * <p/>
	 * This method will honor any cancellation signal when iterating
	 * 
	 * @throws NoMoreItemsException
	 *             if no more items were found, but were expected
	 * @throws JargonException
	 */
	protected void execute() throws NoMoreItemsException, JargonException {
		log.info("execute()");
		initializeInvoker();
		log.info("invoker initialized....now processing results");

		try {
			while (!cancel && hasMore()) {
				VisitorDesiredAction action = visitor.invoke(next(), this);
				if (action == VisitorDesiredAction.HALT) {
					log.info("halt returned from visitor, cancelling");
					cancel = true;
				}
			}
			log.info("processing complete, calling complete() on the visitor");
			visitor.complete(this);
		} catch (JargonException je) {
			log.error(
					"unhandled jargon exception in visitor processing, calling close and terminating",
					je);
			throw je;
		} catch (Exception e) {
			log.error(
					"unhandled  exception in visitor processing, calling close and terminating, rethrow as JargonException",
					e);
			throw new JargonException(
					"unhandled exception in visitor processing", e);

		} finally {
			log.info("close processing");
			close();
		}

	}

	/**
	 * Method template to be implemented by the developer to initialize the
	 * collection of items to iterate over. Note that the <code>next()</code>
	 * method will be called and this method will handle any 'requery' to obtain
	 * pages of data from iRODS
	 * 
	 * @throws JargonException
	 */
	protected abstract void initializeInvoker() throws JargonException;

	/**
	 * Method to access the next item in the collection to be iterated and
	 * 'visited'. This method will be responsible for any 'paging' that requires
	 * a re-query to iRODS.
	 * 
	 * @return <E> with the next item of iterated data to visit
	 * @throws NoMoreItemsException
	 * @throws JargonException
	 */
	protected abstract E next() throws NoMoreItemsException, JargonException;

	/**
	 * Return a <code>boolean</code> that indicates whether the underlying
	 * collection of data has more results to process
	 * 
	 * @return <code>boolean</code> of <code>true</code> if there is more data
	 *         to process
	 */
	protected abstract boolean hasMore();

	/**
	 * Complete the operation, called even if cancel or error occurs.
	 * <p/>
	 * Any resource freeing or final evaluation should be implemented here
	 * 
	 * @throws JargonException
	 */
	public abstract void close() throws JargonException;

	/**
	 * Checks if cancel has been called on this object, or a <code>HALT</code>
	 * was returned from the visitor.
	 * 
	 * @return
	 */
	public boolean isCancel() {
		return cancel;
	}

	/**
	 * signal that the iterator/visitor operation should be cancelled. Note that
	 * this can also be accomplished by returning a <code>HALT</code> value from
	 * the visitor implementation.
	 */
	public void setCancel(final boolean cancel) {
		log.warn("attempting to cancel...");
		this.cancel = cancel;
	}

}
