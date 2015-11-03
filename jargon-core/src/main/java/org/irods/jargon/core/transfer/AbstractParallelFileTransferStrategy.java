package org.irods.jargon.core.transfer;

import java.io.File;

import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.ParallelEncryptionCipherWrapper.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for a parallel transfer controller. This will process
 * parallel transfers from iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractParallelFileTransferStrategy {

	public static final Logger log = LoggerFactory
			.getLogger(AbstractParallelFileTransferStrategy.class);

	public enum TransferType {
		GET_TRANSFER, PUT_TRANSFER
	}

	public abstract void transfer() throws JargonException;

	protected final String host;
	protected final int port;
	protected final int numberOfThreads;
	protected final int password;
	protected final File localFile;
	protected final long transferLength;
	private final PipelineConfiguration pipelineConfiguration;
	private final FileRestartInfo fileRestartInfo;

	/**
	 * Negotiated encryption configuration for transport security, and any other
	 * future determined aspects of
	 */
	private NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

	public PipelineConfiguration getPipelineConfiguration() {
		return pipelineConfiguration;
	}

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final TransferControlBlock transferControlBlock;
	private final TransferStatusCallbackListener transferStatusCallbackListener;
	private ConnectionProgressStatusListener connectionProgressStatusListener = null;
	private final int parallelSocketTimeoutInSecs;
	private final JargonProperties jargonProperties;

	/**
	 * Constructor for a parallel file transfer runner. This runner will create
	 * the parallel transfer threads and process the transfer.
	 * 
	 * @param host
	 *            <code>String</code> with the name of the host for the transfer
	 * @param port
	 *            <code>int</code> with the port for the transfer
	 * @param numberOfThreads
	 *            <code>int</code> with the number of threads to spawn, which is
	 *            set by iRODS.
	 * @param password
	 *            <code>int</code> with the one-time transfer token set by
	 *            iRODS.
	 * @param localFile
	 *            <code>File</code> that will transferrred.
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} for the session.
	 * @param transferLength
	 *            <code>long</code> with the total length of the file to
	 *            transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that controls and keeps track of
	 *            the transfer operation, required.
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} or <code>null</code> if
	 *            not desired. This can receive call-backs on the status of the
	 *            parallel transfer operation.
	 * @param fileRestartInfo
	 *            {@link FileRestartinfo} or <code>null</code> if not supporting
	 *            a restart of this transfer
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration} represents the
	 *            result of client server negotiation of any potential
	 *            encryption.
	 * 
	 * @throws JargonException
	 */

	protected AbstractParallelFileTransferStrategy(
			final String host,
			final int port,
			final int numberOfThreads,
			final int password,
			final File localFile,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final long transferLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final FileRestartInfo fileRestartInfo,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
			throws JargonException {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("host is null or empty");
		}

		if (port < 1) {
			throw new IllegalArgumentException("port must be supplied");
		}

		if (numberOfThreads == 0) {
			throw new IllegalArgumentException(
					"this is not a parallel transfer, the number of threads supplied is zero");
		}

		if (password <= 0) {
			throw new IllegalArgumentException("password is invalid");
		}

		if (localFile == null) {
			throw new IllegalArgumentException("Local file is null");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException(
					"irodsAccessObjectFactory is null");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		if (negotiatedClientServerConfiguration == null) {
			throw new IllegalArgumentException(
					"null negotiatedClientServerConfiguration");
		}

		this.host = host;
		this.port = port;
		this.numberOfThreads = numberOfThreads;
		this.password = password;
		this.localFile = localFile;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.transferControlBlock = transferControlBlock;
		this.transferStatusCallbackListener = transferStatusCallbackListener;
		this.transferLength = transferLength;
		/*
		 * Make a clone of the jargon props to avoid synchronization
		 */
		jargonProperties = new SettableJargonProperties(
				irodsAccessObjectFactory.getIrodsSession()
						.getJargonProperties());
		pipelineConfiguration = irodsAccessObjectFactory.getIrodsSession()
				.buildPipelineConfigurationBasedOnJargonProperties();
		this.fileRestartInfo = fileRestartInfo;

		parallelSocketTimeoutInSecs = jargonProperties
				.getIRODSParallelTransferSocketTimeout();
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractParallelFileTransferStrategy [");
		if (host != null) {
			builder.append("host=");
			builder.append(host);
			builder.append(", ");
		}
		builder.append("port=");
		builder.append(port);
		builder.append(", numberOfThreads=");
		builder.append(numberOfThreads);
		builder.append(", password=");
		builder.append(password);
		builder.append(", ");
		if (localFile != null) {
			builder.append("localFile=");
			builder.append(localFile);
			builder.append(", ");
		}
		builder.append("transferLength=");
		builder.append(transferLength);
		builder.append(", ");
		if (pipelineConfiguration != null) {
			builder.append("pipelineConfiguration=");
			builder.append(pipelineConfiguration);
			builder.append(", ");
		}
		if (fileRestartInfo != null) {
			builder.append("fileRestartInfo=");
			builder.append(fileRestartInfo);
			builder.append(", ");
		}
		if (negotiatedClientServerConfiguration != null) {
			builder.append("negotiatedClientServerConfiguration=");
			builder.append(negotiatedClientServerConfiguration);
			builder.append(", ");
		}
		if (irodsAccessObjectFactory != null) {
			builder.append("irodsAccessObjectFactory=");
			builder.append(irodsAccessObjectFactory);
			builder.append(", ");
		}
		if (transferControlBlock != null) {
			builder.append("transferControlBlock=");
			builder.append(transferControlBlock);
			builder.append(", ");
		}
		if (transferStatusCallbackListener != null) {
			builder.append("transferStatusCallbackListener=");
			builder.append(transferStatusCallbackListener);
			builder.append(", ");
		}
		if (connectionProgressStatusListener != null) {
			builder.append("connectionProgressStatusListener=");
			builder.append(connectionProgressStatusListener);
			builder.append(", ");
		}
		builder.append("parallelSocketTimeoutInSecs=");
		builder.append(parallelSocketTimeoutInSecs);
		builder.append(", ");
		if (jargonProperties != null) {
			builder.append("jargonProperties=");
			builder.append(jargonProperties);
		}
		builder.append("]");
		return builder.toString();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public int getPassword() {
		return password;
	}

	public File getLocalFile() {
		return localFile;
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @return the transferControlBlock
	 */
	protected TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @return the transferStatusCallbackListener
	 */
	protected TransferStatusCallbackListener getTransferStatusCallbackListener() {
		return transferStatusCallbackListener;
	}

	/**
	 * @return the connectionProgressStatusListener
	 */
	protected synchronized ConnectionProgressStatusListener getConnectionProgressStatusListener() {
		return connectionProgressStatusListener;
	}

	/**
	 * @param connectionProgressStatusListener
	 *            the connectionProgressStatusListener to set
	 */
	protected synchronized void setConnectionProgressStatusListener(
			final ConnectionProgressStatusListener connectionProgressStatusListener) {
		this.connectionProgressStatusListener = connectionProgressStatusListener;
	}

	/**
	 * @return the transferLength
	 */
	protected long getTransferLength() {
		return transferLength;
	}

	/**
	 * @return the parallelSocketTimeoutInSecs
	 */
	protected int getParallelSocketTimeoutInSecs() {
		return parallelSocketTimeoutInSecs;
	}

	/**
	 * @return the jargonProperties
	 */
	protected JargonProperties getJargonProperties() {
		return jargonProperties;
	}

	public FileRestartInfo getFileRestartInfo() {
		return fileRestartInfo;
	}

	/**
	 * Retrieves a reference to the restart manager, if configured. It may be
	 * <code>null</code>
	 * 
	 * @return {@link AbstractRestartManager}
	 * 
	 */
	public AbstractRestartManager getRestartManager() {
		return getIrodsAccessObjectFactory().getIrodsSession()
				.getRestartManager();
	}

	/**
	 * Handy method for threads to determine whether encryption should be done
	 * 
	 * @return
	 */
	boolean doEncryption() {
		return this.negotiatedClientServerConfiguration.isSslConnection();
	}

	/**
	 * Provides individual threads a hook to create the appropriate encryption
	 * cipher if needed.
	 * 
	 * @return {@link ParallelEncryptionCipherWrapper}
	 * @throws ClientServerNegotiationException
	 */
	ParallelEncryptionCipherWrapper initializeCypherForEncryption()
			throws ClientServerNegotiationException {
		log.debug("initializeCypherForEncryption()");
		if (!this.negotiatedClientServerConfiguration.isSslConnection()) {
			log.error("should not be trying to encrypt, is not ssl configured");
			throw new ClientServerNegotiationException(
					"attempt to encrypt a transfer when SSL not configured");
		}

		return EncryptionWrapperFactory.instance(pipelineConfiguration,
				negotiatedClientServerConfiguration, Mode.ENCRYPT);

	}

}