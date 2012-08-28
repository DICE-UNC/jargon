package org.irods.jargon.core.connection;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PipelineConfigurationTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test propogation of reconnect time in millis set in jargon properties to
	 * show up in pipeline configuration
	 */
	@Test
	public final void testGetReconnectTimeInMillisAfterSettingInJargonProperty()
			throws Exception {
		SettableJargonProperties settableJargonProperties = new SettableJargonProperties();
		settableJargonProperties.setReconnectTimeInMillis(1);
		PipelineConfiguration actual = PipelineConfiguration
				.instance(settableJargonProperties);
		TestCase.assertEquals(
				"did not set reconnect time as reflected in jargon properties",
				1, actual.getReconnectTimeInMillis());
	}

}
