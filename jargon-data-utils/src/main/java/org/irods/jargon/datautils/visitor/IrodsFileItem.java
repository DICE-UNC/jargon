/**
 * 
 */
package org.irods.jargon.datautils.visitor;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFileImpl;

/**
 * Represents an iRODS file extended to include visitor semantics
 * 
 * @author conwaymc
 *
 */
public abstract class IrodsFileItem extends IRODSFileImpl implements HierComponent {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 3234717720636343981L;

	/**
	 * @param pathName
	 * @param irodsFileSystemAO
	 * @throws JargonException
	 */
	public IrodsFileItem(String pathName, IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(pathName, irodsFileSystemAO);
	}

	/**
	 * @param parent
	 * @param child
	 * @param irodsFileSystemAO
	 * @throws JargonException
	 */
	public IrodsFileItem(String parent, String child, IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	/**
	 * @param parent
	 * @param child
	 * @param irodsFileSystemAO
	 * @throws JargonException
	 */
	public IrodsFileItem(File parent, String child, IRODSFileSystemAO irodsFileSystemAO) throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	@Override
	public abstract boolean accept(HierVisitor visitor) throws JargonException;

}
