/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;

/**
 * Overlay of iRODS header containing SSL encryption algo and key information
 * for SSL negotiation. This is used for encryption of parallel transfers where
 * 
 * @author Mike Conway - DICE
 *
 */
public class SslEncryptionHeader {

	private final IRODSSession irodsSession;

	/**
	 * Default constructor required session
	 * 
	 * @param irodsSession
	 *            {@link IRODSSession}
	 */
	public SslEncryptionHeader(final IRODSSession irodsSession) {
		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}
		this.irodsSession = irodsSession;
	}

	/**
	 * Get the ssl parameter 'header' as used in libssl.cpp to transmit the
	 * parallel transfer encryption settings encoded as a variant of the iRODS
	 * header
	 * 
	 * @param encryptionAlgorithmEnum
	 * @param encryptionKeySize
	 * @param encryptionSaltSize
	 * @param encryptionNumberHashRounds
	 * @return <code>byte[]</code> with encryption data
	 * @throws JargonException
	 */
	public byte[] instanceBytesForSslParameters(
			final EncryptionAlgorithmEnum encryptionAlgorithmEnum,
			final int encryptionKeySize, final int encryptionSaltSize,
			final int encryptionNumberHashRounds) throws JargonException {

		byte[] encryptionHeader = AbstractIRODSMidLevelProtocol
				.createHeaderBytesFromData(encryptionAlgorithmEnum
						.getTextValue(), encryptionKeySize, encryptionSaltSize,
						encryptionNumberHashRounds, 0, irodsSession
								.getJargonProperties().getEncoding());
		return encryptionHeader;

	}
}
