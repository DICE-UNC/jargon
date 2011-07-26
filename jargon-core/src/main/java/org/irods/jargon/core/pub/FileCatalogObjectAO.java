package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for an object that interacts with the iRODS data catalog. This
 * object is the parent of access objects that deal with iRODS collections
 * (directories) and data objects (files), and contains common operations for
 * both.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface FileCatalogObjectAO extends IRODSAccessObject {

	/**
	 * Given a target file, and an optional resource, retrieve the host name
	 * that is the resource server that contains the data. This is used in
	 * connection re-routing.
	 * <p/>
	 * Note that this method will return <code>null</code> if no rerouting is
	 * done
	 * 
	 * @param targetAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file or
	 *            collection
	 * @param resourceName
	 *            <code>String</code> that contains the resource name, or blank
	 *            if not used
	 * @return <code>String</code> with the iRODS host name that should be used
	 *         to connect, or null if no reconnect host is needed.
	 * @throws JargonException
	 */
	String getHostForPutOperation(String targetAbsolutePath, String resourceName)
			throws JargonException;

	/**
	 * Given a target file, and an optional resource, retrieve the host name
	 * that is the resource server that contains the data. This is used in
	 * connection re-routing.
	 * <p/>
	 * Note that this method will return <code>null</code> if no rerouting is
	 * done
	 * 
	 * @param targetAbsolutePath
	 *            <code>String</code> with the absolute path to an iRODS file or
	 *            collection
	 * @param resourceName
	 *            <code>String</code> that contains the resource name, or blank
	 *            if not used
	 * @return <code>String</code> with the iRODS host name that should be used
	 *         to connect, or null if no reconnect host is needed.
	 * @throws JargonException
	 */
	String getHostForGetOperation(String sourceAbsolutePath, String resourceName)
			throws JargonException;

}