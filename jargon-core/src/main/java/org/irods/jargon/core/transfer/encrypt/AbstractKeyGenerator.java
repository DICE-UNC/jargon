/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import javax.crypto.SecretKey;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for a shared encryption key generator implementation of a
 * particular algorithm and configuration
 *
 * @author Mike Conway - DFC
 *
 */
public abstract class AbstractKeyGenerator {

	private PipelineConfiguration pipelineConfiguration;
	private NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

	public static final Logger log = LoggerFactory
			.getLogger(AbstractKeyGenerator.class);

	protected PipelineConfiguration getPipelineConfiguration() {
		return pipelineConfiguration;
	}

	protected NegotiatedClientServerConfiguration getNegotiatedClientServerConfiguration() {
		return negotiatedClientServerConfiguration;
	}

	protected static Logger getLog() {
		return log;
	}

	public AbstractKeyGenerator(
			final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super();
		this.pipelineConfiguration = pipelineConfiguration;
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;
	}

	/**
	 * Generate an {@link SecretKey} based on the pipeline configuration. Note
	 * that this method should NOT update the {@code SecretKey} in the
	 * {@code NegotiatedClientServerConfiguration}
	 *
	 * @return {@link SecretKey} for the algo and settings in the pipeline and
	 *         negotiated configuration
	 * @throws EncryptionException
	 */
	public abstract SecretKey generateKey() throws EncryptionException;

}
