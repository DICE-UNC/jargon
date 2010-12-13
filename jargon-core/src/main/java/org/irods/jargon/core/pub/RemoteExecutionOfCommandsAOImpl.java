/**
 * 
 */
package org.irods.jargon.core.pub;

import java.io.InputStream;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImpl;
import org.irods.jargon.core.remoteexecute.RemoteExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object implementation to remotely execute scripts and commands on
 * iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RemoteExecutionOfCommandsAOImpl extends IRODSGenericAO implements
		RemoteExecutionOfCommandsAO {

	private static final Logger log = LoggerFactory
			.getLogger(RemoteExecutionOfCommandsAOImpl.class);

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected RemoteExecutionOfCommandsAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO#
	 * executeARemoteCommandAndGetStreamGivingCommandNameAndArgs
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand) throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(this.getIRODSProtocol(),
						commandToExecuteWithoutArguments,
						argumentsToPassWithCommand, "", "");
		return remoteExecuteService.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO#
	 * executeARemoteCommandAndGetStreamGivingCommandNameAndArgsAndHost
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream executeARemoteCommandAndGetStreamGivingCommandNameAndArgsAndHost(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand, final String executionHost)
			throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(this.getIRODSProtocol(),
						commandToExecuteWithoutArguments,
						argumentsToPassWithCommand, executionHost, "");
		return remoteExecuteService.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO#
	 * executeARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToDetermineHost
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream executeARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToDetermineHost(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn)
			throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instance(this.getIRODSProtocol(),
						commandToExecuteWithoutArguments,
						argumentsToPassWithCommand, "",
						absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);
		return remoteExecuteService.execute();
	}

}
