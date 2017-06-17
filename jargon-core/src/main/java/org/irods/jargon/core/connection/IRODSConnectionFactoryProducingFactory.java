/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * A factory for producing connection factories.
 * <p>
 * In this implementation, this factory will create connection factory based on
 * the provided jargon properties. This will create the factory object that
 * initializes the low level networking connection code used by Jargon. This
 * would allow for the later development of an nio based layer or alternative
 * tcp layers going forward with minimal headaches.
 *
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 *
 */
public class IRODSConnectionFactoryProducingFactory {

	/**
	 * Default (no values) constructor
	 */
	public IRODSConnectionFactoryProducingFactory() {
	}

	/**
	 * Given the {@code JargonProperties}, return the factory that will
	 * create the networking layer
	 *
	 * @param jargonProperties
	 * @return {@link IRODSConnectionFactory}
	 * @throws JargonException
	 */
	public IRODSConnectionFactory instance(
			final JargonProperties jargonProperties) throws JargonException {
		if (jargonProperties == null) {
			throw new IllegalArgumentException("null jargonProperties");
		}

		if (jargonProperties.getConnectionFactory().equals("tcp")) {
			return new IRODSTCPConnectionFactoryImpl();
		} else {
			throw new JargonException("unsupported connection factory type:"
					+ jargonProperties.getConnectionFactory());
		}
	}

}
