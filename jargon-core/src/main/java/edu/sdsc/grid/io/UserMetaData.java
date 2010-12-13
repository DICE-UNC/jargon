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
//  UserMetaData.java  -  edu.sdsc.grid.io.UserMetaData
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.StandardMetaData
//            |
//            +-.UserMetaData
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * The metadata naming interface for user metadata. Some meta groups are
 * standard regardless of implementation. To handle standard metadata, we
 * defined interfaces that are implemented by protocol-specific metadata groups.
 * The UserMetaData interface contract is that the implementor supports meta
 * data we might find as common user metadata, such as:
 * <ul>
 * <li>USER_ID
 * <li>USER_NAME
 * <li>USER_GROUP_NAME
 * <li>USER_ADDRESS
 * <li>USER_PHONE
 * <li>USER_EMAIL
 * </ul>
 * <P>
 * An implementation specific class, such as the SRB user metadata support these
 * fields, but also may support further user metadata fields, such as:
 * <ul>
 * <li>USER_TYPE_NAME
 * <li>USER_DOMAIN
 * <li>USER_AUDIT_TIME_STAMP
 * <li>USER_AUDIT_COMMENTS
 * <li>USER_DISTINGUISHED_NAME
 * <li>USER_AUTHENTICATION_SCHEME
 * </ul>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public interface UserMetaData extends StandardMetaData {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	public final static String USER_ID = "user id";
	public final static String USER_NAME = "user name";
	public final static String USER_ADDRESS = "user address";
	public final static String USER_PHONE = "user phone";
	public final static String USER_EMAIL = "user email";

	public static final String USER_TYPE = "User Type";
	public static final String USER_ZONE = "User Zone";
	public static final String USER_DN = "User DN";
	public static final String USER_DN_2_1 = "User DN 2.1";

	public static final String USER_INFO = "User Information";
	public static final String USER_COMMENT = "User Comment";
	public static final String USER_CREATE_DATE = "User Time created (Unix Time)";
	public static final String USER_MODIFY_DATE = "User Time last modified (Unix Time)";

	// User Group
	// public static final String USER_GROUP_ID = "User Group Identifier";
	public static final String USER_GROUP = "User Group Name";

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------

}
