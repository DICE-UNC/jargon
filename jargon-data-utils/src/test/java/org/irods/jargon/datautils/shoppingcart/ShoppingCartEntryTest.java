package org.irods.jargon.datautils.shoppingcart;

import org.junit.Assert;
import org.junit.Test;

public class ShoppingCartEntryTest {

	@Test
	public void testInstance() throws Exception {
		String fileName = "huh";
		ShoppingCartEntry entry = ShoppingCartEntry.instance(fileName);
		Assert.assertEquals("did not set file name", fileName, entry.getFileName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceNullFile() throws Exception {
		ShoppingCartEntry.instance(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstanceBlankFile() throws Exception {
		ShoppingCartEntry.instance("");
	}

}
