package org.irods.jargon.core.transfer;


import junit.framework.TestCase;

import org.irods.jargon.core.exception.JargonException;
import org.junit.Test;

public class DefaultTransferControlBlockTest {

	
	@Test
	public void testInstance() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock.instance();
		TestCase.assertNotNull(testControlBlock);
	}
	
	@Test(expected=JargonException.class)
	public void testInstanceBadMaxErrors() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock.instance(null, -3);
		TestCase.assertNotNull(testControlBlock);
	}
	
	public void testInstanceGoodMaxErrors() throws Exception {
		TransferControlBlock testControlBlock = DefaultTransferControlBlock.instance(null, 5);
		TestCase.assertNotNull(testControlBlock);
	}
	
}
