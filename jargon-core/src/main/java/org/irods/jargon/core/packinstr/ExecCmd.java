/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Immutable object gives translation of an ExecCmd operation into XML. This is
 * the operation for remote execution protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

public final class ExecCmd extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "ExecCmd_PI";

	public static final int API_NBR = 634;

	public static final String CMD = "cmd";
	public static final String CMD_ARGV = "cmdArgv";
	public static final String EXEC_ADDR = "execAddr";
	public static final String HINT_PATH = "hintPath";
	public static final String ADD_PATH_TO_ARGV = "addPathToArgv";
	public static final String STREAM_STDOUT_KW = "streamStdout";

	private final String commandToExecuteWithoutArguments;
	private final String argumentsToPassWithCommand;
	private final String executionHost;
	private final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;
	private final boolean useStreaming;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ExecCmd");
		sb.append("\n  commandToExecuteWithoutArguments:");
		sb.append(commandToExecuteWithoutArguments);
		sb.append("\n   argumentsToPassWithCommand:");
		sb.append(argumentsToPassWithCommand);
		sb.append("\n   executionHost:");
		sb.append(executionHost);
		sb.append("\n   absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn:");
		sb.append(absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn);
		sb.append("\n useStreaming:");
		sb.append(useStreaming);
		return sb.toString();
	}

	/**
	 * Create an instance of the packing instruction to execute a remote command
	 * (script)
	 * 
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the name of the command to execute.
	 *            Do not put arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> that is blank, or has the arguments to
	 *            send with the given command
	 * @return <code>ExcecCmd</code> instance.
	 * @throws JargonException
	 */
	public static final ExecCmd instanceWithCommand(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand) throws JargonException {
		return new ExecCmd(commandToExecuteWithoutArguments,
				argumentsToPassWithCommand, "", "", false);
	}

	/**
	 * Create an instance of the packing instruction to execute a remote command
	 * (script). This initializer indicates to iRODS that large result buffers
	 * should be transmitted via a stream.
	 * 
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the name of the command to execute.
	 *            Do not put arguments into this field.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> that is blank, or has the arguments to
	 *            send with the given command
	 * @return <code>ExcecCmd</code> instance.
	 * @throws JargonException
	 */
	public static final ExecCmd instanceWithCommandAllowingStreamingForLargeResults(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand) throws JargonException {
		return new ExecCmd(commandToExecuteWithoutArguments,
				argumentsToPassWithCommand, "", "", true);
	}

	/**
	 * Create an instance of the packing instruction to execute a remote command
	 * (script)
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
	 * @throws JargonException
	 */
	public static final ExecCmd instanceWithHostAndArgumentsToPassParameters(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn)
			throws JargonException {
		return new ExecCmd(commandToExecuteWithoutArguments,
				argumentsToPassWithCommand, executionHost,
				absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn,
				false);
	}

	/**
	 * Create an instance of the packing instruction to execute a remote command
	 * (script). This initializer indicates to iRODS that large result buffers
	 * should be transmitted via a stream.
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
	 * @throws JargonException
	 */
	public static final ExecCmd instanceWithHostAndArgumentsToPassParametersAllowingStreamingForLargeResults(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn)
			throws JargonException {
		return new ExecCmd(commandToExecuteWithoutArguments,
				argumentsToPassWithCommand, executionHost,
				absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn,
				true);
	}

	/**
	 * Constructor for a remote execution service packing instruction call.
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
	 * @param useStreaming
	 *            <code>boolean</code> that indicates whether streaming of large
	 *            results should be allowed.
	 * @throws JargonException
	 */
	private ExecCmd(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String executionHost,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn,
			final boolean useStreaming) throws JargonException {

		super();

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

		this.commandToExecuteWithoutArguments = commandToExecuteWithoutArguments;
		this.argumentsToPassWithCommand = argumentsToPassWithCommand;
		this.executionHost = executionHost;
		this.absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn = absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn;
		this.setApiNumber(API_NBR);
		this.useStreaming = useStreaming;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(
				PI_TAG,
				new Tag[] {
						new Tag(CMD, commandToExecuteWithoutArguments),
						new Tag(CMD_ARGV, argumentsToPassWithCommand),
						new Tag(EXEC_ADDR, executionHost),
						new Tag(HINT_PATH,
								absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn),
						new Tag(ADD_PATH_TO_ARGV, 0) });
		if (useStreaming) {
			message.addTag(Tag.createKeyValueTag(STREAM_STDOUT_KW, ""));
		} else {
			message.addTag(Tag.createKeyValueTag(null));
		}

		return message;
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

	protected boolean isUseStreaming() {
		return useStreaming;
	}
}
