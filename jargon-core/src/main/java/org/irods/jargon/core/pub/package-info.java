/**
 * This package is the primary public API for iRODS, and should be the primary place to find operations necessary to work with iRODS.
 *
 * To connect to iRODS, the standard method is to obtain a reference to an {@link org.irods.jargon.core.pub.IRODSFileSystem} object.  From this
 * object you are provided with references to the {@link org.irods.jargon.core.pub.IRODSAcessObjectFactoryI} to obtain iRODS access objects, and you may use the
 * {@link org.irods.jargon.core.pub.io.IRODSFileFactory} to obtain references to an iRODS file.  The various factory objects, and lower-level connection creation and
 * management objects may be found in the {@code org.irods.jargon.core.connection} package.
 *
 * iRODS access objects mirror the typical DAO pattern, and provide CRUD operations for Resources, Collections, Data Objects, Users, and other
 * objects in the iRODS domain (iCAT).  Consult each of the objects to see relevant methods for the domain object.  There are objects here that can
 * help you submit rules, issue queries, and other common iRODS operations.
 *
 * These iRODS access objects use POJO objects to represent items in the iRODS metadata catalog.  These domain objects are used as input
 * and output parameters to the access object methods.  These domain objects can be found in the {@code org.irods.jargon.core.pub.domain} package.
 */
package org.irods.jargon.core.pub;

