/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.CatalogSQLException;
import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileIntegrityException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.exception.NoAPIPrivException;
import org.irods.jargon.core.exception.NoMoreRulesException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.exception.RemoteScriptExecutionException;
import org.irods.jargon.core.exception.SpecificQueryException;
import org.irods.jargon.core.protovalues.ErrorEnum;

/**
 * This object is interposed in the process of interpreting the iRODS responses
 * to various protocol operations. The job of this class is to inspect the iRODS
 * response, and to detect and throw appropriate Exceptions based on the status
 * of the iRODS response. Specifically, this object detects iRODS error codes in
 * the 'intInfo' part of the response, and maps them to Jargon exceptions.
 * <p/>
 * Note that this is an early implementation, and a fuller error hierarchy will
 * develop over time.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSErrorScanner {

	public static void inspectAndThrowIfNeeded(final int infoValue)
			throws JargonException {

		if (infoValue == 0) {
			return;
		}

		ErrorEnum errorEnum;

		try {
			errorEnum = ErrorEnum.valueOf(infoValue);
		} catch (IllegalArgumentException ie) {
			throw new JargonException(
					"error code received from iRODS, not in ErrorEnum translation table:"
							+ infoValue, infoValue);
		}

		// non-zero value, create appropriate exception

		switch (errorEnum) {
		case OVERWITE_WITHOUT_FORCE_FLAG:
			throw new JargonFileOrCollAlreadyExistsException(
					"Attempt to overwrite file without force flag.", infoValue);
		case CAT_INVALID_AUTHENTICATION:
			throw new AuthenticationException("AuthenticationException",
					infoValue);
		case CAT_INVALID_USER:
			throw new InvalidUserException("InvalidUserException");
		case SYS_NO_API_PRIV:
			throw new NoAPIPrivException(
					"User lacks privileges to invoke the given API");
		case CAT_NO_ROWS_FOUND:
			throw new DataNotFoundException("No data found");
		case CAT_NAME_EXISTS_AS_COLLECTION:
			throw new JargonFileOrCollAlreadyExistsException(
					"Collection already exists", infoValue);
		case CAT_NAME_EXISTS_AS_DATAOBJ:
			throw new JargonFileOrCollAlreadyExistsException(
					"Attempt to overwrite file without force flag", infoValue);
		case CATALOG_ALREADY_HAS_ITEM_BY_THAT_NAME:
			throw new DuplicateDataException(
					"Catalog already has item by that name");
		case USER_CHKSUM_MISMATCH:
			throw new FileIntegrityException(
					"File checksum verification mismatch");
		case CAT_UNKNOWN_FILE:
			throw new DataNotFoundException("Unknown file");
		case CAT_UNKNOWN_COLLECTION:
			throw new DataNotFoundException("Unknown collection");
		case CAT_COLLECTION_NOT_EMPTY:
			throw new CollectionNotEmptyException("Collection not empty",
					infoValue);
		case EXEC_CMD_ERROR:
			throw new RemoteScriptExecutionException(
					"Remote script execution error" + infoValue);
		case USER_FILE_DOES_NOT_EXIST:
			throw new FileNotFoundException("File not found", infoValue);
		case CAT_INVALID_GROUP:
			throw new InvalidGroupException("Invalid iRODS group", infoValue);
		case CAT_NO_ACCESS_PERMISSION:
			throw new CatNoAccessException("No access to item in catalog");
		case COLLECTION_NOT_EMPTY:
			throw new CollectionNotEmptyException("The collection is not empty");
		case USER_NO_RESC_INPUT_ERR:
			throw new NoResourceDefinedException("No resource defined");
		case NO_MORE_RULES_ERR:
			throw new NoMoreRulesException("No more rules");
		case CAT_SQL_ERR:
			throw new CatalogSQLException("Catalog sql error");
		case SPECIFIC_QUERY_EXCEPTION:
			throw new SpecificQueryException("Exception processing specific query", infoValue);
		default:
			throw new JargonException("error code recieved from iRODS:"
					+ infoValue, infoValue);
		}

	}

}
