/**
 * 
 */
package org.irods.jargon.conveyor.gridaccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

/**
 * Helper class can read a file of pre-seeded iRODS accounts in a , delimited
 * format from a text file and can create a set of <code>IRODSAccount</code>
 * objects that can be processed.
 * <p/>
 * This class can also take a set of <code>IRODSAccount</code> objects and
 * serialize them into a text file
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GridAccountConfigurationProcessor {

	public static final String DELIM = ",";
	public static final String COMMENT = "#";

	/**
	 * Take a list of <code>IRODSAccount</code> and serialize into a text file
	 * 
	 * @param gridAccountFile
	 *            <code>File</code> to which the accounts will serialized (no
	 *            password information is saved)
	 * @param irodsAccounts
	 *            <code>List</code> of {@link IRODSAccount}
	 * @throws GridAccountConfigurationException
	 */
	public static void serializeIRODSAccountListToFile(
			final File gridAccountFile, final List<IRODSAccount> irodsAccounts)
			throws GridAccountConfigurationException {

		if (gridAccountFile == null) {
			throw new IllegalArgumentException("null gridAccountFile");
		}

		if (irodsAccounts == null) {
			throw new IllegalArgumentException("null irodsAccounts");
		}

		try {
			gridAccountFile.delete();
			PrintWriter out = new PrintWriter(gridAccountFile);

			for (IRODSAccount irodsAccount : irodsAccounts) {
				out.println(buildLineForAccount(irodsAccount));
			}
			out.flush();
			out.close();

		} catch (IOException e) {
			throw new GridAccountConfigurationException(
					"unable to write out to grid account file", e);
		}
	}

	public static List<IRODSAccount> deserializeIRODSAccountListFromFile(
			final File gridAccountFile)
			throws GridAccountConfigurationException {

		if (gridAccountFile == null) {
			throw new IllegalArgumentException("null gridAccountFile");
		}

		if (!gridAccountFile.exists()) {
			throw new GridAccountConfigurationException(
					"gridAccountFile does not exist");
		}

		List<IRODSAccount> irodsAccounts = new ArrayList<IRODSAccount>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					gridAccountFile));
			String line;
			IRODSAccount irodsAccount = null;
			while ((line = br.readLine()) != null) {
				irodsAccount = buildAccountForLine(line);
				// tolerate null or blank lines
				if (irodsAccount != null) {
					irodsAccounts.add(buildAccountForLine(line));
				}
			}
			br.close();
			return irodsAccounts;
		} catch (IOException e) {
			throw new GridAccountConfigurationException(
					"unable to write out to grid account file", e);
		}

	}

	/**
	 * Given a <code>String</code> reflecting a line in a preset file, return the associated 
	 * <code>IRODSAccount</code> if one can be found.  This method will return <code>null</code> if
	 * the line is blank, or if it is a comment (prepended by a #).
	 * <p/>
	 * The caller must check for nulls
	 * 
	 * @param line <code>String</code> with a line from a serialized file
	 * @return {@link IRODSAccount} or <code>null</code>
	 * @throws GridAccountConfigurationException
	 */
	private static IRODSAccount buildAccountForLine(String line) throws GridAccountConfigurationException {
		
		if (line == null || line.isEmpty()) {
			return null;
		}
		
		// lines that start with # are treated as comments and ignored
		if (line.startsWith(COMMENT)) {
			return null;
		}
		
		String[] elements = line.split("[" + DELIM + "]");
		
		if (elements.length != 7) {
			throw new GridAccountConfigurationException("unexpected number of parameters in line");
		}
		
		try {
			return IRODSAccount.instance(elements[0], 
					Integer.parseInt(elements[1]), 
					elements[3], 
					"", // no password here 
					elements[6], 
					elements[2], 
					elements[5],
					AuthScheme.findTypeByString(elements[4]));
		} catch (NumberFormatException e) {
			throw new GridAccountConfigurationException(
					"invalid port in position 1", e);
		} catch (JargonException e) {
			throw new GridAccountConfigurationException(
					"error creating IRODSAccount", e);
		}

	}

	/**
	 * Turn an <code>IRODSAccount</code> into a | delim String
	 * 
	 * @param irodsAccount
	 * @return
	 */
	private static String buildLineForAccount(final IRODSAccount irodsAccount) {
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAccount.getHost());
		sb.append(DELIM);
		sb.append(irodsAccount.getPort());
		sb.append(DELIM);
		sb.append(irodsAccount.getZone());
		sb.append(DELIM);
		sb.append(irodsAccount.getUserName());
		sb.append(DELIM);
		sb.append(irodsAccount.getAuthenticationScheme());
		sb.append(DELIM);
		sb.append(irodsAccount.getDefaultStorageResource());
		sb.append(DELIM);
		sb.append(irodsAccount.getHomeDirectory());
		return sb.toString();
	}

}
