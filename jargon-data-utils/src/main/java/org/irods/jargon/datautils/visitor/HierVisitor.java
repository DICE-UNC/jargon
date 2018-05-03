/**
 * 
 */
package org.irods.jargon.datautils.visitor;

/**
 * Interface for the 'Visitor' in the pattern
 * 
 * @author conwaymc
 *
 */
public interface HierVisitor {

	/**
	 * Called on visiting a component that is a composite (a directory with
	 * children). This will answer <code>true</code> if it should be visited, and
	 * will call to visit each of this composite's children until told to stop.
	 * <p/>
	 * A composite returning false will cause the visiting of the children to halt.
	 * 
	 * @param node
	 *            {@link HierComposite} that is being visited in the current
	 *            iteration.
	 * @return {@link boolean} with a <code>true</code> if the node and the node's
	 *         children should be visited
	 */
	public boolean visitEnter(HierComposite node);

	/**
	 * Called on visiting a component that is a composite (a directory with
	 * children) when all the children of this composite have completed processing.
	 * This will answer <code>true</code> if siblings of this node should be
	 * processed. A return of <code>false</code> indicates that subsequent
	 * processing of sibling nodes should cease, short circuiting this level of the
	 * hierarchy.
	 * <p/>
	 * A composite returning false will cause the visiting of siblings to halt.
	 * 
	 * @param node
	 *            {@link HierComposite} that was being visited in the current
	 *            iteration.
	 * @param visitorEntered
	 *            {@link boolean} indicating that the visitEnter() method had
	 *            returned 'false'. Can be used for orchestrating proper action on
	 *            leave in an implementing indexer
	 * 
	 * @return {@link boolean} with a <code>true</code> if the node and the node's
	 *         siblings should be visited
	 * 
	 */
	public boolean visitLeave(HierComposite node, boolean visitorEntered);

	/**
	 * Called on visiting a leaf node (file). This visitor can return
	 * <code>false</code> to short-circuit processing of siblings.
	 * 
	 * @param node
	 *            is a {@link HierLeaf} leaf node that is a file in the hierarchy
	 * @return {@link boolean} indicating whether processing of siblings should
	 *         continue (<code>true</code>) or short-circuit.
	 */
	public boolean visit(HierLeaf node);
	
	public void launch(final String startingCollectionPath, final HierVisitor visitor);
	
	//public boolean shutdown();
}
