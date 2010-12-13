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
//  LocalAccount.java  -  edu.sdsc.grid.io.LocalAccount
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralAccount
//            |
//            +-.LocalAccount
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.local;

import java.net.URI;
import java.net.URISyntaxException;

import edu.sdsc.grid.io.GeneralAccount;

/**
 * An object to hold the user information used when connecting to a remote
 * server.
 * <P>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 * @since Jargon1.0
 */
public class LocalAccount extends GeneralAccount {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Constructs an object to hold the user information used when connecting to
	 * a remote server.
	 * <P>
	 * 
	 * @param homeDirectory
	 *            home directory on the local filesystem
	 */
	public LocalAccount() {
		this(null);
	}

	/**
	 * Constructs an object to hold the user information used when connecting to
	 * a remote server.
	 * <P>
	 * 
	 * @param homeDirectory
	 *            home directory on the local filesystem
	 */
	public LocalAccount(final String homeDir) {
		super(homeDir);
	}

	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------
	/**
	 * Sets the home directory of this GeneralAccount.
	 */
	@Override
	public void setHomeDirectory(final String homeDir) {
		if (homeDir == null) {
			try {
				homeDirectory = System.getProperty("user.home");
			} catch (java.security.AccessControlException e) {
				e.printStackTrace();
				// rare security problems, just give up
				homeDirectory = "/";
			}
		} else {
			homeDirectory = homeDir;
		}
	}

	/**
	 * Tests this local file system account object for equality with the given
	 * object. Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and both are account objects for the same filesystem.
	 * 
	 * @param obj
	 *            The object to be compared with this local user account
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

			if (obj instanceof LocalAccount) {
				if (((LocalAccount) obj).getHomeDirectory().equals(
						getHomeDirectory())) {
					return true;
				}
			}
		} catch (ClassCastException e) {
			return false;
		}
		return false;
	}

	/**
	 * Return the URI expression of this account information. i.e.,
	 * file:///homeDirectory
	 */
	public URI toURI() {
		URI uri = null;
		try {
			uri = new URI("file:///" + getHomeDirectory());
		} catch (URISyntaxException e) {
			if (LocalFileSystem.DEBUG > 0) {
				e.printStackTrace();
			}
		}
		return uri;
	}
}
