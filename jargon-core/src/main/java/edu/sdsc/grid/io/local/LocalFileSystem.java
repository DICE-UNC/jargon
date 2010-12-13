//  Copyright (c) 2005, Regents of the University of California
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//    * Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//    * Neither the name of the University of California, San Diego (UCSD) nor
//  the names of its contributors may be used to endorse or promote products
//  derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//  FILE
//  LocalFileSystem.java  -  edu.sdsc.grid.io.local.LocalFileSystem
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralFileSystem
//              |
//              +-.LocalFileSystem
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.local;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.sdsc.grid.io.GeneralAccount;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralFileSystem;
import edu.sdsc.grid.io.MetaDataCondition;
import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.MetaDataSelect;

/**
 * The LocalFileSystem class is the class for connection implementations to the
 * local file systems. It was added to the GeneralFileSystem tree to provide
 * compatibility and support for remote metadata queries. Unfortunately, local
 * filesystems cannot actually be queried.
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 * @since Jargon1.0
 */
public class LocalFileSystem extends GeneralFileSystem {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	/**
	 * Default number of records returned by a query
	 */
	static final int DEFAULT_RECORDS_WANTED = 300;
	// TODO why is this even here?

	/**
	 * Debug setting
	 */
	static int DEBUG = GeneralFileSystem.DEBUG;

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Opens a socket connection to read from and write to. Loads the default
	 * Local user account information from their home directory. The account
	 * information stored in this object cannot be changed once constructed.
	 * <P>
	 * This constructor is provided for convenience however, it is recommended
	 * that all necessary data be sent to the constructor and not left to the
	 * defaults.
	 * 
	 * @throws FileNotFoundException
	 *             if the user data file cannot be found.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public LocalFileSystem() {
		this(new LocalAccount());
	}

	/**
	 * Opens a socket connection to read from and write to. Opens the account
	 * held in the LocalAccount object. The account information stored in this
	 * object cannot be changed once constructed.
	 * <P>
	 * This constructor is provided for convenience however, it is recommended
	 * that all necessary data be sent to the constructor and not left to the
	 * defaults.
	 * 
	 * @param localAccount
	 *            the Local account information object.
	 * @throws FileNotFoundException
	 *             if the user data file cannot be found.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public LocalFileSystem(final LocalAccount localAccount) {
		if (localAccount == null) {
			account = new LocalAccount();
		} else {
			account = (LocalAccount) localAccount.clone();
		}
	}

	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------
	// General
	/**
	 * Sets the <code>account</code> object.
	 */
	@Override
	protected void setAccount(GeneralAccount account) {
		if (account == null) {
			account = new LocalAccount();
		} else {
			this.account = (LocalAccount) account.clone();
		}
	}

	/**
	 * Returns the root directories of the local file system.
	 */
	@Override
	public String[] getRootDirectories() {
		GeneralFile[] roots = LocalFile.listRoots();
		String[] rootStrings = new String[roots.length];
		for (int i = 0; i < roots.length; i++) {
			rootStrings[i] = roots[i].toString();
		}

		return rootStrings;
	}

	// ----------------------------------------------------------------------
	// GeneralFileSystem methods
	// ----------------------------------------------------------------------
	/**
	 * Queries the file system to find all files that match a set of conditions.
	 * For all those that match, the fields indicated in the select array are
	 * returned in the result object.
	 */
	@Override
	public MetaDataRecordList[] query(final MetaDataCondition[] conditions,
			final MetaDataSelect[] selects) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Queries the file system to find all files that match a set of conditions.
	 * For all those that match, the fields indicated in the select array are
	 * returned in the result object.
	 * <P>
	 * While condition and select array objects have all been checked for
	 * self-consistency during their construction, there are additional problems
	 * that must be detected at query time:
	 * <P>
	 * <ul>
	 * <li>Redundant selection fields
	 * <li>Redundant query fields
	 * <li>Fields incompatible with a file system
	 * </ul>
	 * <P>
	 * For instance, it is possible to build a condition object appropriate for
	 * the Local system, then pass that object in a local file system query.
	 * That will find that the condition is incompatible and generate a mismatch
	 * exception.
	 * <P>
	 * Query is implemented by the file-system-specific classes, like that for
	 * the SRB, FTP, etc. Those classes must re-map condition and select field
	 * names and operator codes to those required by a particular file system
	 * and protocol version. Once re-mapped, they issue the query and get
	 * results. The results are then mapped back to the standard public field
	 * names of the MetaDataGroups. So, if a MetaDataGroup uses a name like
	 * "file path", but the SRB calls it "data name", then query maps first from
	 * "file path" to "data name" before issuing the query, and then from
	 * "data name" back to "file path" within the results. The programmer using
	 * this API should never see the internal field names.
	 * 
	 * @param conditionArray
	 *            The conditional statements that describe the values to query
	 *            the system, like WHERE in SQL.
	 * @param selectArray
	 *            The attributes to be returned from those values that met the
	 *            conditions, like SELECT in SQL.
	 */
	@Override
	public MetaDataRecordList[] query(final MetaDataCondition[] conditions,
			final MetaDataSelect[] selects, final int recordsWanted)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests this filesystem object for equality with the given object. Returns
	 * <code>true</code> if and only if the argument is not <code>null</code>
	 * and both are filesystem objects connected to the same filesystem using
	 * the same account information.
	 * 
	 * @param obj
	 *            The object to be compared with this abstract pathname
	 * 
	 * @return <code>true</code> if and only if the objects are the same;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		try {
			if (obj == null) {
				return false;
			}

			LocalFileSystem temp = (LocalFileSystem) obj;

			if (getAccount().equals(temp.getAccount())) {
				return true;
			}
		} catch (ClassCastException e) {
			return false;
		}
		return false;
	}

	/**
	 * Tests the connection to the filesystem. Local always returns true.
	 */
	@Override
	public boolean isConnected() {
		return true;
	}
}
