/**
 * 
 */
package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * Abstract implementation of a Hierarchical Visitor.
 * 
 * @author conwaymc
 *
 */
public abstract class AbstractIrodsVisitor extends AbstractJargonService implements HierVisitor {

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public AbstractIrodsVisitor(IRODSAccessObjectFactory irodsAccessObjectFactory, IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public AbstractIrodsVisitor() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.visitor.HierVisitor#visitEnter(org.irods.jargon.
	 * datautils.visitor.HierComposite)
	 */
	@Override
	public abstract boolean visitEnter(HierComposite node);

	@Override
	public abstract boolean visitLeave(HierComposite node, boolean visitorEntered);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.visitor.HierVisitor#visit(org.irods.jargon.
	 * datautils.visitor.HierLeaf)
	 */
	@Override
	public abstract boolean visit(HierLeaf node);

}
