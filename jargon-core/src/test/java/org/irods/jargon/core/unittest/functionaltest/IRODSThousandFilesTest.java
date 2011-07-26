package org.irods.jargon.core.unittest.functionaltest;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IputCommand;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IRODSThousandFilesTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsThousandFilesTestParent";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	public static final String collDir = "coll";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);

		// put in the thousand files
		String testFilePrefix = "thousandFileTest";
		String testFileSuffix = ".txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateManyFilesInGivenDirectory(IRODS_TEST_SUBDIR_PATH
				+ '/' + collDir, testFilePrefix, testFileSuffix, 1000, 20, 500);

		// put scratch files into irods in the right place
		// 1000 files from 20-500K size
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		// make the put subdir
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
		ImkdirCommand iMkdirCommand = new ImkdirCommand();
		iMkdirCommand.setCollectionName(targetIrodsCollection);
		invoker.invokeCommandAndGetResultAsString(iMkdirCommand);

		// put the files by putting the collection
		IputCommand iputCommand = new IputCommand();
		iputCommand.setForceOverride(true);
		iputCommand.setIrodsFileName(targetIrodsCollection);
		iputCommand.setLocalFileName(absPath + collDir);
		iputCommand.setRecursive(true);
		invoker.invokeCommandAndGetResultAsString(iputCommand);

		// now add avu's to each
		addAVUsToEachFile();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public static final void addAVUsToEachFile() throws Exception {

		String avu1Attrib = "avu1";
		String avu1Value = "avu1value";
		String avu2Attrib = "avu2";

		IRODSAccount account = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

		IRODSFile irodsFile = irodsFileSystem
				.getIRODSFileFactory(account)
				.instanceIRODSFile(
						testingPropertiesHelper
								.buildIRODSCollectionAbsolutePathFromTestProperties(
										testingProperties,
										IRODS_TEST_SUBDIR_PATH + '/' + collDir));

		// get a list of files underneath the top-level directory, and add some
		// avu's to each one

		String[] fileList = irodsFile.list();
		IRODSFile subFile = null;
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(account);

		for (String element : fileList) {
			subFile = irodsFileSystem.getIRODSFileFactory(account)
					.instanceIRODSFile(
							irodsFile.getAbsolutePath() + '/' + element);

			dataObjectAO.addAVUMetadata(subFile.getAbsolutePath(),
					AvuData.instance(avu1Attrib, avu1Value, ""));
			dataObjectAO.addAVUMetadata(subFile.getAbsolutePath(),
					AvuData.instance(avu2Attrib, avu1Value, ""));

		}

		irodsFileSystem.close();
	}

	@Test
	public void testSearchForAvuFiles() throws Exception {
		String avu1Attrib = "avu1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements
				.add(AVUQueryElement.instanceForValueQuery(
						AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
						avu1Attrib));

		List<MetaDataAndDomainData> metadataElements = dataObjectAO
				.findMetadataValuesByMetadataQuery(avuQueryElements);

		// should have 1000 in this batch
		Assert.assertEquals("did not get back the  rows I requested", 1000,
				metadataElements.size());

		irodsFileSystem.close();

	}

}
