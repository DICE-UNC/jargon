package org.irods.jargon.httpstream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.ByteCountingCallbackInputStreamWrapper;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service providing the ability to interact between iRODS and HTTP resources,
 * for streaming data into and out of iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class HttpStreamingServiceImpl implements HttpStreamingService {

	public static final Logger log = LoggerFactory
			.getLogger(HttpStreamingServiceImpl.class);
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private IRODSAccount irodsAccount;

	/**
	 * Default constructor takes the objects necessary to communicate with iRODS
	 * via Access Objects
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create various
	 *            access objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} with login information for the target
	 *            grid
	 * @throws JargonException
	 */
	public HttpStreamingServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.httpstream.HttpStreamingService#
	 * streamHttpUrlContentsToIRODSFile(java.lang.String,
	 * org.irods.jargon.core.pub.io.IRODSFile,
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener,
	 * org.irods.jargon.core.transfer.TransferControlBlock)
	 */
	@Override
	public String streamHttpUrlContentsToIRODSFile(
			final String sourceURL,
			final IRODSFile irodsTargetFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws JargonException, HttpStreamingException {


		log.info("streamHttpUrlContentsToIRODSFile()");

		if (sourceURL == null || sourceURL.isEmpty()) {
			throw new IllegalArgumentException("null sourceURL");
		}

		if (irodsTargetFile == null) {
			throw new IllegalArgumentException("irodsTargetFile is null");
		}

		log.info("sourceURL:{}", sourceURL);
		log.info("irodsTargetFile:{}", irodsTargetFile);

		TransferControlBlock operativeTransferControlBlock = transferControlBlock;
		if (operativeTransferControlBlock == null) {
			operativeTransferControlBlock = irodsAccessObjectFactory
					.buildDefaultTransferControlBlockBasedOnJargonProperties();
		}

		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(sourceURL);

		// Execute the request
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			log.error("client protocol exception ocurred in streaming", e);
			throw new HttpStreamingException(e);
		} catch (Exception e) {
			log.error("IOException ocurred in streaming", e);
			throw new HttpStreamingException(e);
		}

		StatusLine statusLine = response.getStatusLine();
		log.info("status from http operation:{}", statusLine);
		if (statusLine.getStatusCode() == 404) {
			throw new HttpStreamingException("404 not found for URL");
		} else if (statusLine.getStatusCode() > 200) {
			log.error("invalid status code:{}", statusLine.getStatusCode());
			throw new HttpStreamingException(
					"invalid status from HTTP operation:"
							+ statusLine.getStatusCode());
		}

		// Get hold of the response entity
		HttpEntity entity = response.getEntity();

		// If the response does not enclose an entity, there is no need
		// to worry about connection release
		if (entity == null) {
			log.error("no input stream available from URI");
			throw new HttpStreamingException(
					"no input stream available for URI");
		}

		InputStream instream = null;
		long urlSize = entity.getContentLength();

		log.debug("size of URL Stream:{}", urlSize);

		try {
			instream = new BufferedInputStream(entity.getContent());
		} catch (IOException e) {
			// In case of an IOException the connection will be released
			// back to the connection manager automatically
			log.error("IOException ocurred in streaming", e);
			throw new HttpStreamingException(e);
		} catch (RuntimeException e) {

			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection and release it back to the connection manager.
			httpget.abort();
			log.error("exception ocurred in streaming", e);
			throw new HttpStreamingException(e);
		} finally {

		}

		operativeTransferControlBlock.setTotalFilesToTransfer(1);

		/*
		 * Source URL is a file, target is either a collection, or specifies the
		 * file. If the target exists, or the target parent exists, format the
		 * appropriate call-back so that it depicts the resulting file
		 */

		StringBuilder targetIrodsPathBuilder = new StringBuilder();

		/*
		 * Reset the iRODS file, as the directory may have been created prior to
		 * the put operation. The reset clears the cache of the exists(),
		 * isFile(), and other basic file stat info
		 */
		irodsTargetFile.reset();
		if (irodsTargetFile.exists() && irodsTargetFile.isDirectory()) {
			log.info("target is a directory, source is an url");
			targetIrodsPathBuilder.append(irodsTargetFile.getAbsolutePath());
			targetIrodsPathBuilder.append("/");
			int slashIndex = sourceURL.lastIndexOf('/');
			String urlFileName = sourceURL.substring(slashIndex + 1);
			targetIrodsPathBuilder.append(urlFileName);
		} else if (irodsTargetFile.getParentFile().exists()
				&& irodsTargetFile.getParentFile().isDirectory()) {
			log.info("treating target as a file, using the whole path");
			targetIrodsPathBuilder.append(irodsTargetFile.getAbsolutePath());
		}

		String callbackTargetIrodsPath = targetIrodsPathBuilder.toString();
		log.info("computed callbackTargetIrodsPath:{}", callbackTargetIrodsPath);

		// send 0th file status callback that indicates startup
		if (transferStatusCallbackListener != null) {
			TransferStatus status = TransferStatus.instance(TransferType.PUT,
					sourceURL, callbackTargetIrodsPath, "",
					operativeTransferControlBlock.getTotalBytesToTransfer(),
					operativeTransferControlBlock
							.getTotalBytesTransferredSoFar(),
					operativeTransferControlBlock
							.getTotalFilesTransferredSoFar(),
					operativeTransferControlBlock.getTotalFilesToTransfer(),
					TransferState.OVERALL_INITIATION, irodsAccount.getHost(),
					irodsAccount.getZone());
			transferStatusCallbackListener.overallStatusCallback(status);
		}

		IRODSFile callbackTargetIrodsFile = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						callbackTargetIrodsPath);
		callbackTargetIrodsFile.setResource(irodsTargetFile.getResource());

		log.info("callbackTargetIrodsFile:{}", callbackTargetIrodsFile);

		if (transferStatusCallbackListener != null) {

			TransferStatus status = TransferStatus.instance(TransferType.PUT,
					sourceURL, callbackTargetIrodsFile.getAbsolutePath(),
					irodsTargetFile.getResource(), urlSize, urlSize, 0, 1,
					TransferState.IN_PROGRESS_START_FILE,
					irodsAccount.getHost(), irodsAccount.getZone());

			transferStatusCallbackListener.statusCallback(status);
		}

		if (transferStatusCallbackListener != null) {
			log.info("setting up a callback listener for within stream progress");
			ConnectionProgressStatusListener listener = DefaultIntraFileProgressCallbackListener
					.instance(TransferType.PUT, urlSize, transferControlBlock,
							transferStatusCallbackListener);
			instream = new ByteCountingCallbackInputStreamWrapper(listener,
					instream);
		}

		try {
			log.debug("getting stream2streamAO");
			Stream2StreamAO stream2StreamAO = irodsAccessObjectFactory
					.getStream2StreamAO(irodsAccount);
			stream2StreamAO.transferStreamToFileUsingIOStreams(instream,
					(File) callbackTargetIrodsFile, urlSize, 0);

			if (transferStatusCallbackListener != null) {

				TransferStatus status = TransferStatus.instance(
						TransferType.PUT, sourceURL,
						callbackTargetIrodsFile.getAbsolutePath(),
						irodsTargetFile.getResource(), urlSize, urlSize, 1, 1,
						TransferState.IN_PROGRESS_COMPLETE_FILE,
						irodsAccount.getHost(), irodsAccount.getZone());

				transferStatusCallbackListener.statusCallback(status);
			}

		} catch (Exception je) {
			// may rethrow or send back to the callback listener
			log.error("exception in transfer", je);

			int totalFiles = 0;
			int totalFilesSoFar = 0;

			operativeTransferControlBlock.reportErrorInTransfer();
			totalFiles = operativeTransferControlBlock
					.getTotalFilesToTransfer();
			totalFilesSoFar = operativeTransferControlBlock
					.getTotalFilesTransferredSoFar();

			if (transferStatusCallbackListener != null) {
				log.error("exception will be passed back to existing callback listener");

				TransferStatus status = TransferStatus.instanceForException(
						TransferType.PUT, sourceURL,
						callbackTargetIrodsFile.getAbsolutePath(),
						callbackTargetIrodsFile.getResource(), urlSize,
						irodsTargetFile.length(), totalFilesSoFar, totalFiles,
						je, irodsAccount.getHost(), irodsAccount.getZone());

				transferStatusCallbackListener.statusCallback(status);

			} else {
				log.error("exception will be re-thrown, as there is no status callback listener");
				throw new JargonException(
						"exception thrown in transfer process, no callback listener supplied",
						je);

			}
		} finally {
			// Closing the input stream will trigger connection release
			try {
				instream.close();
			} catch (IOException e) {
				log.error("IOException in close of HTTP input stream, logged and igonored");
			}
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		log.info("contents streamed to:{}",
				callbackTargetIrodsFile.getAbsolutePath());
		return callbackTargetIrodsFile.getAbsolutePath();

	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}
