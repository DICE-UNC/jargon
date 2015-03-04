/**
 * 
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.transfer.FileRestartInfo.RestartType;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mike Conway - DICE
 *
 */
public class MemoryBasedTransferRestartManagerTest {

	@Test
	public void testStore() throws Exception {
		MemoryBasedTransferRestartManager manager = new MemoryBasedTransferRestartManager();
		String irodsPath = "/irods/path";
		String localPath = "/local/path";
		RestartType restartType = RestartType.GET;
		IRODSAccount account = TestingPropertiesHelper.buildBogusIrodsAccount();
		FileRestartInfo fileRestartInfo = new FileRestartInfo();
		fileRestartInfo.setIrodsAbsolutePath(irodsPath);
		fileRestartInfo.setLocalAbsolutePath(localPath);
		fileRestartInfo.setRestartType(restartType);
		fileRestartInfo.setIrodsAccountIdentifier(account.toString());
		FileRestartInfoIdentifier actual = manager
				.storeRestart(fileRestartInfo);
		Assert.assertNotNull("null identifier", actual);
		Assert.assertEquals(irodsPath, actual.getAbsolutePath());
		Assert.assertEquals(restartType, actual.getRestartType());
		Assert.assertEquals(account.toString(),
				actual.getIrodsAccountIdentifier());

		FileRestartInfo retrievedInfo = manager.retrieveRestart(actual);
		Assert.assertNotNull("did not get info back from key", retrievedInfo);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveViaNull() throws Exception {
		MemoryBasedTransferRestartManager manager = new MemoryBasedTransferRestartManager();
		manager.retrieveRestart(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreNull() throws Exception {
		MemoryBasedTransferRestartManager manager = new MemoryBasedTransferRestartManager();
		manager.storeRestart(null);
	}

	@Test
	public void testDelete() throws Exception {
		MemoryBasedTransferRestartManager manager = new MemoryBasedTransferRestartManager();
		String irodsPath = "/irods/path";
		String localPath = "/local/path";
		RestartType restartType = RestartType.GET;
		IRODSAccount account = TestingPropertiesHelper.buildBogusIrodsAccount();
		FileRestartInfo fileRestartInfo = new FileRestartInfo();
		fileRestartInfo.setIrodsAbsolutePath(irodsPath);
		fileRestartInfo.setLocalAbsolutePath(localPath);
		fileRestartInfo.setRestartType(restartType);
		fileRestartInfo.setIrodsAccountIdentifier(account.toString());
		FileRestartInfoIdentifier actual = manager
				.storeRestart(fileRestartInfo);
		manager.deleteRestart(actual);

		FileRestartInfo retrievedInfo = manager.retrieveRestart(actual);
		Assert.assertNull("did not remove info", retrievedInfo);

	}

}
