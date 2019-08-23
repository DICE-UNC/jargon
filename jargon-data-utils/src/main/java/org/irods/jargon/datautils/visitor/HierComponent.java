/**
 *
 */
package org.irods.jargon.datautils.visitor;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for the 'Component' in the Hierarchical Visitor pattern. This is
 * the file or directory (leaf or composite) that is accessed by the crawler
 *
 * @author conwaymc
 *
 */
public interface HierComponent {

	/**
	 * Called by the framework to notify the node that a visitor is here
	 *
	 * @param visitor
	 *            {@link HierVisitor} that is visiting. This is an adaptation of the
	 *            InternalIterator pattern (http://wiki.c2.com/?InternalIterator)
	 * @return {@code boolean} with an indication that the visitor said to leave the
	 *         iteration and short-circuit the operation
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public boolean accept(HierVisitor visitor) throws JargonException;

	/**
	 * Get the display name of the particular node under the hierarchy, essentially
	 * the last part of the iRODS path that is the collection or file name. The
	 * relative path.
	 *
	 * @return {@link String} with the node label
	 */
	public String getName();

	/**
	 * Get the full iRODS absolute path to the node
	 *
	 * @return {@code String} with the iRODS absolute path
	 */
	public String getAbsolutePath();

}
