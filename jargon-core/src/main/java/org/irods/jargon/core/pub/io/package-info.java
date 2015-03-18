/**
 * This package is the primary public API for <code>java.io.*</code> interfaces to iRODS.  See the <code>org.irods.jargon.core.pub.*</code> directory
 * for objects that can manage connections and obtain the factory for iRODS files and streams.  The {@link org.irods.jargon.core.pub.io.IRODSFileFactory}
 * is the factory object that is used to get file and stream objects.
 *
 * To connect to iRODS, the standard method is to obtain a reference to an {@link org.irods.jargon.core.pub.IRODSFileSystem} object.  From this
 * object you are provided with references to the {@link org.irods.jargon.core.pub.IRODSAcessObjectFactoryI} to obtain iRODS access objects, and you may use the
 * {@link org.irods.jargon.core.pub.io.IRODSFileFactory} to obtain references to an iRODS file.  The various factory objects, and lower-level connection creation and
 * management objects may be found in the <code>org.irods.jargon.core.connection</code> package.
 *
 * Note that this package is dedicated to the <code>java.io.*</code> interfaces.  There are another set of public API in the <code>org.irods.jargon.core.pub.*</code>
 * package that present iRODS access objects.  These access objects are meant to mirror the DAO pattern, and use POJO domain objects to represent
 * the entities in the iRODS metadata catalog, as well as the functions and components of the iRODS server.  Look to these access objects when doing
 * operations that are not defined by the <code>java.io</code> interfaces.
 *
 */
package org.irods.jargon.core.pub.io;

