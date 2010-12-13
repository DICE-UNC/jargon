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
//  GeneralMetaData.java  -  edu.sdsc.grid.io.GeneralMetaData
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.StandardMetaData
//            |
//            +-.GeneralMetaData
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * The metadata naming interface for general metadata. Some meta groups are
 * standard regardless of implementation. To handle standard metadata, we
 * defined interfaces that are implemented by protocol-specific metadata groups.
 * The GeneralMetaData interface contract is that the implementor supports meta
 * data we might find as common general metadata, such as:
 * <ul>
 * <li>FILE_NAME
 * <li>FILE_GROUP_NAME
 * <li>SIZE
 * <li>CREATION_DATE
 * <li>MODIFICATION_DATE
 * <li>OWNER
 * <li>ACCESS_CONSTRAINT
 * <li>FILE_COMMENTS
 * <li>DIRECTORY_NAME
 * </ul>
 * <P>
 * An implementation specific class, such as SRB general metadata support these
 * fields, but also may support further other metadata fields, such as:
 * <ul>
 * <li>Replication number
 * <li>File type
 * <li>Data classification name
 * <li>Data classification type
 * <li>Access constraint
 * <li>Comments
 * <li>Comments date
 * <li>Deleted flag
 * <li>Owner domain
 * <li>Owner email address
 * </ul>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public interface GeneralMetaData extends StandardMetaData {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	public final static String FILE_GROUP_NAME = "file group name";

	/**
	 * File size
	 */
	public final static String SIZE = "file size";

	/**
	 * File creation date
	 */
	public final static String CREATION_DATE = "file creation date";

	/**
	 * File modification date
	 */
	public final static String MODIFICATION_DATE = "file modification date";

	/**
	 * File owner
	 */
	public final static String OWNER = "file owner";

	/**
	 * File access constraint/permissions.
	 */
	public final static String ACCESS_CONSTRAINT = "file access constraint";
	// public final static String FILE_ID = "file id";

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------

}
