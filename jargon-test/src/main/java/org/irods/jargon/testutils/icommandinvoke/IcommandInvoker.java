/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.TestingUtilsException;
import org.irods.jargon.testutils.icommandinvoke.icommands.Icommand;

/**
 * Invoke an icommand on a provided irods server
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/16/2009
 */
public class IcommandInvoker {
	private IrodsInvocationContext irodsInvocationContext;
	private TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	/**
	 * Default (no-values) constructor
	 */
	public IcommandInvoker() {
	}

	/**
	 * Constructor that sets the <code>IrodsInvocationContext</code>
	 * 
	 * @param irodsInvocationContext
	 */
	public IcommandInvoker(final IrodsInvocationContext irodsInvocationContext) {
		this();
		this.irodsInvocationContext = irodsInvocationContext;
	}

	/**
	 * @return the irodsInvocationContext
	 */
	public IrodsInvocationContext getIrodsInvocationContext() {
		return irodsInvocationContext;
	}

	/**
	 * @param irodsInvocationContext
	 *            the irodsInvocationContext to set
	 */
	public void setIrodsInvocationContext(
			final IrodsInvocationContext irodsInvocationContext) {
		this.irodsInvocationContext = irodsInvocationContext;
	}

	/**
	 * @param icommand
	 *            <code>ICommand</code> object that contains specification of
	 *            the particular operation
	 * @return <code>InputStream</code> containing the response to the command
	 * @throws IcommandException
	 *             if an error occurs in command invocation, with details in the
	 *             error message
	 */
	private InputStream invoke(final Icommand icommand)
			throws IcommandException {
		/*
		 * set irods enviroment variables like so
		 * (https://www.irods.org/index.php/user_environment)
		 * 
		 * irodsHost 'zuri.sdsc.edu' irodsPort 1378 irodsDefResource=demoResc
		 * irodsHome=/tempZone/home/rods irodsCwd=/tempZone/home/rods
		 * irodsUserName 'rods' irodsZone 'tempZone'
		 */

		ProcessBuilder pb = new ProcessBuilder(icommand.buildCommand());
		Process p = null;
		BufferedInputStream bis;
		BufferedInputStream errStream = null;

		try {
			pb.directory(new File(irodsInvocationContext
					.getLocalWorkingDirectory()));

			p = pb.start();
			bis = new BufferedInputStream(p.getInputStream());
			errStream = new BufferedInputStream(p.getErrorStream());

			StringBuilder errData = new StringBuilder();

			int aChar = 0;

			while ((aChar = errStream.read()) >= 0) {
				errData.append((char) aChar);
			}

			if (errData.length() > 0) {
				if (errData.indexOf("CAT_SUCCESS") > -1) {
					// this is an ok status
				} else {
					StringBuilder message = new StringBuilder();
					message.append("error executing icommand:");
					message.append(errData);
					throw new IcommandException(message.toString());
				}
			}
		} catch (IOException ioe) {
			throw new IcommandException("error invoking icommand", ioe);
		} finally {
			try {
				p.waitFor();
				p.getOutputStream().close();
			} catch (Exception e) {
			}
			if (errStream != null) {
				try {
					errStream.close();
				} catch (Exception e) {
					// ignore
				}
			}

		}

		return bis;
	}

	/**
	 * @param icommand
	 *            <code>ICommand</code> object that contains specification of
	 *            the particular operation
	 * @return <code>String</code> containing the response to the command
	 * @throws IcommandException
	 *             if an error occurs in command invocation, with details in the
	 *             error message
	 */
	public String invokeCommandAndGetResultAsString(final Icommand icommand)
			throws IcommandException {

		String result = "";

		String osType = System.getProperty("os.name");
		if (osType.equals("Mac OS X")) {
			result = invokeViaExecutor(icommand);
		} else {
			result = invokeViaProcessBuilder(icommand);
		}

		return result;
	}

	protected String invokeViaExecutor(final Icommand icommand)
			throws IcommandException {
		String result = "";

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayOutputStream bosErrors = new ByteArrayOutputStream();
		List<String> commands = icommand.buildCommand();
		if (commands.size() <= 0) {
			throw new IcommandException(
					"improperly formatted command, no executable provided");
		}

		StringBuilder icommandFullPath = new StringBuilder();

		try {
			Properties testingProperties = testingPropertiesHelper
					.getTestProperties();
			icommandFullPath.append(testingProperties
					.getProperty(TestingPropertiesHelper.MAC_ICOMMANDS_PATH));
		} catch (TestingUtilsException e1) {
			e1.printStackTrace();
			throw new IcommandException(
					"property for icommand path needs to be set in testing properties",
					e1);
		}

		icommandFullPath.append(commands.get(0));

		CommandLine cl = new CommandLine(icommandFullPath.toString());

		for (int i = 1; i < commands.size(); i++) {
			cl.addArgument(commands.get(i));
		}

		int exitValue = 0;
		try {
			exitValue = 0;
			PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(bos,
					bosErrors);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(pumpStreamHandler);
			exitValue = executor.execute(cl);
		} catch (ExecuteException e) {
			throw new IcommandException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IcommandException(e);
		}

		if (exitValue != 0) {
		}

		// now get the output of the command
		result = bos.toString();
		String errors = bosErrors.toString();

		if (errors.length() > 0) {
			if (errors.indexOf("CAT_SUCCESS") > -1) {
				// this is an ok status
			} else {
				StringBuilder message = new StringBuilder();
				message.append("error executing icommand:");
				message.append(errors);
				throw new IcommandException(message.toString());
			}
		}

		return result;

	}

	protected String invokeViaProcessBuilder(final Icommand icommand)
			throws IcommandException {
		StringBuilder resultBuilder = new StringBuilder();

		BufferedInputStream bis = null;

		try {
			bis = new BufferedInputStream(invoke(icommand));

			int aChar = 0;

			while ((aChar = bis.read()) >= 0) {
				resultBuilder.append((char) aChar);
			}
		} catch (IOException ioe) {
			throw new IcommandException("error invoking icommand", ioe);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

		return resultBuilder.toString();
	}
}
