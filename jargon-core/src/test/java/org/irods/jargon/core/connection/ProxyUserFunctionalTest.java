package org.irods.jargon.core.connection;

import java.util.Properties;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProxyUserFunctionalTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	/*
	 * creates a file in a user home dir as rods admin via proxy function
	 */
	@Test
	public void testCreateUserHomeDirViaProxy() throws Exception {

		IRODSAccount userAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccount rodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(rodsAccount.getZone());
		sb.append("/home/");
		sb.append(userAccount.getUserName());
		sb.append("/testCreateUserHomeDirViaProxy");

		String testRoot = sb.toString();

		IRODSAccount proxyAccount = IRODSAccount.instanceWithProxy(rodsAccount.getHost(), rodsAccount.getPort(),
				userAccount.getUserName(), rodsAccount.getPassword(), "", rodsAccount.getZone(), "",
				rodsAccount.getUserName(), rodsAccount.getZone(), AuthScheme.STANDARD, null);

		IRODSFile projectRootFile = accessObjectFactory.getIRODSFileFactory(proxyAccount).instanceIRODSFile(testRoot);
		projectRootFile.deleteWithForceOption();
		projectRootFile.mkdirs();

		// now find the dir in place as the owning user
		IRODSFile actual = accessObjectFactory.getIRODSFileFactory(userAccount).instanceIRODSFile(testRoot);
		Assert.assertTrue("file not created via proxy", actual.exists());

	}

}
