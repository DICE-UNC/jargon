package org.irods.jargon.conveyor.flowmanager.flow;

import junit.framework.Assert;

import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Test;

public class SelectorProcessorTest {

	@Test
	public void testCompareSelectorHostExact() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setHostSelector("test");
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost("test");
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertTrue("should have evaluated as passed", actual);

	}

	@Test
	public void testCompareSelectorHostWildCard() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setHostSelector("te*");
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost("test");
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertTrue("should have evaluated as passed", actual);

	}

	@Test
	public void testCompareSelectorHostExactNoMatch() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setHostSelector("testx");
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost("test");
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertFalse("should not have evaluated as passed", actual);

	}

	@Test
	public void testCompareSelectorZoneExact() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setZoneSelector("test");
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setZone("test");
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertTrue("should have evaluated as passed", actual);

	}

	@Test
	public void testCompareSelectorZoneWildCard() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setZoneSelector("te*");
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setZone("test");
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertTrue("should have evaluated as passed", actual);

	}

	@Test
	public void testCompareSelectorZoneExactNoMatch() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setZoneSelector("testx");
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setZone("test");
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertFalse("should not have evaluated as passed", actual);

	}

	@Test
	public void testCompareActionExact() {
		FlowSpec flowSpec = new FlowSpec();
		flowSpec.getSelector().setHostSelector("test");
		flowSpec.getSelector().setFlowActionEnum(FlowActionEnum.GET);
		SelectorProcessor selectorProcessor = new SelectorProcessor();
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost("test");
		transfer.setGridAccount(gridAccount);
		transfer.setTransferType(TransferType.GET);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		boolean actual = selectorProcessor.evaluateSelectorForTransfer(
				flowSpec, transferAttempt);

		Assert.assertTrue("should have evaluated as passed", actual);

	}

}
