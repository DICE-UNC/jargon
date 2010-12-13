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
//  ResourceMetaData.java  -  edu.sdsc.grid.io.ResourceMetaData
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.StandardMetaData
//            |
//            +-.ResourceMetaData
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * The metadata naming interface for resource metadata.
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public interface ResourceMetaData extends StandardMetaData {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	// public static final String RESOURCE_ID = "Resource Identifier";
	public static final String COLL_RESOURCE_NAME = "Coll Resource Name";
	public static final String RESOURCE_NAME = "Resource Name";
	public static final String RESOURCE_ZONE = "Resource Zone Name";
	public static final String RESOURCE_TYPE = "Resource Type";
	public static final String RESOURCE_CLASS = "Resource Class";
	public static final String RESOURCE_LOCATION = "Resource Location";
	public static final String RESOURCE_VAULT_PATH = "Resource Vault Path";
	public static final String RESOURCE_FREE_SPACE = "Resource Free Space";
	public static final String RESOURCE_INFO = "Resource Information";
	public static final String RESOURCE_COMMENTS = "Resource Comment";
	public static final String RESOURCE_CREATE_DATE = "Resource Time created (Unix Time)";
	public static final String RESOURCE_MODIFY_DATE = "Resource Time last modified (Unix Time)";

	// Resource Group
	public static final String RESOURCE_GROUP_ID = "Resource Group Identifier";
	public static final String RESOURCE_GROUP = "Resource Group Name";

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------

}
