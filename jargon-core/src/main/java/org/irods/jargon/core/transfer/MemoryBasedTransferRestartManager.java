/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple restart manager that exists in an in-memory map. This version is
 * fairly naive and doesn't really support concurrent restarts.
 * <p/>
 * TODO: Perhaps some sort of locking mechanism is needed?
 * 
 * @author Mike Conway - DICE
 *
 */
public class MemoryBasedTransferRestartManager extends AbstractRestartManager {

	private static final Logger log = LoggerFactory
			.getLogger(MemoryBasedTransferRestartManager.class);

	private ConcurrentHashMap<FileRestartInfoIdentifier, FileRestartInfo> cacheOfRestartInfo = new ConcurrentHashMap<FileRestartInfoIdentifier, FileRestartInfo>(
			8, 0.9f, 1);

	public MemoryBasedTransferRestartManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.AbstractRestartManager#storeRestart(org
	 * .irods.jargon.core.transfer.FileRestartInfo)
	 */
	@Override
	public FileRestartInfoIdentifier storeRestart(
			FileRestartInfo fileRestartInfo)
			throws FileRestartManagementException {

		log.info("storeRestart()");
		if (fileRestartInfo == null) {
			throw new IllegalArgumentException("null fileRestartInfo");
		}

		FileRestartInfoIdentifier identifier = FileRestartInfoIdentifier
				.instanceFromFileRestartInfo(fileRestartInfo);
		cacheOfRestartInfo.put(identifier, fileRestartInfo);
		return identifier;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.AbstractRestartManager#deleteRestart(org
	 * .irods.jargon.core.transfer.FileRestartInfoIdentifier)
	 */
	@Override
	public void deleteRestart(
			FileRestartInfoIdentifier fileRestartInfoIdentifier)
			throws FileRestartManagementException {

		log.info("deleteRestart()");
		if (fileRestartInfoIdentifier == null) {
			throw new IllegalArgumentException("null fileRestartInfoIdentifier");
		}

		cacheOfRestartInfo.remove(fileRestartInfoIdentifier);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.AbstractRestartManager#retrieveRestart
	 * (org.irods.jargon.core.transfer.FileRestartInfoIdentifier)
	 */
	@Override
	public FileRestartInfo retrieveRestart(
			FileRestartInfoIdentifier fileRestartInfoIdentifier)
			throws FileRestartManagementException {

		log.info("retrieveRestart()");
		if (fileRestartInfoIdentifier == null) {
			throw new IllegalArgumentException("null fileRestartInfoIdentifier");
		}

		return cacheOfRestartInfo.get(fileRestartInfoIdentifier);

	}
}
