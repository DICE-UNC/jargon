/**
 * 
 */
package org.irods.jargon.datautils.visitor;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;

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
	 * @param irodsFileSystemAO
	 * @throws JargonException
	 */
	public IrodsVisitedLeaf(String pathName, IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(pathName, irodsFileSystemAO);
	}

	/**
	 * @param parent
	 * @param child
	 * @param irodsFileSystemAO
	 * @throws JargonException
	 */
	public IrodsVisitedLeaf(String parent, String child, IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	/**
	 * @param parent
	 * @param child
	 * @param irodsFileSystemAO
	 * @throws JargonException
	 */
	public IrodsVisitedLeaf(File parent, String child, IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.visitor.HierComponent#accept(org.irods.jargon.
	 * datautils.visitor.HierVisitor)
	 */
	@Override
	public boolean accept(HierVisitor visitor) throws JargonException {
		return visitor.visit(this);
	}

}
