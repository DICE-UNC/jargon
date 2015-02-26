/**
 *
 */
package org.irods.jargon.core.utils;

import java.util.Properties;

/**
 * Utilities that help derive data from properties in usable formats
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PropertyUtils {

	/**
	 * Verify the given property exists and return as a <code>String</code>
	 *
	 * @param properties
	 * @param propKey
	 * @return
	 */
	public static String verifyPropExistsAndGetAsString(
			final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = properties.getProperty(propKey).trim();
		if (propVal == null) {
			throw new IllegalArgumentException(propKey
					+ " not set in jargon.properties");
		}
		return propVal;
	}

	/**
	 * Verify the given property exists and return as an <code>int</code>
	 *
	 * @param propKey
	 * @return
	 * @throws
	 */
	public static int verifyPropExistsAndGetAsInt(final Properties properties,
			final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = verifyPropExistsAndGetAsString(properties, propKey);

		try {
			return Integer.parseInt(propVal);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("prop " + propKey
					+ "did not result in an int value, was:" + propVal);
		}

	}

	/**
	 * Verify the property exists and get as a <code>boolean</code> value
	 *
	 * @param properties
	 * @param propKey
	 * @return
	 */
	public static boolean verifyPropExistsAndGetAsBoolean(
			final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = verifyPropExistsAndGetAsString(properties, propKey);
		return Boolean.parseBoolean(propVal);

	}

	/**
	 * Verify the property exists and get as a <code>long</code>
	 *
	 * @param properties
	 * @param propKey
	 * @return
	 */
	public static long verifyPropExistsAndGetAsLong(
			final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = verifyPropExistsAndGetAsString(properties, propKey);

		try {
			return Long.parseLong(propVal);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("prop " + propKey
					+ "did not result in a long value, was:" + propVal);
		}

	}
}
