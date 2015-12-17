/**
 *
 */
package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitor;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitorInvoker;
import org.irods.jargon.datautils.visitor.NoMoreItemsException;

/**
 * Invoker that will iterate over every file in a given parent directory
 * (recursively), calling the provided visitor for each file.
 * <p/>
 * This iterator is based on Apache FileUtils iterator methods and works for
 * both local and iRODS files
 *
 * @author Mike Conway - DICE
 *
 */
public class FileTreeIteratorVisitorInvoker extends
AbstractIRODSVisitorInvoker<File> {

	private final File parentFile;
	private Iterator<File> iter = null;

	/**
	 * Create an invoker that will iterate over childen of the provided parent
	 * directory, calling the provided visitor class at each file. Note that the
	 * semantics of this framework are iRODS oriented so you have to provide the
	 * iRODS values no matter what.
	 * <p/>
	 * Sorry...you can write your own version without the iRODS semantics...I'm
	 * in a hurry
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can connect to iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} used to connect to iRODS
	 * @param visitor
	 *            {@link AbstractIRODSVisitor} subclass that will be called for
	 *            each node
	 * @param parentFile
	 *            {@link} file (can be an IRODSFile) that is the parent
	 */
	public FileTreeIteratorVisitorInvoker(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractIRODSVisitor<File> visitor, final File parentFile) {
		super(irodsAccessObjectFactory, irodsAccount, visitor);

		if (parentFile == null) {
			throw new IllegalArgumentException("null or empty parentFile");
		}

		this.parentFile = parentFile;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.visitor.AbstractIRODSVisitorInvoker#
	 * initializeInvoker()
	 */
	@Override
	protected void initializeInvoker() throws JargonException {

		if (!parentFile.exists()) {
			throw new JargonException("parentFile does not exist");
		}

		if (!parentFile.isDirectory()) {
			throw new JargonException("parentFile  is not a directory");
		}

		iter = FileUtils.iterateFiles(parentFile, TrueFileFilter.TRUE,
				TrueFileFilter.TRUE);

	}

	@Override
	protected File next() throws NoMoreItemsException, JargonException {
		return iter.next();
	}

	@Override
	protected boolean hasMore() throws JargonException {
		return iter.hasNext();
	}

	@Override
	public void close() throws JargonException {

	}

}
