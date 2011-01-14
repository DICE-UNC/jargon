package org.irods.jargon.core.pub;

import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;

/**
 * Access object to remotely execute scripts and commands on iRODS.
 * <p/>
 * Note that in iRODS versions post 2.4.1, it is possible to stream large results from remote execution.  This object will interrogate the
 * capabilities of the iRODS server, and automatically use the advanced streaming algorithm if available.  
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface RemoteExecutionOfCommandsAO extends IRODSAccessObject {

	/**
	 * Execute a script remotely and return the results as an InputStream.
	 * 
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the command name. Do not provide
	 *            input arguments here.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> with the arguments for the command.
	 * @return <code>InputStream<code> with the reults of the command invocation.
	 * @throws JargonException
	 */
	InputStream executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand) throws JargonException;

	/**
	 * Execute a script remotely on the given host. Note that a
	 * <code>JargonException</code> will occur if the host does not exist.
	 * 
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the command name. Do not provide
	 *            input arguments here.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> with the arguments for the command.
	 * @param executionHost
	 *            <code>String</code> with the name of the host on which to run
	 *            the command
	 * @return <code>InputStream<code> with the reults of the command invocation.
	 * @throws JargonException
	 */
	InputStream executeARemoteCommandAndGetStreamGivingCommandNameAndArgsAndHost(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand, final String executionHost)
			throws JargonException;

	/**
	 * Execute a script remotely. Find the server to run the command on by
	 * finding the host that contains the file with the given iRODS absolute
	 * path. Note that if the file is not found, an empty stream will be
	 * returned. <code>null</code> is never returned.
	 * 
	 * @param commandToExecuteWithoutArguments
	 *            <code>String</code> with the command name. Do not provide
	 *            input arguments here.
	 * @param argumentsToPassWithCommand
	 *            <code>String</code> with the arguments for the command.
	 * @param absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn
	 *            <code>String</code> with the absolute path to an iRODS file
	 *            used to find the correct host.
	 * @return <code>InputStream<code> with the reults of the command invocation.  Empty buffer if file was not found.
	 * @throws JargonException
	 */
	InputStream executeARemoteCommandAndGetStreamUsingAnIRODSFileAbsPathToDetermineHost(
			final String commandToExecuteWithoutArguments,
			final String argumentsToPassWithCommand,
			final String absolutePathOfIrodsFileThatWillBeUsedToFindHostToExecuteOn)
			throws JargonException;

}