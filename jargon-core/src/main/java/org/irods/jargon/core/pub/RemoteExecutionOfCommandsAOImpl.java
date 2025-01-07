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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Access object implementation to remotely execute scripts and commands on
 * iRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RemoteExecutionOfCommandsAOImpl extends IRODSGenericAO implements RemoteExecutionOfCommandsAO {

	private static final Logger log = LogManager.getLogger(RemoteExecutionOfCommandsAOImpl.class);

	/**
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected RemoteExecutionOfCommandsAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO#
	 * executeARemoteCommandAndGetStreamGivingCommandNameAndArgs (java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public InputStream executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(
			final String commandToExecuteWithoutArguments, final String argumentsToPassWithCommand)
			throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl.instance(getIRODSProtocol(),
				commandToExecuteWithoutArguments, argumentsToPassWithCommand, "");

		if (isAbleToStreamLargeResults()) {
			return remoteExecuteService.executeAndStream();
		} else {
			return remoteExecuteService.execute();
		}
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
			final String commandToExecuteWithoutArguments, final String argumentsToPassWithCommand,
			final String executionHost) throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl.instance(getIRODSProtocol(),
				commandToExecuteWithoutArguments, argumentsToPassWithCommand, executionHost);

		if (isAbleToStreamLargeResults()) {
			return remoteExecuteService.executeAndStream();
		} else {
			return remoteExecuteService.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO#
	 * executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream executeARemoteCommandAndGetStreamAddingPhysicalPathAsFirstArgumentToRemoteScript(
			final String commandToExecuteWithoutArguments, final String argumentsToPassWithCommand,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn) throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl.instanceWhenUsingAbsPathToSetCommandArg(
				getIRODSProtocol(), commandToExecuteWithoutArguments, argumentsToPassWithCommand, "",
				absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);

		if (isAbleToStreamLargeResults()) {
			return remoteExecuteService.executeAndStream();
		} else {
			return remoteExecuteService.execute();
		}
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
			final String commandToExecuteWithoutArguments, final String argumentsToPassWithCommand,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn) throws JargonException {
		log.info("executing remote command");
		// input parms checked in instance method
		RemoteExecutionService remoteExecuteService = RemoteExecuteServiceImpl
				.instanceWhenUsingAbsPathToFindExecutionHost(getIRODSProtocol(), commandToExecuteWithoutArguments,
						argumentsToPassWithCommand, "", absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);

		if (isAbleToStreamLargeResults()) {
			return remoteExecuteService.executeAndStream();
		} else {
			return remoteExecuteService.execute();
		}
	}

	/**
	 * Inquire if this irodsServer has the ability to stream large results back.
	 *
	 * @return {@code boolean}
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected boolean isAbleToStreamLargeResults() throws JargonException {

		if (getIRODSServerProperties().isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods2.4.1")) {
			return true;
		} else {
			return false;
		}
	}

}
