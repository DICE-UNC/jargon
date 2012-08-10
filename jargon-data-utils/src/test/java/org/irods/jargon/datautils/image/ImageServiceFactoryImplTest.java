package org.irods.jargon.datautils.image;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ImageServiceFactoryImplTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInstanceThumbnailService() {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		ImageServiceFactory factory = new ImageServiceFactoryImpl(
				irodsAccessObjectFactory);
		ThumbnailService service = factory
				.instanceThumbnailService(testingPropertiesHelper
						.buildIRODSAccountFromTestProperties(testingProperties));
		Assert.assertNotNull("no service returned", service);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceThumbnailServiceNullAccount() {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		ImageServiceFactory factory = new ImageServiceFactoryImpl(
				irodsAccessObjectFactory);
		factory.instanceThumbnailService(null);
	}

}
