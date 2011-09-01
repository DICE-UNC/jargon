package org.irods.jargon.core.connection;

import java.util.Properties;
import java.util.concurrent.Executor;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSSessionTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

	}

	@Test
	public final void testCloseSessionTwice() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();
		irodsSession.closeSession();

	}

	@Test
	public void testGetDefaultJargonProperties() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();
		JargonProperties jargonProperties = irodsSession.getJargonProperties();
		Assert.assertNotNull("null jargon properties", jargonProperties);

	}

	@Test
	public void testOverrideDefaultJargonProperties() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setMaxParallelThreads(8000);
		irodsSession.setJargonProperties(overrideJargonProperties);
		JargonProperties jargonProperties = irodsSession.getJargonProperties();
		Assert.assertEquals("did not get the preset number of threads", 8000,
				jargonProperties.getMaxParallelThreads());

	}

	@Test
	public void testBuildTransferThreadPool() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setMaxParallelThreads(8000);
		overrideJargonProperties.setUseTransferThreadsPool(true);
		overrideJargonProperties.setTransferThreadPoolMaxSimultaneousTransfers(4);
		overrideJargonProperties.setTransferThreadPoolTimeoutMillis(60000);
		irodsSession.setJargonProperties(overrideJargonProperties);
		Executor executor = irodsSession.getParallelTransferThreadPool();
		Assert.assertNotNull("executor was null", executor);

	}

	@Test
	public void testBuildTransferThreadPoolAndGetTwice() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setMaxParallelThreads(8000);
		overrideJargonProperties.setUseTransferThreadsPool(true);
		overrideJargonProperties.setTransferThreadPoolMaxSimultaneousTransfers(4);
		overrideJargonProperties.setTransferThreadPoolTimeoutMillis(60000);
		irodsSession.setJargonProperties(overrideJargonProperties);
		Executor executor = irodsSession.getParallelTransferThreadPool();
		executor = irodsSession.getParallelTransferThreadPool();
		Assert.assertNotNull("executor was null", executor);

	}

	@Test
	public void testBuildTransferThreadPoolNotInProps() throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setMaxParallelThreads(8000);
		overrideJargonProperties.setUseTransferThreadsPool(false);
		overrideJargonProperties.setTransferThreadPoolMaxSimultaneousTransfers(4);
		overrideJargonProperties.setTransferThreadPoolTimeoutMillis(60000);
		irodsSession.setJargonProperties(overrideJargonProperties);
		Executor executor = irodsSession.getParallelTransferThreadPool();
		Assert.assertNull("executor should be  null", executor);

	}

	@Test
	public void testGetTransferOptionsWithComputeAndVerifyChecksumValTrue()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setComputeAndVerifyChecksumAfterTransfer(true);

		irodsSession.setJargonProperties(overrideJargonProperties);
		TransferOptions transferOptions = irodsSession
				.buildTransferOptionsBasedOnJargonProperties();

		Assert.assertEquals("did not set compute and verify checksum", true,
				transferOptions.isComputeAndVerifyChecksumAfterTransfer());

	}

	@Test
	public void testGetTransferOptionsWithComputeChecksumValTrue()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setComputeChecksumAfterTransfer(true);

		irodsSession.setJargonProperties(overrideJargonProperties);
		TransferOptions transferOptions = irodsSession
				.buildTransferOptionsBasedOnJargonProperties();

		Assert.assertEquals("did not set computechecksum", true,
				transferOptions.isComputeChecksumAfterTransfer());

	}
	
	@Test
	public void testGetTransferOptionsWithIntraFileCallbacksTrue()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setIntraFileStatusCallbacks(true);

		irodsSession.setJargonProperties(overrideJargonProperties);
		TransferOptions transferOptions = irodsSession
				.buildTransferOptionsBasedOnJargonProperties();

		Assert.assertEquals("did not set intra file callbacks", true,
				transferOptions.isIntraFileStatusCallbacks());

	}


	@Test
	public void testGetTransferOptionsWithResourceRedirectsTrue()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setAllowPutGetResourceRedirects(true);

		irodsSession.setJargonProperties(overrideJargonProperties);
		TransferOptions transferOptions = irodsSession
				.buildTransferOptionsBasedOnJargonProperties();

		Assert.assertEquals("did not set allow resource redirects", true,
				transferOptions.isAllowPutGetResourceRedirects());

	}

	@Test
	public void testBuildPipelineConfiguration()
			throws Exception {
		IRODSProtocolManager irodsConnectionManager = IRODSSimpleProtocolManager
				.instance();
		testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSSession irodsSession = IRODSSession
				.instance(irodsConnectionManager);
		irodsSession.closeSession();

		SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
		overrideJargonProperties.setInternalCacheBufferSize(1);
		overrideJargonProperties.setInternalInputStreamBufferSize(2);
		overrideJargonProperties.setInternalOutputStreamBufferSize(3);
		overrideJargonProperties.setIRODSParallelTransferSocketTimeout(4);
		overrideJargonProperties.setIRODSSocketTimeout(5);
		overrideJargonProperties.setLocalFileOutputStreamBufferSize(6);
		overrideJargonProperties.setSendInputStreamBufferSize(7);
		
		irodsSession.setJargonProperties(overrideJargonProperties);
		
		PipelineConfiguration pipelineConfiguration = PipelineConfiguration.instance(overrideJargonProperties);

		TestCase.assertEquals("did not set cacheBufferSize", overrideJargonProperties.getInternalCacheBufferSize(), pipelineConfiguration.getInternalCacheBufferSize());
		TestCase.assertEquals("did not set internalInputStreamBufferSize", overrideJargonProperties.getInternalInputStreamBufferSize(), pipelineConfiguration.getInternalInputStreamBufferSize());
		TestCase.assertEquals("did not set internalOutputStreamBufferSize", overrideJargonProperties.getInternalOutputStreamBufferSize(), pipelineConfiguration.getInternalOutputStreamBufferSize());
		TestCase.assertEquals("did not set parallelSocketTimeout", overrideJargonProperties.getIRODSParallelTransferSocketTimeout(), pipelineConfiguration.getIrodsParallelSocketTimeout());
		TestCase.assertEquals("did not set irodsSocketTimeout", overrideJargonProperties.getIRODSSocketTimeout(), pipelineConfiguration.getIrodsSocketTimeout());
		TestCase.assertEquals("did not set localFileOutputStreamBuffer", overrideJargonProperties.getLocalFileOutputStreamBufferSize(), pipelineConfiguration.getLocalFileOutputStreamBufferSize());
		TestCase.assertEquals("did not set sendInputStreamBufferSize", overrideJargonProperties.getSendInputStreamBufferSize(), pipelineConfiguration.getSendInputStreamBufferSize());
		
	}
	
}
