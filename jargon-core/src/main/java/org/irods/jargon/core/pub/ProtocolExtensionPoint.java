package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.irods.jargon.core.packinstr.Tag;

/**
 * Interface describing an extension point to jargon, such that higher level
 * packages may implement subsets of the iRODS client protocol directly. This is
 * the object through which these protocol operations should flow.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface ProtocolExtensionPoint {

	/**
	 * Execute an iRODS packing instruction and return the {@code Tag} data
	 * encapsulating the iRODS response.
	 *
	 * @param irodsPI
	 *            {@link IRodsPI} instance that represents the given packing
	 *            instruction
	 * @return {@link Tag} that represents the response from iRODS, this must be
	 *         processed by the invoker
	 * @throws JargonException
	 *             for iRODS error
	 */
	Tag irodsFunction(final IRodsPI irodsPI) throws JargonException;

}