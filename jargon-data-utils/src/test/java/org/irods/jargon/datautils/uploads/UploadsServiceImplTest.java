/**
 * 
 */
package org.irods.jargon.datautils.uploads;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class UploadsServiceImplTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSAccount irodsAccount;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
	}

	/**
	 * Test method for
	 * {@link org.irods.jargon.datautils.uploads.UploadsServiceImpl#getUploadsDirectory()}
	 * .
	 */
	@Test
	public void testGetUploadsDirectory() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		String testUploadsDir = MiscIRODSUtils
				.computeHomeDirectoryForIRODSAccount(irodsAccount)
				+ "/"
				+ UploadsService.UPLOADS_DIR_DEFAULT_NAME;

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);

		Mockito.when(irodsFile.exists()).thenReturn(false);

		Mockito.when(irodsFileFactory.instanceIRODSFile(testUploadsDir))
				.thenReturn(irodsFile);

		UploadsService uploadsService = new UploadsServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		IRODSFile actual = uploadsService.getUploadsDirectory();
		Assert.assertNotNull("no irods file returned", actual);
		Mockito.verify(irodsFile).mkdirs();

	}

	@Test
	public void testGetUploadsDirectoryAlreadyExists() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		String testUploadsDir = MiscIRODSUtils
				.computeHomeDirectoryForIRODSAccount(irodsAccount)
				+ "/"
				+ UploadsService.UPLOADS_DIR_DEFAULT_NAME;

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);

		Mockito.when(irodsFile.exists()).thenReturn(true);

		Mockito.when(irodsFileFactory.instanceIRODSFile(testUploadsDir))
				.thenReturn(irodsFile);

		UploadsService uploadsService = new UploadsServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		IRODSFile actual = uploadsService.getUploadsDirectory();
		Assert.assertNotNull("no irods file returned", actual);
		Mockito.verify(irodsFile, Mockito.never()).mkdirs();

	}

	@Test
	public void testDeleteUploadsDirectory() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSFileFactory irodsFileFactory = Mockito
				.mock(IRODSFileFactory.class);

		Mockito.when(irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount))
				.thenReturn(irodsFileFactory);

		String testUploadsDir = MiscIRODSUtils
				.computeHomeDirectoryForIRODSAccount(irodsAccount)
				+ "/"
				+ UploadsService.UPLOADS_DIR_DEFAULT_NAME;

		IRODSFile irodsFile = Mockito.mock(IRODSFile.class);

		Mockito.when(irodsFile.exists()).thenReturn(true);

		Mockito.when(irodsFileFactory.instanceIRODSFile(testUploadsDir))
				.thenReturn(irodsFile);

		UploadsService uploadsService = new UploadsServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		uploadsService.deleteUploadsDirectory();
		Mockito.verify(irodsFile).deleteWithForceOption();
	}

}
