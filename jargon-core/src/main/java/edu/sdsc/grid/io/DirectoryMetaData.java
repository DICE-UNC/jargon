//  Copyright (c) 2007, Regents of the University of California
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
//  DirectoryMetaData.java  -  edu.sdsc.grid.io.DirectoryMetaData
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.StandardMetaData
//            |
//            +-.DirectoryMetaData
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * The metadata naming interface for directory metadata.
 * 
 * @see GeneralMetaData
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public interface DirectoryMetaData extends StandardMetaData {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	// public static final String DIRECTORY_ID = "Directory Identifier";

	public final static String PARENT_DIRECTORY_NAME = "parent directory name";
	public final static String DIRECTORY_OWNER = "directory owner";
	public final static String DIRECTORY_CREATE_TIMESTAMP = "directory creation timestamp";
	public final static String DIRECTORY_COMMENTS = "directory comments";

	public static final String DIRECTORY_OWNER_ZONE = "Directory Owner Zone";
	// public static final String DIRECTORY_MAP_ID = "Directory Map ID";
	public static final String DIRECTORY_INHERITANCE = "Directory Inheritance";
	public static final String DIRECTORY_CREATE_DATE = "Directory Time created (Unix Time)";
	public static final String DIRECTORY_MODIFY_DATE = "Directory Time last modified (Unix Time)";

	/**
	 * Directory access constraint/permissions.
	 */
	public final static String DIRECTORY_ACCESS_CONSTRAINT = "Directory access constraint";

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------

}
