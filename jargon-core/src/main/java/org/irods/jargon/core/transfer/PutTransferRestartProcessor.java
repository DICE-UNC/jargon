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
 * @author Mike Conway - DICE
 */
public class PutTransferRestartProcessor extends
		AbstractTransferRestartProcessor {

	private static Logger log = LoggerFactory
			.getLogger(PutTransferRestartProcessor.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @param restartManager
	 */
	public PutTransferRestartProcessor(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractRestartManager restartManager,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) {
		super(irodsAccessObjectFactory, irodsAccount, restartManager,
				transferStatusCallbackListener, transferControlBlock);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.AbstractTransferRestartProcessor#
	 * restartIfNecessary()
	 */
	@Override
	public void restartIfNecessary(final String irodsAbsolutePath)
			throws RestartFailedException, FileRestartManagementException,
			JargonException {
		log.info("restartIfNecessary()");

		FileRestartInfo fileRestartInfo = retrieveRestartIfConfiguredOrNull(
				irodsAbsolutePath, FileRestartInfo.RestartType.PUT);
		if (fileRestartInfo == null) {
			log.info("no restart");
			return;
		}

		processRestart(irodsAbsolutePath, fileRestartInfo);

	}

	/**
	 * Note that jargon exceptions are passed back so the restart may be
	 * retried, versus RestartFailedException and FileRestartManagerException
	 * where I don't want to try a restart again.
	 *
	 * @param irodsAbsolutePath
	 * @param fileRestartInfo
	 * @throws RestartFailedException
	 * @throws FileRestartManagementException
	 * @throws JargonException
	 */
	private void processRestart(final String irodsAbsolutePath,
			final FileRestartInfo fileRestartInfo)
			throws RestartFailedException, FileRestartManagementException,
			JargonException {

		/*
		 * If specified by options, and with a call-back listener registered,
		 * create an object to aggregate and channel within-file progress
		 * reports to the caller.
		 */

		IRODSRandomAccessFile irodsRandomAccessFile;
		try {
			irodsRandomAccessFile = getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSRandomAccessFile(irodsAbsolutePath,
							OpenFlags.READ_WRITE_CREATE_IF_NOT_EXISTS);
			irodsRandomAccessFile.seek(0, SeekWhenceType.SEEK_START);
		} catch (NoResourceDefinedException e1) {
			log.error("no resource defined", e1);
			throw new RestartFailedException(
					"cannot get irodsRandomAccessFile", e1);
		} catch (JargonException e1) {
			log.error("general jargon error getting irods random file", e1);
			throw e1;
		} catch (IOException e) {
			log.error("io exception getting irods random file", e);
			throw new JargonException("cannot get irodsRandomAccessFile", e);
		}

		RandomAccessFile localFile = null;
		byte[] buffer;
		/*
		 * See rcPortalOpr.cpp lfRestartPutWithInfo at about line 1522
		 */
		try {
			localFile = localFileAsFileAndCheckExists(fileRestartInfo,
					OpenType.READ);

			ConnectionProgressStatusListener intraFileStatusListener = null;
			if (getTransferStatusCallbackListener() != null
					&& getTransferControlBlock().getTransferOptions()
							.isIntraFileStatusCallbacks()) {
				intraFileStatusListener = DefaultIntraFileProgressCallbackListener
						.instanceSettingTransferOptions(TransferType.PUT,
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
				log.debug("gap:{}", gap);
				if (gap < 0) {
					log.warn("my segment has a gap < 0..continuing:{}", segment);
				} else if (gap > 0) {

					// ok, have a gap > 0, let's get our restart on

					putSegment(gap, localFile, buffer, fileRestartInfo, i - 1,
							irodsRandomAccessFile, intraFileStatusListener);
					currentOffset += gap;
				}

				if (segment.getLength() > 0) {
					currentOffset += segment.getLength();
					log.info("currentOffset after putSegment:{}", currentOffset);
					localFile.seek(currentOffset);
					irodsRandomAccessFile.seek(currentOffset,
							SeekWhenceType.SEEK_START);
				}

			}

			/*
			 * See rcPortalOpr.cpp at about line 1616
			 */
			log.info("computing gap for last segment");
			log.info("local file length:{}", localFile.length());
			log.info("current offset:{}", currentOffset);

			gap = localFile.length() - currentOffset;
			log.info("last segment gap:{}", gap);
			if (gap > 0) {
				log.info("writing last segment based on file length");
				int i = fileRestartInfo.getFileRestartDataSegments().size() - 1;

				putSegment(gap, localFile, buffer, fileRestartInfo, i,
						irodsRandomAccessFile, intraFileStatusListener);
			}

			log.info("restart completed..remove from the cache");// put final
			// segment
			// based on
			// file size

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
				if (localFile != null) {
					localFile.close();
				}
			} catch (IOException e) {
				log.warn("error closing local file, logged and ignored", e);
			}

		}

	}

	/**
	 * Put the segment to iRODS, and update the length of the given segment
	 *
	 * @param gap
	 * @param localFile
	 * @param buffer
	 * @param fileRestartInfo
	 * @param segment
	 * @param indexOfCurrentSegment
	 * @param lengthToUpdate
	 * @param irodsRandomAccessFile
	 * @param intraFileStatusListener
	 * @throws RestartFailedException
	 * @throws FileRestartManagementException
	 */
	private void putSegment(final long gap, final RandomAccessFile localFile,
			final byte[] buffer, final FileRestartInfo fileRestartInfo,
			final int indexOfSegmentToUpdateLength,
			final IRODSRandomAccessFile irodsRandomAccessFile,
			final ConnectionProgressStatusListener intraFileStatusListener)
			throws RestartFailedException, FileRestartManagementException {

		long myGap = gap;
		long writtenSinceUpdated = 0;
		int toRead = 0;
		long totalWrittenOverall = 0L;
		while (myGap > 0) {

			if (getTransferControlBlock().isCancelled()) {
				return;
			}

			if (myGap > buffer.length) {
				toRead = buffer.length;
			} else {
				toRead = (int) myGap;
			}

			log.debug("toRead:{}", toRead);

			log.info("reading buffer from input file...");
			int amountRead;
			try {
				amountRead = localFile.read(buffer, 0, toRead);
				log.debug("amountRead:{}", amountRead);

				if (amountRead <= 0) {
					log.error("read 0 or less from localFile:{}", amountRead);
					throw new RestartFailedException(
							"restart failed in read of local file");
				}

				irodsRandomAccessFile.write(buffer, 0, amountRead);
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
