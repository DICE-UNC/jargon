/**
 * 
 */
package org.irods.jargon.core.utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Handle iRODS representations of data from GenQuery and translation into
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSDataConversionUtil {

	private IRODSDataConversionUtil() {
	}

	/**
	 * For a given string that should contain an integer value, return either
	 * the <code>int</code> value or a zero if empty.
	 * 
	 * @param irodsValue
	 * @return <code>int</code> equivilent of irods value
	 */
	public static int getIntOrZeroFromIRODSValue(final String irodsValue) {

		int result = 0;

		if (irodsValue == null) {
			throw new IllegalArgumentException("null irodsValue");
		}

		if (irodsValue.isEmpty()) {
			// nothing
		} else {
			try {
				result = Integer.parseInt(irodsValue);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("cannot format number:"
						+ irodsValue, nfe);
			}
		}

		return result;

	}

	/**
	 * For a given string that should contain an long value, return either the
	 * <code>long</code> value or a zero if empty.
	 * 
	 * @param irodsValue
	 * @return <code>long</code> equivilent of irods value
	 */
	public static long getLongOrZeroFromIRODSValue(final String irodsValue) {

		long result = 0L;

		if (irodsValue == null) {
			throw new IllegalArgumentException("null irodsValue");
		}

		if (irodsValue.isEmpty()) {
			// nothing
		} else {
			try {
				result = Long.parseLong(irodsValue);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("cannot format number:"
						+ irodsValue, nfe);
			}
		}

		return result;

	}

	/**
	 * Utility to return an irods date value as a <code>java.util.Date</code>
	 * 
	 * @param irodsValue
	 *            <code>String</code> containing an IRODS date value as returned
	 *            from a query to ICAT
	 * @return <code>java.util.Date</code> reflecting the IRODS time
	 */
	public static Date getDateFromIRODSValue(final String irodsValue) {

		if (irodsValue == null) {
			throw new IllegalArgumentException("null date value");
		}

		if (irodsValue.isEmpty()) {
			return new Date();
		}

		Integer dateInteger;

		try {
			dateInteger = Integer.parseInt(irodsValue);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(
					"malformed date value, cannot translate to integer:"
							+ irodsValue);
		}

		TimeZone timeZone = TimeZone.getTimeZone("GMT");
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		dateFormat.setTimeZone(timeZone);
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(0L);
		calendar.add(Calendar.SECOND, dateInteger.intValue());
		Date computedDate = calendar.getTime();
		return computedDate;
	}

	public static String escapeSingleQuotes(final String inputString) {
		if (inputString == null) {
			throw new IllegalArgumentException("null inputString");
		}

		return inputString.replace("'", "\\'");

	}

}
