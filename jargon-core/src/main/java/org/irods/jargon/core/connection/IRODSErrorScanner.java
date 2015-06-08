/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.CatalogSQLException;
import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.CollectionNotMountedException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileDriverError;
import org.irods.jargon.core.exception.FileIntegrityException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InternalIrodsOperationException;
import org.irods.jargon.core.exception.InvalidArgumentException;
import org.irods.jargon.core.exception.InvalidClientUserException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidInputParameterException;
import org.irods.jargon.core.exception.InvalidResourceException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.exception.KeyException;
import org.irods.jargon.core.exception.NegotiationException;
import org.irods.jargon.core.exception.NoAPIPrivException;
import org.irods.jargon.core.exception.NoMoreRulesException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.exception.RemoteScriptExecutionException;
import org.irods.jargon.core.exception.ResourceDoesNotExistException;
import org.irods.jargon.core.exception.ResourceHierarchyException;
import org.irods.jargon.core.exception.SpecificQueryException;
import org.irods.jargon.core.exception.UnixFileCreateException;
import org.irods.jargon.core.exception.UnixFileMkdirException;
import org.irods.jargon.core.exception.UnixFileRenameException;
import org.irods.jargon.core.exception.ZoneUnavailableException;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final Logger log = LoggerFactory
			.getLogger(IRODSErrorScanner.class);

	/**
	 * Scan the response for errors, and incorporate any message information
	 * that might expand the error
	 *
	 * @param infoValue
	 *            <code>int</code> with the iRODS info value from a packing
	 *            instruction response header
	 * @param message
	 *            <code>String</code> with any additional error information
	 *            coming from the response in the <code>msg</code> field of the
	 *            header
	 * @throws JargonException
	 */
	public static void inspectAndThrowIfNeeded(final int infoValue,
			String message) throws JargonException {

		log.debug("inspectAndThrowIfNeeded:{}", infoValue);

		if (infoValue == 0) {
			return;
		}

		if (message == null) {
			message = "";
		}

		// non-zero value, create appropriate exception, first try some ranges
		// (especially for unix file system exceptions, which can have subcodes
		if (infoValue <= -511000 && infoValue >= -511199) {
			throw new UnixFileCreateException(
					"Exception creating file in file system", infoValue);
		} else if (infoValue >= -520013 && infoValue <= -520000) {
			throw new UnixFileMkdirException("Exception making unix directory",
					infoValue);
		} else if (infoValue >= -528036 && infoValue <= -528000) {
			throw new UnixFileRenameException(
					"Exception renaming file in file system", infoValue);

		}

		ErrorEnum errorEnum;

		try {
			log.debug("scanning for info value...");

			try {

				errorEnum = ErrorEnum.valueOf(infoValue);

			} catch (IllegalArgumentException iae) {
				throw new JargonException(
						"Unknown iRODS exception code recieved:" + infoValue,
						infoValue);
			}

			log.debug("errorEnum val:{}", errorEnum);
		} catch (IllegalArgumentException ie) {
			log.error("error getting error enum value", ie);
			throw new JargonException(
					"error code received from iRODS, not in ErrorEnum translation table:"
							+ infoValue, infoValue);
		}

		checkSpecificCodesAndThrowIfExceptionLocated(infoValue, message,
				errorEnum);
	}

	/**
	 * @param infoValue
	 * @param message
	 * @param errorEnum
	 * @throws JargonException
	 *             or specific child exception of <code>JargonException</code>
	 */
	private static void checkSpecificCodesAndThrowIfExceptionLocated(
			final int infoValue, final String message, final ErrorEnum errorEnum)
					throws JargonException {
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
			throw new FileNotFoundException("Unknown file");
		case CAT_UNKNOWN_COLLECTION:
			throw new FileNotFoundException("Unknown collection");
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
			throw new NoMoreRulesException("no more rules");
		case COLLECTION_NOT_MOUNTED:
			throw new CollectionNotMountedException("collection not mounted");
		case UNIX_FILE_OPENDIR_ERR:
			throw new FileDriverError("file driver error", infoValue);
		case CAT_SQL_ERR:
			throw new CatalogSQLException("Catalog SQL error");
		case SPECIFIC_QUERY_EXCEPTION:
			throw new SpecificQueryException(
					"Exception processing specific query", infoValue);
		case CAT_INVALID_ARGUMENT:
			throw new InvalidArgumentException(message, infoValue);

		case CAT_INVALID_RESOURCE:
			throw new InvalidResourceException(message, infoValue);
		case FEDERATED_ZONE_NOT_AVAILABLE:
			throw new ZoneUnavailableException(
					"the federated zone is not available");
		case SYS_MOUNT_MOUNTED_COLL_ERR:
			throw new CollectionNotMountedException(
					"unable to mount collection, potential duplicate mount");
		case SYS_SPEC_COLL_OBJ_NOT_EXIST:
			throw new DataNotFoundException("Special collection not found");
		case PAM_AUTH_ERROR:
			throw new AuthenticationException("PAM authentication error");
		case INVALID_INPUT_PARAM:
			throw new InvalidInputParameterException("Invalid input parameter");
		case CAT_INVALID_CLIENT_USER:
			throw new InvalidClientUserException(message);
		case KEY_NOT_FOUND:
			throw new KeyException(ErrorEnum.KEY_NOT_FOUND.toString(),
					ErrorEnum.KEY_NOT_FOUND.getInt());
		case KEY_TYPE_MISMATCH:
			throw new KeyException(ErrorEnum.KEY_TYPE_MISMATCH.toString(),
					ErrorEnum.KEY_TYPE_MISMATCH.getInt());
		case CHILD_EXISTS:
			throw new ResourceHierarchyException(
					ErrorEnum.CHILD_EXISTS.toString(),
					ErrorEnum.CHILD_EXISTS.getInt());
		case HIERARCHY_ERROR:
			throw new ResourceHierarchyException(
					ErrorEnum.HIERARCHY_ERROR.toString(),
					ErrorEnum.HIERARCHY_ERROR.getInt());
		case CHILD_NOT_FOUND:
			throw new ResourceHierarchyException(
					ErrorEnum.CHILD_NOT_FOUND.toString(),
					ErrorEnum.CHILD_NOT_FOUND.getInt());
		case NO_NEXT_RESOURCE_FOUND:
			throw new ResourceHierarchyException(
					ErrorEnum.NO_NEXT_RESOURCE_FOUND.toString(),
					ErrorEnum.NO_NEXT_RESOURCE_FOUND.getInt());
		case NO_PDMO_DEFINED:
			throw new ResourceHierarchyException(
					ErrorEnum.NO_PDMO_DEFINED.toString(),
					ErrorEnum.NO_PDMO_DEFINED.getInt());
		case INVALID_LOCATION:
			throw new ResourceHierarchyException(
					ErrorEnum.INVALID_LOCATION.toString(),
					ErrorEnum.INVALID_LOCATION.getInt());
		case PLUGIN_ERROR:
			throw new InternalIrodsOperationException(
					ErrorEnum.PLUGIN_ERROR.toString(),
					ErrorEnum.PLUGIN_ERROR.getInt());
		case INVALID_RESC_CHILD_CONTEXT:
			throw new ResourceHierarchyException(
					ErrorEnum.INVALID_RESC_CHILD_CONTEXT.toString(),
					ErrorEnum.INVALID_RESC_CHILD_CONTEXT.getInt());
		case INVALID_FILE_OBJECT:
			throw new ResourceHierarchyException(
					ErrorEnum.INVALID_FILE_OBJECT.toString(),
					ErrorEnum.INVALID_FILE_OBJECT.getInt());
		case INVALID_OPERATION:
			throw new InternalIrodsOperationException(
					ErrorEnum.INVALID_OPERATION.toString(),
					ErrorEnum.INVALID_OPERATION.getInt());
		case CHILD_HAS_PARENT:
			throw new ResourceHierarchyException(
					ErrorEnum.CHILD_HAS_PARENT.toString(),
					ErrorEnum.CHILD_HAS_PARENT.getInt());
		case FILE_NOT_IN_VAULT:
			throw new ResourceHierarchyException(
					ErrorEnum.FILE_NOT_IN_VAULT.toString(),
					ErrorEnum.FILE_NOT_IN_VAULT.getInt());
		case DIRECT_ARCHIVE_ACCESS:
			throw new ResourceHierarchyException(
					ErrorEnum.DIRECT_ARCHIVE_ACCESS.toString(),
					ErrorEnum.DIRECT_ARCHIVE_ACCESS.getInt());
		case ADVANCED_NEGOTIATION_NOT_SUPPORTED:
			throw new NegotiationException(
					ErrorEnum.ADVANCED_NEGOTIATION_NOT_SUPPORTED.toString(),
					ErrorEnum.ADVANCED_NEGOTIATION_NOT_SUPPORTED.getInt());
		case DIRECT_CHILD_ACCESS:
			throw new ResourceHierarchyException(
					ErrorEnum.DIRECT_CHILD_ACCESS.toString(),
					ErrorEnum.DIRECT_CHILD_ACCESS.getInt());
		case INVALID_DYNAMIC_CAST:
			throw new InternalIrodsOperationException(
					ErrorEnum.INVALID_DYNAMIC_CAST.toString(),
					ErrorEnum.INVALID_DYNAMIC_CAST.getInt());
		case INVALID_ACCESS_TO_IMPOSTOR_RESOURCE:
			throw new InternalIrodsOperationException(
					ErrorEnum.INVALID_ACCESS_TO_IMPOSTOR_RESOURCE.toString(),
					ErrorEnum.INVALID_ACCESS_TO_IMPOSTOR_RESOURCE.getInt());
		case INVALID_LEXICAL_CAST:
			throw new InternalIrodsOperationException(
					ErrorEnum.INVALID_LEXICAL_CAST.toString(),
					ErrorEnum.INVALID_LEXICAL_CAST.getInt());
		case CONTROL_PLANE_MESSAGE_ERROR:
			throw new InternalIrodsOperationException(
					ErrorEnum.CONTROL_PLANE_MESSAGE_ERROR.toString(),
					ErrorEnum.CONTROL_PLANE_MESSAGE_ERROR.getInt());
		case REPLICA_NOT_IN_RESC:
			throw new ResourceHierarchyException(
					ErrorEnum.REPLICA_NOT_IN_RESC.toString(),
					ErrorEnum.REPLICA_NOT_IN_RESC.getInt());
		case INVALID_ANY_CAST:
			throw new InternalIrodsOperationException(
					ErrorEnum.INVALID_ANY_CAST.toString(),
					ErrorEnum.INVALID_ANY_CAST.getInt());
		case BAD_FUNCTION_CALL:
			throw new InternalIrodsOperationException(
					ErrorEnum.BAD_FUNCTION_CALL.toString(),
					ErrorEnum.BAD_FUNCTION_CALL.getInt());
		case CLIENT_NEGOTIATION_ERROR:
			throw new NegotiationException(
					ErrorEnum.CLIENT_NEGOTIATION_ERROR.toString(),
					ErrorEnum.CLIENT_NEGOTIATION_ERROR.getInt());
		case SERVER_NEGOTIATION_ERROR:
			throw new NegotiationException(
					ErrorEnum.SERVER_NEGOTIATION_ERROR.toString(),
					ErrorEnum.SERVER_NEGOTIATION_ERROR.getInt());
		case SYS_RESC_DOES_NOT_EXIST:
			throw new ResourceDoesNotExistException(
					ErrorEnum.SYS_RESC_DOES_NOT_EXIST.toString(),
					ErrorEnum.SYS_RESC_DOES_NOT_EXIST.getInt());
		default:
			StringBuilder sb = new StringBuilder();
			if (message.isEmpty()) {
				sb.append("error code received from iRODS:");
				sb.append(infoValue);

				throw new JargonException(sb.toString(), infoValue);
			} else {
				sb.append("error code received from iRODS:");
				sb.append(infoValue);
				sb.append(" message:");
				sb.append(message);
				throw new JargonException(sb.toString(), infoValue);
			}
		}
	}

	/**
	 * Inspect the <code>info</code> value from an iRODS packing instruction
	 * response header and throw an exception if an error was detected
	 *
	 * @param infoValue
	 * @throws JargonException
	 */
	public static void inspectAndThrowIfNeeded(final int infoValue)
			throws JargonException {

		inspectAndThrowIfNeeded(infoValue, "");
	}

}
