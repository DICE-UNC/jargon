/**
 *
 */
package org.irods.jargon.datautils.visitor;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFileImpl;

/**
 * Concrete implementation of a
 *
 * @author conwaymc
 *
 */
public class IrodsVisitedLeaf extends IrodsFileItem implements HierLeaf {

	/**
	 *
	 */
	private static final long serialVersionUID = -235389028639499934L;

	/**
	 * @param pathName
	 *            {@code String} with dir path
	 * @param irodsFileSystemAO
	 *            {@link IRODSFileSystemAO}
	 *
	 * @throws JargonException
	 *             {@link JargonException}
	 */

	public IrodsVisitedLeaf(final String pathName, final IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(pathName, irodsFileSystemAO);
	}

	/**
	 * @param parent
	 *            {@code String} with the parent
	 * @param child
	 *            {@code String} with the child
	 * @param irodsFileSystemAO
	 *            {@link IRODSFileSystemAO}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public IrodsVisitedLeaf(final String parent, final String child, final IRODSFileSystemAO irodsFileSystemAO)
			throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	/**
	 * @param parent
	 *            {@link File} with the parent
	 * @param child
	 *            {@code String} with the child
	 * @param irodsFileSystemAO
	 *            {@link IRODSFileSystemAO}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public IrodsVisitedLeaf(final File parent, final String child, final IRODSFileSystemAO irodsFileSystemAO)
			throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	/**
	 * Handy constructor takes an <code>IRODSFileImpl</code>
	 *
	 * @param irodsFile
	 *            {@link IRODSFileImpl} that represents the iRODS file as a leaf
	 *            node
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public IrodsVisitedLeaf(final IRODSFileImpl irodsFile) throws JargonException {
		this(irodsFile.getAbsolutePath(), irodsFile.getIrodsFileSystemAO());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.visitor.HierComponent#accept(org.irods.jargon.
	 * datautils.visitor.HierVisitor)
	 */
	@Override
	public boolean accept(final HierVisitor visitor) throws JargonException {
		return visitor.visit(this);
	}

}
