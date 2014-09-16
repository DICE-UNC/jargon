/**
 * 
 */
package org.irods.jargon.conveyor.unittest.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.LocalFileUtils;

/**
 * Utilities to provision flow directories for various unit tests
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowTestProvisioningUtil {

	public static Properties retrieveTransferProperties()
			throws JargonException {

		ClassLoader loader = FlowTestProvisioningUtil.class.getClassLoader();
		InputStream in = loader.getResourceAsStream("transfer.properties");
		Properties properties = new Properties();

		try {
			properties.load(in);
		} catch (IOException ioe) {
			throw new JargonException("error loading test properties", ioe);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
		return properties;
	}

	/**
	 * Given a resource name (for test/resources) clear out the dsl dir and plug
	 * this file in as the only dsl.
	 * 
	 * @param transferProperties
	 * @param dslName
	 * @throws JargonException
	 */
	public static void clearAndProvisionTestDslDirecory(
			final Properties transferProperties, final String dslName)
			throws JargonException {

		if (transferProperties == null) {
			throw new IllegalArgumentException("no transfer.properties found");
		}

		if (dslName == null || dslName.isEmpty()) {
			throw new IllegalArgumentException("null dslName");
		}

		clearDslDirectory(transferProperties);

		File groovyFile = LocalFileUtils.getClasspathResourceAsFile(dslName);

		String dslDir = transferProperties.getProperty("flow.dir");
		if (dslDir == null || dslDir.isEmpty()) {
			throw new JargonException("no flow.dir specified");
		}

		String userHome = System.getProperty("user.home");
		userHome = LocalFileUtils.normalizePath(userHome);
		dslDir = dslDir.replaceAll("\\$\\{user.home\\}", userHome);

		File dslDirFile = new File(dslDir, groovyFile.getName());

		try {
			FileUtils.copyFile(groovyFile, dslDirFile);
		} catch (IOException e) {
			throw new JargonException("io exception deleting flow dir", e);
		}
	}

	/**
	 * Clear out and reinitialize the flow.dir directory specified in
	 * transfer.properties
	 * 
	 * @param transferProperties
	 * @throws JargonException
	 */
	public static void clearDslDirectory(final Properties transferProperties)
			throws JargonException {

		if (transferProperties == null) {
			throw new JargonException("no transfer.properties found");
		}

		String dslDir = transferProperties.getProperty("flow.dir");
		String userHome = System.getProperty("user.home");
		userHome = LocalFileUtils.normalizePath(userHome);
		dslDir = dslDir.replaceAll("\\$\\{user.home\\}", userHome);

		if (dslDir == null || dslDir.isEmpty()) {
			throw new JargonException("no flow.dir specified");
		}

		File dslDirFile = new File(dslDir);

		try {
			FileUtils.deleteDirectory(dslDirFile);
			dslDirFile.mkdirs();
		} catch (IOException e) {
			throw new JargonException("io exception deleting flow dir", e);
		}

	}

}
