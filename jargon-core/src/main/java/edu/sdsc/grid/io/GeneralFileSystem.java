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
//  GeneralFileSystem.java  -  edu.sdsc.grid.io.GeneralFileSystem
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralFileSystem
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The GeneralFileSystem class is the common superclass for connection
 * implementations to any file system. It provides the framework to support
 * specific file system semantics. Specifically, the functions needed to
 * interact with a file system are provided abstractly by GeneralFileSystem and
 * concretely by its subclass(es).
 * <P>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public abstract class GeneralFileSystem extends Object implements Cloneable {

	/**
	 * Standard path separator character represented as a string for
	 * convenience. This string contains a single character, namely
	 * <code>{@link GeneralFile#PATH_SEPARATOR_CHAR}</code>.
	 * 
	 * @deprecate used a variable and name not matching the java.io package. Use
	 *            GeneralFile separator and pathSeparator
	 */
	public static String PATH_SEPARATOR = GeneralFile.pathSeparator;

	/**
	 * Store the abstract pathnames for the root directories of this file
	 * system.
	 */
	protected static String[] roots;

	// this DEBUG value is here solely for the obfucasted 'Lucid' class that
	// depends on it.
	public static int DEBUG = 0;

	/**
	 * Default number of records returned by a query
	 */
	public final static int DEFAULT_RECORDS_WANTED = 300;

	/**
	 * Default buffer size used for communicating with the remote filesystem and
	 * the various copyTo and copyFrom transfers.
	 */
	static final int DEFAULT_BUFFER_SIZE = 65535;

	/**
	 * Buffer size used for communicating with the remote filesystem and the
	 * various copyTo and copyFrom transfers. Buffers defaults to size defined
	 * in DEFAULT_BUFFER_SIZE
	 */
	protected static int writeBufferSize = DEFAULT_BUFFER_SIZE;

	/**
	 * The account info for connecting to the server.
	 */
	protected GeneralAccount account;

	/**
	 * Finalizes the object by explicitly letting go of each of its internally
	 * held values.
	 * <P>
	 */
	@Override
	protected void finalize() throws Throwable {
		account = null;
	}

	/**
	 * Sets the account object, the info used to connect to the file system.
	 */
	protected abstract void setAccount(GeneralAccount account)
			throws FileNotFoundException, IOException;

	/**
	 * Returns a copy of the account object used by this GeneralFileSystem.
	 */
	public GeneralAccount getAccount() throws NullPointerException {
		if (account != null) {
			return (GeneralAccount) account.clone();
		}

		throw new NullPointerException();
	}

	/**
	 * Returns the homeDirectory used by this account on GeneralFileSystem.
	 */
	public String getHomeDirectory() {
		return account.getHomeDirectory();
	}

	/**
	 * Returns the rootDirectory used by this file system.
	 */
	public abstract String[] getRootDirectories();

	/**
	 * Set the buffer size used by the SRB socket write. Must be greater than 0.
	 * 
	 * @param bufferSize
	 *            The buffer size used by the SRB socket write.
	 */
	public static void setWriteBufferSize(final int bufferSize) {
		if (bufferSize > 0) {
			writeBufferSize = bufferSize;
		}
	}

	/**
	 * Get the buffer size used by the SRB socket write.
	 */
	public static int getWriteBufferSize() {
		return writeBufferSize;
	}

	/**
	 * Queries all files in the metadata catalog and uses metadata values,
	 * <code>fieldName</code>, to be returned.
	 * <P>
	 * This is a convenience method, the same as the code:<br>
	 * <code>query( MetaDataSet.newSelection( fieldName ) );</code>
	 * 
	 * @param fieldName
	 *            The string name used to form the select object.
	 * @return The metadata values for this file refered to by
	 *         <code>fieldName</code>
	 */
	public MetaDataRecordList[] query(final String fieldName)
			throws IOException {
		return query(
				new MetaDataSelect[] { MetaDataSet.newSelection(fieldName) },
				GeneralFileSystem.DEFAULT_RECORDS_WANTED);
	}

	/**
	 * Queries all files in the metadata catalog and uses metadata values,
	 * <code>selects</code>, to be returned.
	 * <P>
	 * This is a convenience method, the same as the code:<br>
	 * <code>query( MetaDataSet.newSelection( fieldNames ) );</code>
	 * 
	 * @param fieldNames
	 *            The string names used to form the select objects.
	 * @return The metadata values for this file refered to by
	 *         <code>fieldNames</code>
	 */
	public MetaDataRecordList[] query(final String[] fieldNames)
			throws IOException {
		return query(MetaDataSet.newSelection(fieldNames),
				GeneralFileSystem.DEFAULT_RECORDS_WANTED);
	}

	/**
	 * Queries all files in the metadata catalog and uses one metadata value,
	 * <code>select</code>, to be returned.
	 */
	public MetaDataRecordList[] query(final MetaDataSelect select)
			throws IOException {
		return query(new MetaDataSelect[] { select },
				GeneralFileSystem.DEFAULT_RECORDS_WANTED);
	}

	/**
	 * Queries all files in the metadata catalog and uses metadata values,
	 * <code>selects</code>, to be returned.
	 */
	public MetaDataRecordList[] query(final MetaDataSelect[] selects)
			throws IOException {
		return query(selects, GeneralFileSystem.DEFAULT_RECORDS_WANTED);
	}

	/**
	 * Queries all files in the metadata catalog and uses metadata values,
	 * <code>selects</code>, to be returned.
	 */
	public MetaDataRecordList[] query(final MetaDataSelect[] selects,
			final int recordsWanted) throws IOException {
		return query(null, selects, recordsWanted);
	}

	/**
	 * Queries the file server to find all files that match the set of
	 * conditions in <code>conditions</code>. For all those that match, the
	 * fields indicated in the <code>selects</code> are returned as a
	 * MetaDataRecordList[].
	 * 
	 * @param conditions
	 *            The conditional statements that describe the values to query
	 *            the server, like WHERE in SQL.
	 * @param selects
	 *            The attributes to be returned from those values that met the
	 *            conditions, like SELECT in SQL.
	 */
	public MetaDataRecordList[] query(final MetaDataCondition[] conditions,
			final MetaDataSelect[] selects) throws IOException {
		return query(conditions, selects,
				GeneralFileSystem.DEFAULT_RECORDS_WANTED);
	}

	/**
	 * Queries the file server to find all files that match the set of
	 * conditions in <code>conditions</code>. For all those that match, the
	 * fields indicated in the <code>selects</code> are returned as a
	 * MetaDataRecordList[].
	 * 
	 * @param conditions
	 *            The conditional statements that describe the values to query
	 *            the server, like WHERE in SQL.
	 * @param selects
	 *            The attributes to be returned from those values that met the
	 *            conditions, like SELECT in SQL.
	 * @param numberOfRecordsWanted
	 *            Maximum number of results of this query that should be
	 *            included in the return value. Default is
	 *            <code>DEFAULT_RECORDS_WANTED</code>. If more results are
	 *            available, they can be obtained using
	 *            <code>MetaDataRecordList.getMoreResults</code>
	 * @return The metadata results from the filesystem, returns
	 *         <code>null</code> if there are no results.
	 */
	public abstract MetaDataRecordList[] query(MetaDataCondition[] conditions,
			MetaDataSelect[] selects, int numberOfRecordsWanted)
			throws IOException;

	/**
	 * @return a copy of this account object.
	 */
	@Override
	public Object clone() {
		try {
			return FileFactory.newFileSystem(account);
		} catch (IOException e) {
			e.initCause(e);
			throw new RuntimeException("IOException in thread.", e);
		}
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
	public abstract boolean equals(Object obj);

	/**
	 * Checks if the fileSystem is connected.
	 */
	public abstract boolean isConnected();

	/**
	 * Returns a string representation of this file system object.
	 */
	@Override
	public String toString() {
		return new String("GeneralFileSystem, " + getHomeDirectory());
	}
}
