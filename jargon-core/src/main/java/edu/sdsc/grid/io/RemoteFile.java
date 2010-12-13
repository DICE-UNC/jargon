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
//  RemoteFile.java  -  edu.sdsc.grid.io.RemoteFile
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralFile
//              |
//              +-.RemoteFile
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.io.IOException;
import java.net.URI;

/**
 * An abstract representation of file and directory pathnames on a remote
 * server. This abstract class can be subclassed to create a file object for
 * refering to a remote file on a particular kind fo remote server.
 * <P>
 * Shares many similarities with the java.io.File class: User interfaces and
 * operating systems use system-dependent pathname strings to name files and
 * directories. This class presents an abstract, system-independent view of
 * hierarchical pathnames. An abstract pathname has two components:
 * <P>
 * An optional system-dependent prefix string, such as a disk-drive specifier,
 * "/" for the UNIX root directory, or "\\" for a Microsoft Windows UNC
 * pathname, and <br>
 * A sequence of zero or more string names. Each name in an abstract pathname
 * except for the last denotes a directory; the last name may denote either a
 * directory or a file. The empty abstract pathname has no prefix and an empty
 * name sequence. The conversion of a pathname string to or from an abstract
 * pathname is inherently system-dependent. When an abstract pathname is
 * converted into a pathname string, each name is separated from the next by a
 * single copy of the default separator character. The default name-separator
 * character is defined by the system property file.separator, and is made
 * available in the public static fields separator and separatorChar of this
 * class. When a pathname string is converted into an abstract pathname, the
 * names within it may be separated by the default name-separator character or
 * by any other name-separator character that is supported by the underlying
 * system.
 * <P>
 * A pathname, whether abstract or in string form, may be either absolute or
 * relative. An absolute pathname is complete in that no other information is
 * required in order to locate the file that it denotes. A relative pathname, in
 * contrast, must be interpreted in terms of information taken from some other
 * pathname. By default the classes in this package always resolve relative
 * pathnames against the current user directory. This directory is named by the
 * system property user.dir, and is typically the directory in which the Java
 * virtual machine was invoked.
 * <P>
 * The prefix concept is used to handle root directories on UNIX platforms, and
 * drive specifiers, root directories and UNC pathnames on Microsoft Windows
 * platforms, as follows:
 * <P>
 * For UNIX platforms, the prefix of an absolute pathname is always "/".
 * Relative pathnames have no prefix. The abstract pathname denoting the root
 * directory has the prefix "/" and an empty name sequence.
 * <P>
 * For Microsoft Windows platforms, the prefix of a pathname that contains a
 * drive specifier consists of the drive letter followed by ":" and possibly
 * followed by "\" if the pathname is absolute. The prefix of a UNC pathname is
 * "\\"; the hostname and the share name are the first two names in the name
 * sequence. A relative pathname that does not specify a drive has no prefix.
 * <P>
 * Instances of the RemoteFile class are immutable; that is, once created, the
 * abstract pathname represented by a RemoteFile object will never change.
 * <P>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 * @see java.io.File
 * @see edu.sdsc.grid.io.GeneralFile
 */
public abstract class RemoteFile extends GeneralFile {
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
	 * Creates a new RemoteFile instance by converting the given pathname string
	 * into an abstract pathname.
	 * <P>
	 * 
	 * @param fileSystem
	 *            The connection to the remote server
	 * @param filePath
	 *            A pathname string
	 */
	public RemoteFile(final RemoteFileSystem fileSystem, final String filePath)
			throws NullPointerException {
		this(fileSystem, "", filePath);
	}

	/**
	 * Creates a new RemoteFile instance from a parent pathname string and a
	 * child pathname string.
	 * <P>
	 * If parent is null then the new RemoteFile instance is created as if by
	 * invoking the single-argument RemoteFile constructor on the given child
	 * pathname string.
	 * <P>
	 * Otherwise the parent pathname string is taken to denote a directory, and
	 * the child pathname string is taken to denote either a directory or a
	 * file. If the child pathname string is absolute then it is converted into
	 * a relative pathname in a system-dependent way. If parent is the empty
	 * string then the new RemoteFile instance is created by converting child
	 * into an abstract pathname and resolving the result against a
	 * system-dependent default directory. Otherwise each pathname string is
	 * converted into an abstract pathname and the child abstract pathname is
	 * resolved against the parent.
	 * <P>
	 * 
	 * @param fileSystem
	 *            The connection to the remote server
	 * @param parent
	 *            The parent pathname string
	 * @param child
	 *            The child pathname string
	 */
	public RemoteFile(final RemoteFileSystem fileSystem, final String parent,
			final String child) throws NullPointerException {
		super(fileSystem, parent, child);
	}

