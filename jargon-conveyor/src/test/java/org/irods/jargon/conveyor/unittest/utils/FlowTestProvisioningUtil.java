/**
 * 
 */
package org.irods.jargon.conveyor.unittest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.core.exception.JargonException;

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

}
