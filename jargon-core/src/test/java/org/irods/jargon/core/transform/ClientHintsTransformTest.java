package org.irods.jargon.core.transform;

import org.irods.jargon.core.protovalues.IcatTypeEnum;
import org.irods.jargon.core.pub.domain.ClientHints;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.junit.Assert;
import org.junit.Test;

public class ClientHintsTransformTest {

	@Test
	public void testClientHintsFromIrodsJson() throws Exception {
		String jsonHints = LocalFileUtils.getClasspathResourceFileAsString("/sample-json/client-hints1.json");
		ClientHintsTransform clientHintsTransform = new ClientHintsTransform();

		ClientHints actual = clientHintsTransform.clientHintsFromIrodsJson(jsonHints);
		Assert.assertNotNull("no client hints returned", actual);
		// check a few vals
		Assert.assertEquals("on", actual.getStrictAcls());
		Assert.assertEquals("SHA256", actual.getHashScheme());
		Assert.assertFalse("no specific queries loaded", actual.getSpecificQueries().isEmpty());
		Assert.assertFalse("no rules loaded", actual.getRules().isEmpty());

	}

	@Test
	public void testClientHintsDetermineIcatDatabase() throws Exception {
		String jsonHints = LocalFileUtils.getClasspathResourceFileAsString("/sample-json/client-hints1.json");
		ClientHintsTransform clientHintsTransform = new ClientHintsTransform();

		ClientHints actual = clientHintsTransform.clientHintsFromIrodsJson(jsonHints);
		Assert.assertEquals(IcatTypeEnum.POSTGRES, actual.whatTypeOfIcatIsIt());

	}

}
