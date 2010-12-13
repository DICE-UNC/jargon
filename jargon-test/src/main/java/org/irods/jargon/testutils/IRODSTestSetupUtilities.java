/**
 *
 */
package org.irods.jargon.testutils;

import static org.irods.jargon.testutils.TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY;

import java.util.Properties;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import org.irods.jargon.testutils.icommandinvoke.IcommandInvoker;
import org.irods.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import org.irods.jargon.testutils.icommandinvoke.icommands.IlsCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.ImkdirCommand;
import org.irods.jargon.testutils.icommandinvoke.icommands.IrmCommand;


/**
 * Common utilities to prep the test irods for unit tests
 *
 *
 * @author Mike Conway, DICE (www.irods.org)
 *
 */
public class IRODSTestSetupUtilities {
	private TestingPropertiesHelper testingPropertiesHelper;
	private Properties testingProperties;

	public IRODSTestSetupUtilities() throws TestingUtilsException {
		testingPropertiesHelper = new TestingPropertiesHelper();
		testingProperties = testingPropertiesHelper.getTestProperties();
	}

	/**
	 * Remove the scratch directory from irods based on the testing.properties
	 * file
	 *
	 * @throws TestingUtilsException
	 */
	public void clearIrodsScratchDirectory() throws TestingUtilsException {
		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);

		try {
			// do an ils and see if scratch dir is there, if so, delete it
			IlsCommand ilsCommand = new IlsCommand();
			ilsCommand.setIlsBasePath(testingPropertiesHelper
					.buildIRODSCollectionAbsolutePathFromTestProperties(
							testingProperties, ""));

			String ilsResult = "";
			try {

				ilsResult = invoker
						.invokeCommandAndGetResultAsString(ilsCommand);
			} catch (IcommandException ice) {
				if (ice.getMessage().indexOf("does not exist") > -1) {
					// result will stay blank, this is ok
				} else {
					throw new IcommandException(ice);
				}
			}

			if (ilsResult.indexOf("/"
					+ testingProperties.getProperty(IRODS_SCRATCH_DIR_KEY)) > 0) {
				IrmCommand irmCommand = new IrmCommand();
				irmCommand
						.setObjectName(invocationContext.getIrodsScratchDir());
				irmCommand.setForce(true);
				
				invoker.invokeCommandAndGetResultAsString(irmCommand);
			}

		} catch (IcommandException ice) {
			throw new TestingUtilsException(
					"error clearing irods scratch directory", ice);
		}
	}

	/**
	 * Clear and then create a fresh scratch directory in irods based on the
	 * testing.properties file
	 *
	 * @throws TestingUtilsException
	 */
	public void initializeIrodsScratchDirectory() throws TestingUtilsException {
		clearIrodsScratchDirectory();

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		String scratchPath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, "");
		imkdirCommand.setCollectionName(scratchPath);

		try {
			invoker.invokeCommandAndGetResultAsString(imkdirCommand);
		} catch (IcommandException ice) {
			throw new TestingUtilsException(
					"error clearing irods scratch directory", ice);
		}
	}

	/**
	 * Create a directory under scratch with the given name, which is typically
	 * a name assigned per Junit test class
	 *
	 * @param testingDirectory
	 *            <code>String</code> with a directory to go underneath scratch,
	 *            do not supply leading '/'
	 * @throws TestingUtilsException
	 */
	public void initializeDirectoryForTest(String testingDirectory)
			throws TestingUtilsException {
		StringBuilder scratchDir = new StringBuilder();
		scratchDir.append(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, ""));
		scratchDir.append('/');
		scratchDir.append(testingDirectory);

		IrodsInvocationContext invocationContext = testingPropertiesHelper
				.buildIRODSInvocationContextFromTestProperties(testingProperties);
		IcommandInvoker invoker = new IcommandInvoker(invocationContext);
		ImkdirCommand imkdirCommand = new ImkdirCommand();
		imkdirCommand.setCollectionName(scratchDir.toString());
		try {
			invoker.invokeCommandAndGetResultAsString(imkdirCommand);
		} catch (IcommandException ice) {
			throw new TestingUtilsException(
					"error creating per test directory", ice);
		}
	}
	

}
