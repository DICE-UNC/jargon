/**
 *
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.JargonException;

/**
 * Accomplish encryption using a specified cipher if SSL was negotiated
 *
 * @author Mike Conway - DICE
 *
 */
public abstract class AbstractParallelEncryptWrapper extends AbstractParallelCipherWrapper {

	AbstractParallelEncryptWrapper(final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/**
	 * Encrypt the given data using the preconfigured algo
	 *
	 * @return {@code byte[]} with the encrypted data;
	 */
	abstract byte[] encrypt(final byte[] input) throws JargonException;

}
