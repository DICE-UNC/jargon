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
//  RemoteAccount.java  -  edu.sdsc.grid.io.RemoteAccount
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralAccount
//            |
//            +-.RemoteAccount
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * An object to hold the user information used when connecting to a remote file
 * system.
 * <P>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public abstract class RemoteAccount extends GeneralAccount {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * The host to connect to on the server.
	 */
	protected String host;

	/**
	 * The port to connect to on the server.
	 */
	protected int port;

	/**
	 * The user name to send to the server.
	 */
	protected String userName;

	/**
	 * Currently, just the text password.
	 */
	protected String password;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Constructs an object to hold the user information used when connecting to
	 * a remote server.
	 * <P>
	 * 
	 * @param host
	 *            the remote system domain name
	 * @param port
	 *            the port on the remote system
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 * @param homeDirectory
	 *            home directory on the remote system
	 */
	public RemoteAccount(final String host, final int port,
			final String userName, final String password, final String homeDir) {
		super(homeDir);
		setHost(host);
		setPort(port);
		setUserName(userName);
		setPassword(password);
	}

	/**
	 * Finalizes the object by explicitly letting go of each of its internally
	 * held values.
	 * <P>
	 */
	@Override
	protected void finalize() {
		super.finalize();
		host = null;
		userName = null;
		password = null;
	}

	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------
	/**
	 * Sets the host of this RemoteAccount.
	 * 
	 * @throws NullPointerException
	 *             if host is null.
	 */
	public void setHost(final String host) {
		if (host == null) {
			throw new NullPointerException("The host string cannot be null");
		}

		this.host = host;
	}

	/**
	 * Sets the port of this RemoteAccount. Port numbers can not be negative.
	 */
	public void setPort(final int port) {
		if (port > 0) {
			this.port = port;
		} else {
			throw new IllegalArgumentException("Invalid port number");
		}
	}

	/**
	 * Sets the userName of this RemoteAccount.
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}

	/**
	 * Sets the client password.
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Returns the host used by this RemoteAccount.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port used by this RemoteAccount.
	 */
	public int getPort() throws IllegalArgumentException {
		return port;
	}

	/**
	 * Returns the userName used by this RemoteAccount.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Returns the password used by this RemoteAccount.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Return the URI representation of this Account object.
	 * 
	 * @param includePassword
	 *            If true, the account's password will be included in the URI,
	 *            if possible.
	 */
	public URI toURI(final boolean includePassword) {

		try {
			return new URI(toString());
		} catch (URISyntaxException e) {
			if (GeneralFileSystem.DEBUG > 0) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
