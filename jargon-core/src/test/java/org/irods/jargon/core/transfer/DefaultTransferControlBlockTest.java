package org.irods.jargon.core.transfer;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.junit.Test;

public class DefaultTransferControlBlockTest {

	@Test
	public void testInstance() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
				.instance();
		Assert.assertNotNull(testControlBlock);
	}

	@Test(expected = JargonException.class)
	public void testInstanceBadMaxErrors() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
				.instance(null, -3);
		Assert.assertNotNull(testControlBlock);
	}

	public void testInstanceGoodMaxErrors() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock
				.instance(null, 5);
		Assert.assertNotNull(testControlBlock);
	}

}
