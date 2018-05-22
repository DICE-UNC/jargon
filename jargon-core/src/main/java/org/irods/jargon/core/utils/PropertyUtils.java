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
	 * Verify the given property exists and return as a {@code String}
	 *
	 * @param properties
	 *            {@link Properties} to inspect
	 * @param propKey
	 *            {@link String} with the key to retrieve
	 * @return {@link String} with the property value that should exist
	 */
	public static String verifyPropExistsAndGetAsString(final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = properties.getProperty(propKey).trim();
		if (propVal == null) {
			throw new IllegalArgumentException(propKey + " not set in jargon.properties");
		}
		return propVal;
	}

	/**
	 * Verify the given property exists and return as an {@code int}
	 *
	 * @param properties
	 *            {@link Properties} to verify
	 * @param propKey
	 *            {@link String} with the key to retrieve
	 * 
	 * @return <code>int</code> with the property as an int
	 */
	public static int verifyPropExistsAndGetAsInt(final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = verifyPropExistsAndGetAsString(properties, propKey);

		try {
			return Integer.parseInt(propVal);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("prop " + propKey + "did not result in an int value, was:" + propVal);
		}

	}

	/**
	 * Verify the property exists and get as a {@code boolean} value
	 *
	 * @param properties
	 *            {@link Properties} to inspect
	 * @param propKey
	 *            propKey {@link String} with the key to retrieve
	 * @return <code>boolean</code> with the property value
	 */
	public static boolean verifyPropExistsAndGetAsBoolean(final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = verifyPropExistsAndGetAsString(properties, propKey);
		return Boolean.parseBoolean(propVal);

	}

	/**
	 * Verify the property exists and get as a {@code long}
	 *
	 * @param properties
	 *            {@link Properties} to inspect
	 * @param propKey
	 *            propKey {@link String} with the key to retrieve
	 * @return <code>long</code> with the property value
	 */
	public static long verifyPropExistsAndGetAsLong(final Properties properties, final String propKey) {

		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}

		String propVal = verifyPropExistsAndGetAsString(properties, propKey);

		try {
			return Long.parseLong(propVal);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("prop " + propKey + "did not result in a long value, was:" + propVal);
		}

	}
}
