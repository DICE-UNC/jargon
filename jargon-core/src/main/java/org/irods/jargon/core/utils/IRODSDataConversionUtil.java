/**
 * 
 */
package org.irods.jargon.core.utils;

import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_HAAW;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_LINK;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_MOUNT;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_MSSO;
import static org.irods.jargon.core.packinstr.DataObjInpForMcoll.COLL_TYPE_TAR;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;

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
	 * @return <code>java.util.Date</code> reflecting the IRODS time, or
	 *         <code>null</code> if no date in the data
	 */
	public static Date getDateFromIRODSValue(final String irodsValue) {

		if (irodsValue == null) {
			throw new IllegalArgumentException("null date value");
		}

		if (irodsValue.isEmpty()) {
			return null;
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

	/**
	 * Utility to determine the collection type contained in an iRODS value.
	 * Null and empty values are mapped to the normal type.
	 * 
	 * @param irodsValue
	 *            <code>String</code> containing an IRODS collection type value
	 *            as returned from a query to ICAT. May be null.
	 * 
	 * @return the collection type
	 */
	public static SpecColType getCollectionTypeFromIRODSValue(
			final String irodsValue) {

		if (irodsValue == null || irodsValue.isEmpty()) {
			return SpecColType.NORMAL;
		}

		if (irodsValue.equals(COLL_TYPE_LINK)) {
			return SpecColType.LINKED_COLL;
		}

		if (irodsValue.equals(COLL_TYPE_MOUNT)) {
			return SpecColType.MOUNTED_COLL;
		}

		if (irodsValue.equals(COLL_TYPE_HAAW)
				|| irodsValue.equals(COLL_TYPE_TAR)
				|| irodsValue.equals(COLL_TYPE_MSSO)) {
			return SpecColType.STRUCT_FILE_COLL;
		}

		throw new IllegalArgumentException("unknown iRODS collection type: "
				+ irodsValue);
	}

	public static String escapeSingleQuotes(final String inputString) {
		if (inputString == null) {
			throw new IllegalArgumentException("null inputString");
		}

		return inputString.replace("'", "\\'");

	}

}
