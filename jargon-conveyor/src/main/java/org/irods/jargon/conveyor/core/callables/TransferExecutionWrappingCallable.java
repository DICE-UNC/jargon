/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import java.util.concurrent.Callable;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferExecutionWrappingCallable implements
		Callable<ConveyorExecutionWrapperFuture> {

	private final Transfer transfer;
	private final ConveyorService conveyorService;

	/**
	 * 
	 */
	public TransferExecutionWrappingCallable(final Transfer transfer,
			final ConveyorService conveyorService) {

		this.transfer = transfer;
		this.conveyorService = conveyorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public ConveyorExecutionWrapperFuture call() throws Exception {

		// create callable from factory

		// run callable and wait for future

		// to stuff to queue server

		// unlock executor
		return null;
	}

}
