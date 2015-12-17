/**
 *
 */
package org.irods.jargon.datautils.tree;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitor;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitorInvoker;
import org.irods.jargon.datautils.visitor.AbstractIRODSVisitorInvoker.VisitorDesiredAction;

/**
 * For each file provided by an iterator, calculate summary statistics that can
 * be obtained by calling <code>getTreeSummary()</code> after the iteration
 * process is complete.
 *
 * @author Mike Conway - DICE
 *
 */
public class TreeSummarizingVisitor extends AbstractIRODSVisitor<File> {

	private final TreeSummary treeSummary = new TreeSummary();

	@Override
	public VisitorDesiredAction invoke(final File visited,
			final AbstractIRODSVisitorInvoker<File> abstractIRODSVisitorInvoker)
					throws JargonException {

		treeSummary.processFileInfo(visited);
		return VisitorDesiredAction.CONTINUE;
	}

	@Override
	public void complete() throws JargonException {

	}

	/**
	 * @return the treeSummary
	 */
	public TreeSummary getTreeSummary() {
		return treeSummary;
	}

}
