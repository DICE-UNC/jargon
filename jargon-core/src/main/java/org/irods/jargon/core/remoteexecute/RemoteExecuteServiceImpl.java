/**
 *
 */
package org.irods.jargon.core.remoteexecute;

import java.io.InputStream;
import java.io.SequenceInputStream;

import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ExecCmd;
import org.irods.jargon.core.packinstr.ExecCmd.PathHandlingMode;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.io.RemoteExecutionBinaryResultInputStream;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for running remote commands (scripts) on iRODS. This is equivalent to
 * the iexecmd. This is a lower-level service, and is not meant for use as a
 * public API. Please consult the appropriate access object for public
 * interfaces for command execution.
 * <p>
 * This object is immutable, but should not be shared between threads, as it
 * holds a reference to a connection to an iRODS Agent.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RemoteExecuteServiceImpl implements RemoteExecutionService {

	private final IRODSMidLevelProtocol irodsMidLevelProtocol;
	private final String commandToExecuteWithoutArguments;
	private final String argumentsToPassWithCommand;
	private final String executionHost;
	private final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;
	public static final String STREAMING_API_CUTOFF = "rods4.1";
	private PathHandlingMode pathHandlingMode = PathHandlingMode.NONE;

	private static final Logger log = LoggerFactory.getLogger(RemoteExecuteServiceImpl.class);
	private static final String STATUS = "status";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RemoteExecuteServiceImpl");
		sb.append("\n  commandToExecuteWithoutArguments:");
		sb.append(commandToExecuteWithoutArguments);
		sb.append("\n   argumentsToPassWithCommand:");
		sb.append(argumentsToPassWithCommand);
		sb.append("\n   executionHost:");
		sb.append(executionHost);
		sb.append("\n   absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn:");
		sb.append(absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);
		sb.append("\n   pathHandlingMode:");
		sb.append(pathHandlingMode);
		sb.append("\n   irodsMidLevelProtocol:");
		sb.append(irodsMidLevelProtocol);
		return sb.toString();
	}

	/**
	 * Static instance method for a remote execution service.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link IRODSMidLevelProtocol}
	 * @param commandToExecuteWithoutArguments
	 *            {@code String} with the name of the command to execute. Do not put
	 *            arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            {@code String} that is blank, or has the arguments to send with
	 *            the given command
	 * @param executionHost
	 *            {@code String} that can optionally point to the host on which the
	 *            command should be executed. Blank if not used.
	 *
	 * @return {@code RemoteExecutionService}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public static final RemoteExecutionService instance(final IRODSMidLevelProtocol irodsMidLevelProtocol,
			final String commandToExecuteWithoutArguments, final String argumentsToPassWithCommand,
			final String executionHost) throws JargonException {
		return new RemoteExecuteServiceImpl(irodsMidLevelProtocol, commandToExecuteWithoutArguments,
				argumentsToPassWithCommand, executionHost, "", PathHandlingMode.NONE);
	}

	/**
	 * Static instance method for a remote execution service when using the provided
	 * iRODS absolute path to compute the physical path and add it as a command
	 * argument.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link IRODSMidLevelProtocol}
	 * @param commandToExecuteWithoutArguments
	 *            {@code String} with the name of the command to execute. Do not put
	 *            arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            {@code String} that is blank, or has the arguments to send with
	 *            the given command
	 * @param executionHost
	 *            {@code String} that can optionally point to the host on which the
	 *            command should be executed. Blank if not used.
	 * @param absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn
	 *            {@code String} that can optionally give an iRODS absolute path.
	 *            This is used within iRODS to find the host upon which the file is
	 *            located, and that host can be used to execute the given command.
	 * @return {@code RemoteExecutionService}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public static final RemoteExecutionService instanceWhenUsingAbsPathToSetCommandArg(
			final IRODSMidLevelProtocol irodsMidLevelProtocol, final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand, final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn) throws JargonException {
		return new RemoteExecuteServiceImpl(irodsMidLevelProtocol, commandToExecuteWithoutArguments,
				argumentsToPassWithCommand, executionHost, absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn,
				PathHandlingMode.USE_PATH_TO_ADD_PHYS_PATH_ARGUMENT_TO_REMOTE_SCRIPT);
	}

	/**
	 * Static instance method for a remote execution service when using the provided
	 * iRODS absolute path to find the host upon which to execute.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link IRODSMidLevelProtocol}
	 * @param commandToExecuteWithoutArguments
	 *            {@code String} with the name of the command to execute. Do not put
	 *            arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            {@code String} that is blank, or has the arguments to send with
	 *            the given command
	 * @param executionHost
	 *            {@code String} that can optionally point to the host on which the
	 *            command should be executed. Blank if not used.
	 * @param absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn
	 *            {@code String} that can optionally give an iRODS absolute path.
	 *            This is used within iRODS to find the host upon which the file is
	 *            located, and that host can be used to execute the given command.
	 * @return {@code RemoteExecutionService}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public static final RemoteExecutionService instanceWhenUsingAbsPathToFindExecutionHost(
			final IRODSMidLevelProtocol irodsMidLevelProtocol, final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand, final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn) throws JargonException {
		return new RemoteExecuteServiceImpl(irodsMidLevelProtocol, commandToExecuteWithoutArguments, argumentsToPassWithCommand,
				executionHost, absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn,
				PathHandlingMode.USE_PATH_TO_FIND_EXECUTING_HOST);
	}

	/**
	 * Constructor for a remote execution service.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link org.irods.jargon.core.connection.IRODSMidLevelProtocol}
	 *            that will be used to send commands to iRODS. The connection is
	 *            used but not closed or altered.
	 * @param commandToExecuteWithoutArguments
	 *            {@code String} with the name of the command to execute. Do not put
	 *            arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            {@code String} that is blank, or has the arguments to send with
	 *            the given command
	 * @param executionHost
	 *            {@code String} that can optionally point to the host on which the
	 *            command should be executed. Blank if not used.
	 * @param absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn
	 *            {@code String} that can optionally give an iRODS absolute path.
	 *            This is used within iRODS to find the host upon which the file is
	 *            located, and that host can be used to execute the given command.
	 * @param pathHandlingMode
	 *            {@link ExecCmd.PathHandlingMode} enum value that provides
	 *            additional information about the request functionality. This is
	 *            used in the -P and -p equivalent modes, and otherwise is set to
	 *            {@code NONE}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	private RemoteExecuteServiceImpl(final IRODSMidLevelProtocol irodsCommands,
			final String commandToExecuteWithoutArguments, final String argumentsToPassWithCommand,
			final String executionHost, final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn,
			final PathHandlingMode pathHandlingMode) throws JargonException {

		if (irodsCommands == null) {
			throw new JargonException("null irodsMidLevelProtocol");
		}

		if (commandToExecuteWithoutArguments == null || commandToExecuteWithoutArguments.length() == 0) {
			throw new JargonException("null commandToExecuteWithoutArguments");
		}

		if (argumentsToPassWithCommand == null) {
			throw new JargonException("null argumentsToPassWithCommand, set to blank if not used");
		}

		if (executionHost == null) {
			throw new JargonException("null executionHost, set to blank if not used");
		}

		if (absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn == null) {
			throw new JargonException(
					"null absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn, set to blank if not used");
		}

		if (pathHandlingMode == null) {
			throw new IllegalArgumentException("null pathHandlingMode");
		}

		this.irodsMidLevelProtocol = irodsCommands;
		this.commandToExecuteWithoutArguments = commandToExecuteWithoutArguments;
		this.argumentsToPassWithCommand = argumentsToPassWithCommand;
		this.executionHost = executionHost;
		this.absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn = absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;
		this.pathHandlingMode = pathHandlingMode;

	}

	@Override
	public InputStream execute() throws JargonException {
		log.info("executing a remote command:{}", toString());

		ExecCmd execCmd = null;
		if (getIrodsCommands().getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(STREAMING_API_CUTOFF)) {
			execCmd = ExecCmd.instanceWithHostAndArgumentsToPassParametersPost25(commandToExecuteWithoutArguments,
					argumentsToPassWithCommand, executionHost,
					absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn, pathHandlingMode);
		} else {
			execCmd = ExecCmd.instanceWithHostAndArgumentsToPassParametersPriorTo25(commandToExecuteWithoutArguments,
					argumentsToPassWithCommand, executionHost,
					absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn, pathHandlingMode);
		}

		Tag message;
		StringBuilder buffer = new StringBuilder();

		try {
			message = irodsMidLevelProtocol.irodsFunction(execCmd);
		} catch (NullPointerException e) {
			log.error("NullPointerException encountered executing a command", e);
			throw new JargonException(
					"NullPointerException executing a command, which can occur if the command output is too long");
		}

		if (message == null) {
			throw new JargonException("null response from remote execution");
		} else {
			// message
			int length = message.getTag(IRODSConstants.BinBytesBuf_PI, 0).getTag(IRODSConstants.buflen).getIntValue();
			if (length > 0) {
				buffer.append(
						message.getTag(IRODSConstants.BinBytesBuf_PI, 0).getTag(IRODSConstants.buf).getStringValue());
			}

			// error
			length = message.getTag(IRODSConstants.BinBytesBuf_PI, 1).getTag(IRODSConstants.buflen).getIntValue();
			if (length > 0) {
				buffer.append(
						message.getTag(IRODSConstants.BinBytesBuf_PI, 1).getTag(IRODSConstants.buf).getStringValue());
			}

		}

		return new java.io.ByteArrayInputStream(Base64.fromString(buffer.toString()));

	}

	@Override
	public InputStream executeAndStream() throws JargonException {
		log.info("executing a remote command with streaming:{}", toString());

		if (!getIrodsCommands().getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion(STREAMING_API_CUTOFF)) {
			log.error("cannot stream remote commands, unsupported on this iRODS version:{}",
					getIrodsCommands().getIRODSServerProperties());
			throw new JargonException("cannot stream remote commands, unsupported on this iRODS version");
		}

		ExecCmd execCmd = ExecCmd.instanceWithHostAndArgumentsToPassParametersAllowingStreamingForLargeResultsPost25(
				commandToExecuteWithoutArguments, argumentsToPassWithCommand, executionHost,
				absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn, pathHandlingMode);

		Tag message;
		StringBuilder buffer = new StringBuilder();

		try {
			message = irodsMidLevelProtocol.irodsFunction(execCmd);
		} catch (NullPointerException e) {
			log.error("NullPointerException encountered executing a command", e);
			throw new JargonException(
					"NullPointerException executing a command, which can occur if the command output is too long");
		}

		if (message == null) {
			throw new JargonException("null response from remote execution");
		}

		// message
		int length = message.getTag(IRODSConstants.BinBytesBuf_PI, 0).getTag(IRODSConstants.buflen).getIntValue();
		if (length > 0) {
			buffer.append(message.getTag(IRODSConstants.BinBytesBuf_PI, 0).getTag(IRODSConstants.buf).getStringValue());
		}

		// error
		length = message.getTag(IRODSConstants.BinBytesBuf_PI, 1).getTag(IRODSConstants.buflen).getIntValue();
		if (length > 0) {
			buffer.append(message.getTag(IRODSConstants.BinBytesBuf_PI, 1).getTag(IRODSConstants.buf).getStringValue());
		}

		return buildAppropriateResultStream(message, buffer);

	}

	private InputStream buildAppropriateResultStream(final Tag message, final StringBuilder buffer) {

		InputStream resultStream;

		/*
		 * see if the status descriptor holds a non zero, positive int If it does, then
		 * I am streaming additional binary data using the int as a file descriptor.
		 */

		int status = message.getTag(STATUS).getIntValue();
		log.debug("status from remoteexec response:{}", status);
		if (status > 0) {
			log.info("additional data will be streamed, opening up will create concatenated stream");

			if (!getIrodsCommands().getIRODSServerProperties().isAtLeastIrods410()) {

				log.error("unable to stream large files in eirods");
				throw new UnsupportedOperationException(
						"eIRODS does not currently support large result streaming from execCmd");
			}

			InputStream piData = new java.io.ByteArrayInputStream(Base64.fromString(buffer.toString()));

			@SuppressWarnings("resource")
			// this will be closed by the caller
			RemoteExecutionBinaryResultInputStream reStream = new RemoteExecutionBinaryResultInputStream(
					getIrodsCommands(), status);

			resultStream = new SequenceInputStream(piData, reStream);
		} else {
			log.info("no additional data to stream, will return simple stream from result buffer");
			resultStream = new java.io.ByteArrayInputStream(Base64.fromString(buffer.toString()));
		}
		return resultStream;
	}

	public IRODSMidLevelProtocol getIrodsCommands() {
		return irodsMidLevelProtocol;
	}

	public String getCommandToExecuteWithoutArguments() {
		return commandToExecuteWithoutArguments;
	}

	public String getArgumentsToPassWithCommand() {
		return argumentsToPassWithCommand;
	}

	public String getExecutionHost() {
		return executionHost;
	}

	public String getAbsolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn() {
		return absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;
	}

}
