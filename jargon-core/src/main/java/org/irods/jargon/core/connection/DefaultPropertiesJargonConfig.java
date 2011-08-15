/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.core.exception.JargonException;

/**
 * Reference class that parses the default Jargon properties (jargon.properties)
 * and provides easy methods to obtain configuration information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultPropertiesJargonConfig implements JargonProperties {

	private final Properties jargonProperties;

	@Override
	public int getMaxFilesAndDirsQueryMax() throws JargonException {
		return verifyPropExistsAndGetAsInt("max.files.and.dirs.query.max");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getParallelThreadsLengthThreshold()
	 */
	@Override
	public long getParallelThreadsLengthThreshold() throws JargonException {
		return verifyPropExistsAndGetAsLong("transfer.send.max.threads.threshold");
	}

	/**
	 * Default constructor will load the default properties from the
	 * 'jargon.properties' file on the classpath.
	 * 
	 * @throws JargonException
	 *             if properties cannot be loaded.
	 */
	public DefaultPropertiesJargonConfig() throws JargonException {

		ClassLoader loader = this.getClass().getClassLoader();
		InputStream in = loader.getResourceAsStream("jargon.properties");
		jargonProperties = new Properties();

		try {
			jargonProperties.load(in);
		} catch (IOException ioe) {
			throw new JargonException("error loading jargon.properties", ioe);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#isUseParallelTransfer()
	 */
	@Override
	public boolean isUseParallelTransfer() throws JargonException {
		String propVal = verifyPropExistsAndGetAsString("transfer.use.parallel");
		return Boolean.valueOf(propVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperites#getMaxParallelThreads()
	 */
	@Override
	public int getMaxParallelThreads() throws JargonException {
		return verifyPropExistsAndGetAsInt("transfer.max.parallel.threads");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#isUseTransferThreadsPool
	 * ()
	 */
	@Override
	public boolean isUseTransferThreadsPool() throws JargonException {
		return verifyPropExistsAndGetAsBoolean("transfer.use.pool");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadCorePoolSize ()
	 */
	@Override
	public int getTransferThreadCorePoolSize() throws JargonException {
		return verifyPropExistsAndGetAsInt("transfer.executor.pool.core.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadMaxPoolSize ()
	 */
	@Override
	public int getTransferThreadMaxPoolSize() throws JargonException {
		return verifyPropExistsAndGetAsInt("transfer.executor.pool.max.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadPoolTimeoutMillis()
	 */
	@Override
	public int getTransferThreadPoolTimeoutMillis() throws JargonException {
		return verifyPropExistsAndGetAsInt("transfer.executor.pool.timeout");
	}

	private String verifyPropExistsAndGetAsString(final String propKey)
			throws JargonException {
		String propVal = ((String) jargonProperties.get(propKey)).trim();
		if (propVal == null) {
			throw new JargonException(propKey + " not set in jargon.properties");
		}
		return propVal;
	}

	/**
	 * @param propKey
	 * @return
	 * @throws JargonException
	 */
	private int verifyPropExistsAndGetAsInt(final String propKey)
			throws JargonException {

		String propVal = verifyPropExistsAndGetAsString(propKey);

		try {
			return Integer.parseInt(propVal);
		} catch (NumberFormatException nfe) {
			throw new JargonException("prop " + propKey
					+ "did not result in an int value, was:" + propVal);
		}

	}

	private boolean verifyPropExistsAndGetAsBoolean(final String propKey)
			throws JargonException {

		String propVal = verifyPropExistsAndGetAsString(propKey);
		return Boolean.parseBoolean(propVal);

	}

	private long verifyPropExistsAndGetAsLong(final String propKey)
			throws JargonException {

		String propVal = verifyPropExistsAndGetAsString(propKey);

		try {
			return Long.parseLong(propVal);
		} catch (NumberFormatException nfe) {
			throw new JargonException("prop " + propKey
					+ "did not result in a long value, was:" + propVal);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isAllowPutGetResourceRedirects()
	 */
	@Override
	public boolean isAllowPutGetResourceRedirects() throws JargonException {
		String propVal = verifyPropExistsAndGetAsString("transfer.allow.redirects");
		return Boolean.valueOf(propVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isComputeChecksumAfterTransfer()
	 */
	@Override
	public boolean isComputeChecksumAfterTransfer() throws JargonException {
		String propVal = verifyPropExistsAndGetAsString("transfer.compute.checksum");
		return Boolean.valueOf(propVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isComputeAndVerifyChecksumAfterTransfer()
	 */
	@Override
	public boolean isComputeAndVerifyChecksumAfterTransfer()
			throws JargonException {
		String propVal = verifyPropExistsAndGetAsString("transfer.computeandvalidate.checksum");
		return Boolean.valueOf(propVal);
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.connection.JargonProperties#isIntraFileStatusCallbacks()
	 */
	@Override
	public boolean isIntraFileStatusCallbacks() throws JargonException {
		String propVal = verifyPropExistsAndGetAsString("transfer.intra.file.callbacks");
		return Boolean.valueOf(propVal);
	}

}
