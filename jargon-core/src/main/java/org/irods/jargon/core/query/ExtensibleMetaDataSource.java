/**
 * 
 */
package org.irods.jargon.core.query;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 *         Defines an object that can provide an ExtensibleMetaDataMapping from
 *         an arbitrary source
 */

public interface ExtensibleMetaDataSource {

	/**
	 * Using some data source, the implementation should be able to generate an
	 * {@link org.irods.jargon.core.query.ExtensibleMetaDataMapping
	 * ExtensibleMetaDataMapping} object.
	 * 
	 * @return {@link org.irods.jargon.core.query.ExtensibleMetaDataMapping
	 *         ExtensibleMetaDataMapping}
	 * @throws JargonException
	 *             an <code>Exception</code> that wraps any underlying issue
	 */
	public ExtensibleMetaDataMapping generateExtensibleMetaDataMapping()
			throws JargonException;

}
