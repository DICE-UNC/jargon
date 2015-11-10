/**
 *
 */
package org.irods.jargon.core.connection;

/**
 * Handy place to put common constants for connection-related purposes
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ConnectionConstants {

	/**
	 * Approximate maximum number of bytes transfered by each thread during a
	 * parallel transfer.
	 */
	public static final int TRANSFER_THREAD_SIZE = 6000000;

	/**
	 * value used to detect and respond to delete status messages
	 */
	public static final int SYS_CLI_TO_SVR_COLL_STAT_REPLY = 99999997;

	/**
	 * number of deletes that will be done in IRODS before sending a status
	 * message
	 */
	public static final int SYS_CLI_TO_SVR_COLL_STAT_SIZE = 10;

	/**
	 * Minimum size of a file to be subject to restart processing in get/put
	 * transfers
	 */
	public static final long MIN_FILE_RESTART_SIZE = 1 * 1024 * 1024; // FIXME:
	// artificially
	// low
	// 64 *
	// 1024
	// *
	// 1024;

	/**
	 * Maximum number of restart attempts before failing in large file restart
	 * TODO: add to jargon props
	 */
	public static final int MAX_FILE_RESTART_ATTEMPTS = 4;

	/**
	 * Maximum threads to open for a parallel transfer. More than this usually
	 * won't help, might even be slower.
	 */
	public static final int MAX_THREAD_NUMBER = 16;

	/**
	 * 16 bit char
	 */
	public static final int CHAR_LENGTH = 2;

	/**
	 * 16 bit short
	 */
	public static final int SHORT_LENGTH = 2;

	/**
	 * 32 bit integer
	 */
	public static final int INT_LENGTH = 4;

	/**
	 * 64 bit long
	 */
	public static final int LONG_LENGTH = 8;

	/**
	 * 4 bytes at the front of the header, outside XML
	 */
	public static final int HEADER_INT_LENGTH = 4;

	/**
	 * Maximum password length. Used in challenge response.
	 */
	public static final int MAX_PASSWORD_LENGTH = 50;
	/**
	 * Standard challenge length. Used in challenge response.
	 */
	public static final int CHALLENGE_LENGTH = 64;

	public static final long MAX_SZ_FOR_SINGLE_BUF = (32 * 1024 * 1024);

	/**
	 * Maximum size of an iRODS absolute path
	 */
	public static final int MAX_PATH_SIZE = 1024;

	/**
	 * Set to true to add gen query out xml protocol messages to debug log
	 */
	public static final boolean DUMP_GEN_QUERY_OUT = false;

	private ConnectionConstants() {
	}

}
