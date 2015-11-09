/**
 *
 */
package org.irods.jargon.core.transfer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle a restart of a get operation
 *
 * @author Mike Conway - DICE
 *
 */
public class GetTransferRestartProcessor extends
AbstractTransferRestartProcessor {

	private static Logger log = LoggerFactory
			.getLogger(GetTransferRestartProcessor.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @param restartManager
	 */
	public GetTransferRestartProcessor(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractRestartManager restartManager,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) {
		super(irodsAccessObjectFactory, irodsAccount, restartManager,
				transferStatusCallbackListener, transferControlBlock);
	}

	@Override
	public void restartIfNecessary(final String irodsAbsolutePath)
			throws RestartFailedException, FileRestartManagementException {

		log.info("restartIfNecessary()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		FileRestartInfo fileRestartInfo = retrieveRestartIfConfiguredOrNull(
				irodsAbsolutePath, FileRestartInfo.RestartType.GET);

		if (fileRestartInfo == null) {
			log.info("no restart");
			return;
		}

		try {
			processRestart(irodsAbsolutePath, fileRestartInfo);
		} catch (JargonException e) {
			log.error("exception accessing restart manager", e);
			throw new FileRestartManagementException("restart manager error", e);
		}

	}

	private void processRestart(final String irodsAbsolutePath,
			final FileRestartInfo fileRestartInfo)
					throws RestartFailedException, FileRestartManagementException,
					JargonException {

		log.info("processRestart()");
		log.info("get local file as rw random access file...");
		RandomAccessFile localFile = null;
		try {
			localFile = localFileAsFileAndCheckExists(fileRestartInfo,
					OpenType.WRITE);
		} catch (FileNotFoundException e) {
			log.error("local file not found", e);
			throw new RestartFailedException("local file not found", e);
		}

		log.info("open iRODS file as read only random access...");

		IRODSRandomAccessFile irodsRandomAccessFile;
		try {
			irodsRandomAccessFile = getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSRandomAccessFile(irodsAbsolutePath,
							OpenFlags.READ);
			irodsRandomAccessFile.seek(0, SeekWhenceType.SEEK_START);
		} catch (NoResourceDefinedException e1) {
			log.error("no resource defined", e1);
			throw new RestartFailedException(
					"cannot get irodsRandomAccessFile", e1);
		} catch (JargonException e1) {
			log.error("general jargon error getting irods random file", e1);
			throw new RestartFailedException(
					"cannot get irodsRandomAccessFile", e1);
		} catch (IOException e) {
			log.error("io exception in initial seek of irods random file", e);
			throw new RestartFailedException(
					"cannot seek in irodsRandomAccessFile", e);
		}

		byte[] buffer;

		try {

			ConnectionProgressStatusListener intraFileStatusListener = null;
			if (getTransferStatusCallbackListener() != null
					&& getTransferControlBlock().getTransferOptions()
					.isIntraFileStatusCallbacks()) {
				intraFileStatusListener = DefaultIntraFileProgressCallbackListener
						.instanceSettingTransferOptions(TransferType.GET,
								localFile.length(), getTransferControlBlock(),
								getTransferStatusCallbackListener(),
								getTransferControlBlock().getTransferOptions());
			}

			// now put each segment
			buffer = new byte[getIrodsAccessObjectFactory()
			                  .getJargonProperties().getPutBufferSize()];
			long currentOffset = 0L;
			long gap;
			FileRestartDataSegment segment = null;
			for (int i = 0; i < fileRestartInfo.getFileRestartDataSegments()
					.size(); i++) {

				if (getTransferControlBlock().isCancelled()) {
					break;
				}

				segment = fileRestartInfo.getFileRestartDataSegments().get(i);
				log.info("process segment:{}", segment);
				gap = segment.getOffset() - currentOffset;
				if (gap < 0) {
					log.warn("my segment has a gap < 0..continuing:{}", segment);
				} else if (gap > 0) {

					// ok, have a gap > 0, let's get our restart on

					getSegment(gap, localFile, buffer, fileRestartInfo, i,
							irodsRandomAccessFile, intraFileStatusListener);
					currentOffset += gap;
				}

				if (segment.getLength() > 0) {
					currentOffset += segment.getLength();
					localFile.seek(currentOffset);
					irodsRandomAccessFile.seek(currentOffset,
							SeekWhenceType.SEEK_START);
				}
			}

			// put final segment based on file size
			gap = irodsRandomAccessFile.length() - currentOffset;
			if (gap > 0) {
				log.info("writing last segment based on file length");
				int i = fileRestartInfo.getFileRestartDataSegments().size() - 1;
				getSegment(gap, localFile, buffer, fileRestartInfo, i,
						irodsRandomAccessFile, intraFileStatusListener);
			}

			log.info("restart completed..remove from the cache");
			getRestartManager().deleteRestart(
					fileRestartInfo.identifierFromThisInfo());
			log.info("removed restart");
		} catch (FileNotFoundException e) {
			log.error("file not found exception with localFile:{}", localFile,
					e);
			throw new RestartFailedException(e);
		} catch (JargonException e) {
			log.error("general jargon error getting irods random file", e);
			throw new RestartFailedException("cannot get local file", e);
		} catch (IOException e) {
			log.error("end of file exception with localFile:{}", localFile, e);
			throw new RestartFailedException(e);
		} finally {

			try {
				irodsRandomAccessFile.close();
			} catch (IOException e) {
				log.error(
						"error closing irods random access file during restart",
						e);
				throw new RestartFailedException(
						"exception closing irods restart file", e);
			}
			try {
				localFile.close();
			} catch (IOException e) {
				log.warn("error closing local file, logged and ignored", e);
			}

		}

	}

	private void getSegment(final long gap, final RandomAccessFile localFile,
			final byte[] buffer, final FileRestartInfo fileRestartInfo,
			final int indexOfSegmentToUpdateLength,
			final IRODSRandomAccessFile irodsRandomAccessFile,
			final ConnectionProgressStatusListener intraFileStatusListener)
					throws RestartFailedException, FileRestartManagementException {

		long myGap = gap;
		long writtenSinceUpdated = 0L;
		long totalWrittenOverall = 0L;
		int toRead = 0;
		while (myGap > 0) {

			if (getTransferControlBlock().isCancelled()) {
				return;
			}

			if (myGap > buffer.length) {
				toRead = buffer.length;
			} else {
				toRead = (int) myGap;
			}

			log.info("reading buffer from input file...");
			int amountRead;
			try {
				amountRead = irodsRandomAccessFile.read(buffer, 0, toRead);

				if (amountRead <= 0) {
					log.error("read 0 or less from irodsFile:{}", amountRead);
					throw new RestartFailedException(
							"restart failed in read of irods file");
				}

				localFile.write(buffer, 0, amountRead);
				myGap -= amountRead;
				writtenSinceUpdated += amountRead;
				totalWrittenOverall += amountRead;

				if (writtenSinceUpdated >= AbstractTransferRestartProcessor.RESTART_FILE_UPDATE_SIZE) {
					log.info("need to update restart");
					getRestartManager().updateLengthForSegment(
							fileRestartInfo.identifierFromThisInfo(),
							indexOfSegmentToUpdateLength, writtenSinceUpdated);
					writtenSinceUpdated = 0;
				}

			} catch (IOException e) {
				log.error("IOException reading local file", e);
				throw new RestartFailedException(
						"IO Exception reading local file", e);
			}
		}

		if (myGap != 0) {
			log.error("final gap should be exactly zero, something is out of balance!");
			throw new RestartFailedException(
					"control balance error in putSeq, myGap should be zero at end");
		}

		if (totalWrittenOverall != gap) {
			log.error("total written does not equal the gap I originally had, something is out of balance!");
			throw new RestartFailedException(
					"control balance error in putSeq, total written does not equal gap");
		}

		if (log.isDebugEnabled()) {
			log.debug("verifying pointers at end");
			try {
				long irodsPointer = irodsRandomAccessFile.getFilePointer();
				long localPointer = localFile.getFilePointer();
				if (irodsPointer != localPointer) {
					log.error("pointers out of synch!");
					log.error("local:{}", localPointer);
					log.error("irods:{}", irodsPointer);
					throw new RestartFailedException(
							"pointers do not match after putseg!");
				}
				log.debug("pointers verified at:{} after putSeg", localPointer);
			} catch (IOException e) {
				log.error("exception obtaining current pointers for local and iRODS");
				throw new RestartFailedException(
						"unable to obtain current pointers for local and iRODS",
						e);
			}
		}

		/*
		 * Start file progress callbacks with what had been sent so far
		 */
		if (intraFileStatusListener != null) {
			ConnectionProgressStatus connectionProgressStatus = ConnectionProgressStatus
					.instanceForSend(fileRestartInfo.estimateLengthSoFar());
			intraFileStatusListener
			.connectionProgressStatusCallback(connectionProgressStatus);
		}

		if (writtenSinceUpdated > 0) {
			log.info("need to update restart");
			getRestartManager().updateLengthForSegment(
					fileRestartInfo.identifierFromThisInfo(),
					indexOfSegmentToUpdateLength, writtenSinceUpdated);
			writtenSinceUpdated = 0;
		}

	}
}
