/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.utils.PropertyUtils;

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
	public int getMaxFilesAndDirsQueryMax() {
		return verifyPropExistsAndGetAsInt("max.files.and.dirs.query.max");
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
	public boolean isUseParallelTransfer() {
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
	public int getMaxParallelThreads() {
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
	public boolean isUseTransferThreadsPool() {
		return verifyPropExistsAndGetAsBoolean("transfer.use.pool");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadPoolTimeoutMillis()
	 */
	@Override
	public int getTransferThreadPoolTimeoutMillis() {
		return verifyPropExistsAndGetAsInt("transfer.executor.pool.timeout");
	}

	private String verifyPropExistsAndGetAsString(final String propKey) {
		return PropertyUtils.verifyPropExistsAndGetAsString(jargonProperties,
				propKey);
	}

	/**
	 * @param propKey
	 * @return
	 * @throws JargonException
	 */
	private int verifyPropExistsAndGetAsInt(final String propKey) {
		return PropertyUtils.verifyPropExistsAndGetAsInt(jargonProperties,
				propKey);

	}

	private boolean verifyPropExistsAndGetAsBoolean(final String propKey) {
		return PropertyUtils.verifyPropExistsAndGetAsBoolean(jargonProperties,
				propKey);
	}

	private long verifyPropExistsAndGetAsLong(final String propKey) {
		return PropertyUtils.verifyPropExistsAndGetAsLong(jargonProperties,
				propKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isAllowPutGetResourceRedirects()
	 */
	@Override
	public boolean isAllowPutGetResourceRedirects() {
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
	public boolean isComputeChecksumAfterTransfer() {
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
	public boolean isComputeAndVerifyChecksumAfterTransfer() {
		String propVal = verifyPropExistsAndGetAsString("transfer.computeandvalidate.checksum");
		return Boolean.valueOf(propVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#isIntraFileStatusCallbacks
	 * ()
	 */
	@Override
	public boolean isIntraFileStatusCallbacks() {
		String propVal = verifyPropExistsAndGetAsString("transfer.intra.file.callbacks");
		return Boolean.valueOf(propVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getIRODSSocketTimeout()
	 */
	@Override
	public int getIRODSSocketTimeout() {
		return verifyPropExistsAndGetAsInt("socket.timeout");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getIRODSParallelTransferSocketTimeout()
	 */
	@Override
	public int getIRODSParallelTransferSocketTimeout() {
		return verifyPropExistsAndGetAsInt("parallel.socket.timeout");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getTransferThreadPoolMaxSimultaneousTransfers()
	 */
	@Override
	public int getTransferThreadPoolMaxSimultaneousTransfers() {
		return verifyPropExistsAndGetAsInt("transfer.executor.pool.max.simultaneous.transfers");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getInternalInputStreamBufferSize()
	 */
	@Override
	public int getInternalInputStreamBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.internal.input.stream.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getInternalOutputStreamBufferSize()
	 */
	@Override
	public int getInternalOutputStreamBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.internal.output.stream.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getInternalCacheBufferSize
	 * ()
	 */
	@Override
	public int getInternalCacheBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.internal.cache.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getSendInputStreamBufferSize()
	 */
	@Override
	public int getSendInputStreamBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.send.input.stream.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getInputToOutputCopyBufferByteSize()
	 */
	@Override
	public int getInputToOutputCopyBufferByteSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.input.to.output.copy.byte.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getLocalFileOutputStreamBufferSize()
	 */
	@Override
	public int getLocalFileOutputStreamBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.local.output.stream.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getPutBufferSize()
	 */
	@Override
	public int getPutBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.put.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getGetBufferSize()
	 */
	@Override
	public int getGetBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.get.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return verifyPropExistsAndGetAsString("encoding");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * getLocalFileInputStreamBufferSize()
	 */
	@Override
	public int getLocalFileInputStreamBufferSize() {
		return verifyPropExistsAndGetAsInt("jargon.io.local.input.stream.buffer.size");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUseNIOForParallelTransfers()
	 */
	@Override
	public boolean isUseNIOForParallelTransfers() {
		return verifyPropExistsAndGetAsBoolean("transfer.use.nio.for.parallel");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#isReconnect()
	 */
	@Override
	public boolean isReconnect() {
		return verifyPropExistsAndGetAsBoolean("jargon.reconnect");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#isInstrument()
	 */
	@Override
	public boolean isInstrument() {
		return verifyPropExistsAndGetAsBoolean("jargon.instrument");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isDefaultToPublicIfNothingUnderRootWhenListing()
	 */
	@Override
	public boolean isDefaultToPublicIfNothingUnderRootWhenListing() {
		return verifyPropExistsAndGetAsBoolean("default.to.public.if.nothing.under.root.when.listing");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getReconnectTimeInMillis
	 * ()
	 */
	@Override
	public long getReconnectTimeInMillis() {
		return verifyPropExistsAndGetAsLong("jargon.reconnect.time.in.millis");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUsingDiscoveredServerPropertiesCache()
	 */
	@Override
	public boolean isUsingDiscoveredServerPropertiesCache() {
		return verifyPropExistsAndGetAsBoolean("use.discovered.server.properties.cache");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUsingSpecificQueryForCollectionListingsWithPermissions()
	 */
	@Override
	public boolean isUsingSpecificQueryForCollectionListingsWithPermissions() {
		return verifyPropExistsAndGetAsBoolean("use.specific.query.for.collection.listings");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#
	 * isUsingSpecQueryForDataObjPermissionsForUserInGroup()
	 */
	@Override
	public boolean isUsingSpecQueryForDataObjPermissionsForUserInGroup() {
		return verifyPropExistsAndGetAsBoolean("use.specquery.for.dataobj.permissions.for.user.in.group");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#getPAMTimeToLive()
	 */
	@Override
	public int getPAMTimeToLive() {
		return verifyPropExistsAndGetAsInt("pam.time.to.live.in.seconds");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.JargonProperties#isForcePamFlush()
	 */
	@Override
	public boolean isForcePamFlush() {
		return verifyPropExistsAndGetAsBoolean("force.pam.flush");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getConnectionFactory()
	 */
	@Override
	public String getConnectionFactory() {
		String propVal = ((String) jargonProperties.get("connection.factory"));
		if (propVal == null) {
			propVal = "tcp";
		}
		return propVal;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.JargonProperties#getChecksumEncoding()
	 */
	@Override
	public ChecksumEncodingEnum getChecksumEncoding() {
		String propVal = ((String) jargonProperties
				.get("transfer.checksum.algorithm"));

		if (propVal == null || propVal.isEmpty()) {
			return ChecksumEncodingEnum.DEFAULT;
		} else {
			return ChecksumEncodingEnum.findTypeByString(propVal);
		}

	}

}
