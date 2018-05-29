/**
 *
 */
package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * Abstract implementation of a Hierarchical Visitor. Basis for concrete visitor
 * implementations.
 *
 * @author conwaymc
 *
 */
public abstract class AbstractIrodsVisitorComponent extends AbstractJargonService implements HierVisitor {

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public AbstractIrodsVisitorComponent(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public AbstractIrodsVisitorComponent() {
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

	@Override
	public abstract void launch(final String startingCollectionPath);

}
