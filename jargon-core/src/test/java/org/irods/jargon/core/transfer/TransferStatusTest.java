package org.irods.jargon.core.transfer;

import junit.framework.Assert;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.junit.Test;

public class TransferStatusTest {

	@Test
	public void testInstance() throws Exception {
		TransferStatus transferStatus = TransferStatus.instance(
				TransferType.GET, "source", "target", "", 10L, 2L, 3, 0, 4,
				TransferState.IN_PROGRESS_START_FILE, "xxx", "xxx");
		Assert.assertNotNull(transferStatus);
		Assert.assertEquals("total files not propery set from constructor", 4,
				transferStatus.getTotalFilesToTransfer());
		Assert.assertEquals(
				"total files so far not propery set from constructor", 3,
				transferStatus.getTotalFilesTransferredSoFar());
	}

	@Test
	public void testInstanceException() throws Exception {
		TransferStatus transferStatus = TransferStatus.instanceForException(
				TransferType.GET, "source", "target", "", 10L, 2L, 3, 0, 4,
				new Exception("blah"), "xxx", "xxx");
		Assert.assertNotNull(transferStatus);
		Assert.assertEquals(TransferState.FAILURE,
				transferStatus.getTransferState());
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullType() throws Exception {
		TransferStatus.instance(null, "source", "target", "", 10L, 2L, 0, 0, 0,
				TransferState.IN_PROGRESS_START_FILE, "xxx", "xxx");
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullSource() throws Exception {
		TransferStatus.instance(TransferType.GET, null, "target", "", 10L, 2L,
				0, 0, 0, TransferState.IN_PROGRESS_COMPLETE_FILE, "xxx", "xxx");
	}

	@Test(expected = JargonException.class)
	public void testInstanceNullTarget() throws Exception {
		TransferStatus.instance(TransferType.GET, "source", null, "", 10L, 2L,
				0, 0, 0, TransferState.IN_PROGRESS_START_FILE, "xxx", "xxx");
	}

	@Test(expected = JargonException.class)
	public void testInstanceTotalLessThanZero() throws Exception {
		TransferStatus.instance(TransferType.GET, "source", "blah", "", -10L,
				2L, 0, 0, 0, TransferState.IN_PROGRESS_COMPLETE_FILE, "xxx",
				"xxx");
	}

}
