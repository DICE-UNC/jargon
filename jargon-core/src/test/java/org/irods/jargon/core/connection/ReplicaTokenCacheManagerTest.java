package org.irods.jargon.core.connection;

import java.util.concurrent.locks.Lock;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReplicaTokenCacheManagerTest {

	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		irodsFileSystem = IRODSFileSystem.instance();
	}

	@Test
	public void testGetTokenCycle() throws Exception {

		irodsFileSystem.getIrodsSession();
		ReplicaTokenCacheManager manager = IRODSSession.replicaTokenCacheManager;

		String path1 = "boo";
		String path2 = "foo";
		String path3 = "bar";

		String userName = "foo";

		Lock lock1 = manager.obtainReplicaTokenLock(path1, userName);
		try {
			lock1.tryLock();
			manager.addReplicaToken(path1, userName, "bar", 1);
		} finally {
			lock1.unlock();
		}

		lock1 = manager.obtainReplicaTokenLock(path1, userName);
		try {
			lock1.tryLock();
			ReplicaTokenCacheEntry entry = manager.claimExistingReplicaToken(path1, userName);
			Assert.assertEquals(path1, entry.getReplicaToken());
		} finally {
			lock1.unlock();
		}

		lock1 = manager.obtainReplicaTokenLock(path1, userName);
		try {
			lock1.tryLock();
			ReplicaTokenCacheEntry entry = manager.claimExistingReplicaToken(path1, userName);
			Assert.assertEquals(path1, entry.getReplicaToken());
		} finally {
			lock1.unlock();
		}

		lock1 = manager.obtainReplicaTokenLock(path1, userName);
		try {
			lock1.tryLock();
			manager.closeReplicaToken(path1, userName);
		} finally {
			lock1.unlock();
		}

		lock1 = manager.obtainReplicaTokenLock(path1, userName);
		try {
			lock1.tryLock();
			manager.closeReplicaToken(path1, userName);
		} finally {
			lock1.unlock();
		}

		lock1 = manager.obtainReplicaTokenLock(path1, userName);
		try {
			lock1.tryLock();
			boolean last = manager.closeReplicaToken(path1, userName);
			Assert.assertTrue(last);
		} finally {
			lock1.unlock();
		}

	}

}
