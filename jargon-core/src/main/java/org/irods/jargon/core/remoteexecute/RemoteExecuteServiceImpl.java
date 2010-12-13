/**
 * 
 */
package org.irods.jargon.core.remoteexecute;

import static edu.sdsc.grid.io.irods.IRODSConstants.BinBytesBuf_PI;
import static edu.sdsc.grid.io.irods.IRODSConstants.buf;
import static edu.sdsc.grid.io.irods.IRODSConstants.buflen;

import java.io.InputStream;

import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ExecCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.Base64;
import edu.sdsc.grid.io.irods.Tag;

/**
 * Service for running remote commands (scripts) on iRODS. This is equivalent to
 * the iexecmd. This is a lower-level service, and is not meant for use as a
 * public API. Please consult the appropriate access object for public
 * interfaces for command execution.
 * 
 * This object is immutable.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RemoteExecuteServiceImpl implements RemoteExecutionService {

	private final IRODSCommands irodsCommands;
	private final String commandToExecuteWithoutArguments;
	private final String argumentsToPassWithCommand;
	private final String executionHost;
	private final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;

	private static final Logger log = LoggerFactory
			.getLogger(RemoteExecuteServiceImpl.class);

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
		sb.append("\n   irodsCommands:");
		sb.append(irodsCommands);
		return sb.toString();
	}

	/**
	 * static initializer for a remote execution service.
	 * 
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the name of the command to execute.
	 *            Do not put arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> that is blank, or has the arguments to
	 *            send with the given command
	 * @param executionHost
	 *            <code>String</code> that can optionally point to the host on
	 *            which the command should be executed. Blank if not used.
	 * @param absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn
	 *            <code>String</code> that can optionally give an iRODS absolute
	 *            path. This is used within iRODS to find the host upon which
	 *            the file is located, and that host can be used to execute the
	 *            given command.
	 * @return <code>RemoteExecutionService</code>
	 * @throws JargonException
	 */
	public static final RemoteExecutionService instance(
			final IRODSCommands irodsCommands,
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn)
			throws JargonException {
		return new RemoteExecuteServiceImpl(irodsCommands,
				commandToExecuteWithoutArguments, argumentsToPassWithCommand,
				executionHost,
				absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);
	}

	/**
	 * Constructor for a remote execution service.
	 * 
	 * @param irodsCommands
	 *            {@link org.irods.jargon.core.connection.IRODSCommands} that
	 *            will be used to send commands to iRODS. The connection is used
	 *            but not closed or altered.
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the name of the command to execute.
	 *            Do not put arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> that is blank, or has the arguments to
	 *            send with the given command
	 * @param executionHost
	 *            <code>String</code> that can optionally point to the host on
	 *            which the command should be executed. Blank if not used.
	 * @param absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn
	 *            <code>String</code> that can optionally give an iRODS absolute
	 *            path. This is used within iRODS to find the host upon which
	 *            the file is located, and that host can be used to execute the
	 *            given command.
	 * @throws JargonException
	 */
	private RemoteExecuteServiceImpl(
			final IRODSCommands irodsCommands,
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn)
			throws JargonException {

		if (irodsCommands == null) {
			throw new JargonException("null irodsCommands");
		}

		if (commandToExecuteWithoutArguments == null
				|| commandToExecuteWithoutArguments.length() == 0) {
			throw new JargonException("null commandToExecuteWithoutArguments");
		}

		if (argumentsToPassWithCommand == null) {
			throw new JargonException(
					"null argumentsToPassWithCommand, set to blank if not used");
		}

		if (executionHost == null) {
			throw new JargonException(
					"null executionHost, set to blank if not used");
		}

		if (absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn == null) {
			throw new JargonException(
					"null absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn, set to blank if not used");
		}

		this.irodsCommands = irodsCommands;
		this.commandToExecuteWithoutArguments = commandToExecuteWithoutArguments;
		this.argumentsToPassWithCommand = argumentsToPassWithCommand;
		this.executionHost = executionHost;
		this.absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn = absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.remoteexecute.RemoteExecutionService#execute()
	 */
	@Override
	public InputStream execute() throws JargonException {
		log.info("executing a remote command:{}", toString());

		ExecCmd execCmd = ExecCmd.instanceWithHostAndArgumentsToPassParameters(
				commandToExecuteWithoutArguments, argumentsToPassWithCommand,
				executionHost,
				absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);

		Tag message;
		StringBuilder buffer = new StringBuilder();

		try {
			message = irodsCommands.irodsFunction(execCmd);
		} catch (NullPointerException e) {
			log.error("NullPointerException encountered executing a command", e);
			throw new JargonException(
					"NullPointerException executing a command, which can occur if the command output is too long");
		}

		if (message == null) {
			throw new JargonException("null response from remote execution");
		} else {
			// message
			int length = message.getTag(BinBytesBuf_PI, 0).getTag(buflen)
					.getIntValue();
			if (length > 0) {
				buffer.append(message.getTag(BinBytesBuf_PI, 0).getTag(buf)
						.getStringValue());
			}

			// error
			length = message.getTag(BinBytesBuf_PI, 1).getTag(buflen)
					.getIntValue();
			if (length > 0) {
				buffer.append(message.getTag(BinBytesBuf_PI, 1).getTag(buf)
						.getStringValue());
			}

		}

		return new java.io.ByteArrayInputStream(Base64.fromString(buffer
				.toString()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.remoteexecute.RemoteExecutionService#execute()
	 */
	@Override
	public InputStream executeAndStream() throws JargonException {
		log.info("executing a remote command with streaming:{}", toString());

		// FIXME: this checks for 2.4.1, but the real capabiiity will be post
		// 2.4.1, fix this at next release
		if (this.getIrodsCommands().getIRODSServerProperties().getRelVersion()
				.compareTo("rods2.4.1") < 0) {
			log.error(
					"cannot stream remote commands, unsupported on this iRODS version:{}",
					getIrodsCommands().getIRODSServerProperties());
			throw new JargonException(
					"cannot stream remote commands, unsupported on this iRODS version");
		}

		ExecCmd execCmd = ExecCmd
				.instanceWithHostAndArgumentsToPassParametersAllowingStreamingForLargeResults(
						commandToExecuteWithoutArguments,
						argumentsToPassWithCommand, executionHost,
						absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);

		Tag message;
		StringBuilder buffer = new StringBuilder();

		try {
			message = irodsCommands.irodsFunction(execCmd);
		} catch (NullPointerException e) {
			log.error("NullPointerException encountered executing a command", e);
			throw new JargonException(
					"NullPointerException executing a command, which can occur if the command output is too long");
		}

		if (message == null) {
			throw new JargonException("null response from remote execution");
		} else {
			// message
			int length = message.getTag(BinBytesBuf_PI, 0).getTag(buflen)
					.getIntValue();
			if (length > 0) {
				buffer.append(message.getTag(BinBytesBuf_PI, 0).getTag(buf)
						.getStringValue());
			}

			// error
			length = message.getTag(BinBytesBuf_PI, 1).getTag(buflen)
					.getIntValue();
			if (length > 0) {
				buffer.append(message.getTag(BinBytesBuf_PI, 1).getTag(buf)
						.getStringValue());
			}

		}

		return new java.io.ByteArrayInputStream(Base64.fromString(buffer
				.toString()));

	}

	public IRODSCommands getIrodsCommands() {
		return irodsCommands;
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
