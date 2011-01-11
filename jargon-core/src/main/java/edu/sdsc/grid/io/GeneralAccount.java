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
//  GeneralAccount.java  -  edu.sdsc.grid.io.GeneralAccount
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralAccount
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * An object to hold the user information used when connecting to a file system.
 * This class does not actually connect to a filesystem. It only hold user
 * connection information. Setting or getting this information only refers to
 * the contents of the object.
 * <P>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public abstract class GeneralAccount extends Object implements Cloneable {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * The home directory on the server
	 */
	protected String homeDirectory;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Constructs an object to hold the user information used when connecting to
	 * a file system.
	 * <P>
	 * 
	 * @param homeDirectory
	 *            home directory on the SRB
	 */
	public GeneralAccount(final String homeDir) {
		setHomeDirectory(homeDir);
	}

	/**
	 * Finalizes the object by explicitly letting go of each of its internally
	 * held values.
	 * <P>
	 */
	@Override
	protected void finalize() {
		if (homeDirectory != null) {
			homeDirectory = null;
		}
	}

	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------
	/**
	 * Sets the home directory of this GeneralAccount.
	 */
	public abstract void setHomeDirectory(String homeDir);

	/**
	 * Returns the homeDirectory used by this GeneralAccount.
	 * 
	 * @return homeDirectory
	 */
	public String getHomeDirectory() throws NullPointerException {
		if (homeDirectory != null) {
			return homeDirectory;
		}

		throw new NullPointerException();
	}

	/**
	 * @return a copy of this account object.
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// Shouldn't happen
			throw new InternalError();
		}
	}

	@Override
	public abstract boolean equals(Object obj);
}