	/**
	 * Creates a new RemoteFile instance from a parent abstract pathname and a
	 * child pathname string.
	 * <P>
	 * If parent is null then the new RemoteFile instance is created as if by
	 * invoking the single-argument RemoteFile constructor on the given child
	 * pathname string.
	 * <P>
	 * Otherwise the parent abstract pathname is taken to denote a directory,
	 * and the child pathname string is taken to denote either a directory or a
	 * file. If the child pathname string is absolute then it is converted into
	 * a relative pathname in a system-dependent way. If parent is the empty
	 * abstract pathname then the new RemoteFile instance is created by
	 * converting child into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 * <P>
	 * 
	 * @param parent
	 *            The parent abstract pathname
	 * @param child
	 *            The child pathname string
	 */
	public RemoteFile(final RemoteFile parent, final String child)
			throws NullPointerException {
		this((RemoteFileSystem) parent.getFileSystem(), parent.getParent(),
				child);
	}

	/**
	 * Creates a new RemoteFile instance by converting the given file: URI into
	 * an abstract pathname.
	 * <P>
	 * The exact form of a file: URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * <P>
	 * For a given abstract pathname f it is guaranteed that
	 * <P>
	 * &nbsp;&nbsp;&nbsp;&nbsp;new RemoteFile( f.toURI()).equals( f)
	 * <P>
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a file: URI that is created in a virtual machine on one operating
	 * system is converted into an abstract pathname in a virtual machine on a
	 * different operating system.
	 * 
	 * @param uri
	 *            An absolute, hierarchical URI using a supported scheme.
	 * @throws NullPointerException
	 *             if <code>uri</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the preconditions on the parameter do not hold.
	 */
	public RemoteFile(final URI uri) throws NullPointerException,
			IllegalArgumentException {
		super(uri);

		// currently, handle everything in subclass.
	}

	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------

	/**
	 * @return resource the physical resource where this RemoteFile is stored.
	 *         Returns null if this abstract pathname is a directory or does not
	 *         exist.
	 * 
	 * @throws IOException
	 *             If an IOException occurs during the system query.
	 */
	public abstract String getResource() throws IOException;

	/**
	 * Replicates this RemoteFile to a new resource. Directories/collections
	 * will be recursively replicated.
	 * <P>
	 * In some remote systems, one can make copies of a data set and store the
	 * copies in different locations. But, all these copies are considered to be
	 * identifiable by the same identifier. That is, each copy is considered to
	 * be equivalent to each other.
	 * <P>
	 * When a user reads a replicated data set, the remote system cycles through
	 * all the copies of the datset and reads the one that is accessible at that
	 * time.
	 * 
	 * @param newResource
	 *            The storage resource name of the new copy.
	 * @throws IOException
	 *             If an IOException occurs.
	 */
	public abstract void replicate(String newResource) throws IOException;

	/**
	 * Constructs a URI that represents this abstract pathname.
	 * 
	 * <p>
	 * The exact form of the URI is according to the remote system. If it can be
	 * determined that the file denoted by this abstract pathname is a
	 * directory, then the resulting URI will end with a slash.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i>, it is guaranteed that
	 * 
	 * <blockquote><tt>
	 * new {@link #RemoteFile(java.net.URI) RemoteFile}
	 * (</tt><i>&nbsp;f</i><tt>.toURI()).equals(</tt><i>&nbsp;f</i><tt>)
	 * </tt> </blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. However, this relationship typically does not hold
	 * when the URI that is created in a virtual machine on one operating system
	 * is converted into an abstract pathname in a virtual machine on a
	 * different operating system.
	 * 
	 * @return An absolute, hierarchical URI with an equal scheme, a path
	 *         representing this abstract pathname, and undefined authority,
	 *         query, and fragment components
	 * 
	 * @see #RemoteFile(java.net.URI)
	 * @see java.net.URI
	 * @see java.net.URI#toURL()
	 */
	public URI toURI(final boolean includePassword) {
		return toURI();
	}
}
