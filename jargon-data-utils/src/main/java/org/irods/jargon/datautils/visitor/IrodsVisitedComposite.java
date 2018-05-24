/**
*
*/
package org.irods.jargon.datautils.visitor;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of an iRODS directory as visited by a hierarchical
 * visitor implementation
 *
 * @author conwaymc
 *
 */
public class IrodsVisitedComposite extends IrodsFileItem implements HierComposite {

	public static final Logger log = LoggerFactory.getLogger(IrodsVisitedComposite.class);

	private static final long serialVersionUID = 2022187755904761797L;

	/**
	 * @param pathName
	 *            {@code String} with dir path
	 * @param irodsFileSystemAO
	 *            {@link IRODSFileSystemAO}
	 *
	 * @throws JargonException
	 *             {@link JargonException}
	 */

	public IrodsVisitedComposite(final String pathName, final IRODSFileSystemAO irodsFileSystemAO)
			throws JargonException {
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
	public IrodsVisitedComposite(final String parent, final String child, final IRODSFileSystemAO irodsFileSystemAO)
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
	public IrodsVisitedComposite(final File parent, final String child, final IRODSFileSystemAO irodsFileSystemAO)
			throws JargonException {
		super(parent, child, irodsFileSystemAO);
	}

	public IrodsVisitedComposite(final IRODSFileImpl irodsFile) throws JargonException {
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

		log.info("accept()");
		if (visitor == null) {
			throw new IllegalArgumentException("null visitor");
		}
		log.debug("check if enter at:{}", getAbsolutePath());
		boolean visitorEntered = visitor.visitEnter(this);
		if (visitorEntered) { // enter this node?

			log.debug("entering...");
			for (File file : this.listFiles()) {
				if (file.isDirectory()) {
					IrodsVisitedComposite child = new IrodsVisitedComposite((IRODSFileImpl) file);
					if (!child.accept(visitor)) {
						log.info("child doesn't accept, short circuit rest of siblings at:{}", child.getAbsolutePath());
						break;
					}
				} else {
					IrodsVisitedLeaf leaf = new IrodsVisitedLeaf((IRODSFileImpl) file);
					if (!leaf.accept(visitor)) {
						log.info("child doesn't accept, short circuit rest of siblings at:{}", leaf.getAbsolutePath());
						break;
					}
				}
			}

		}

		log.debug("done with children...");
		return visitor.visitLeave(this, visitorEntered);
	}

}
