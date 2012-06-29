package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;

public interface ResourceAO extends IRODSAccessObject {

	/**
	 * List all of the <code>Resource</code> in the zone. This returns a list of
	 * domain objects with detailed information.
	 * 
	 * @param zoneName
	 *            <code>String</code> with the target zone name.
	 * @return <code>List</code> of {@link Resource}
	 * @throws JargonException
	 */
	List<Resource> listResourcesInZone(String zoneName) throws JargonException;

	/**
	 * Get the first <code>Resource</code> associated with an iRODS file. There
	 * may be other iRODS resources associated with the given file
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} representing the file in iRODS
	 * @return {@link Resource} which is the first (of potentially many)
	 *         resources associated with the given file
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	Resource getFirstResourceForIRODSFile(IRODSFile irodsFile)
			throws JargonException, DataNotFoundException;

	/**
	 * Find all resources on the connected zone and return as a list of
	 * <code>Resource</code> objects.
	 * 
	 * @return a <code>List</code> of
	 *         {@link org.irods.jargon.core.pub.domain.Resource};
	 * @throws JargonException
	 */
	List<Resource> findAll() throws JargonException;

	/**
	 * Find a resource given its name
	 * 
	 * @param resourceName
	 *            <code>String</code> with the name of the resource to be looked
	 *            up
	 * @return {@link org.irods.jargon.core.pub.domain.Resource}
	 * @throws JargonException
	 * @throws DataNotFoundException
	 *             indicates that the resource with the given name is not found
	 */
	Resource findByName(final String resourceName) throws JargonException,
			DataNotFoundException;

	/**
	 * Handy query method will return a list of resources that fit a given where
	 * statement.
	 * 
	 * @param whereStatement
	 *            <code>String</code> with an iquest formatted query where
	 *            statement, does not include the leading 'WHERE' clause
	 * @return <code>List<Resource></code> with the resources that match the
	 *         given query
	 * @throws JargonException
	 */
	List<Resource> findWhere(String whereStatement) throws JargonException;

	/**
	 * Find a resource by its id
	 * 
	 * @param resourceId
	 *            <code>String</code> with the unique id of the resource
	 * @return {@link org.irods.jargon.core.pub.domain.Resource} with the
	 *         matching id
	 * @throws JargonException
	 * @throws DataNotFoundException
	 */
	Resource findById(final String resourceId) throws JargonException,
			DataNotFoundException;

	/**
	 * List the AVU metadata, as well as information identifying the Resource
	 * associated with that metadata, based on a metadata query.
	 * 
	 * @param avuQuery
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            defines the metadata query
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.MetaDataAndDomainData}
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			List<AVUQueryElement> avuQuery) throws JargonQueryException,
			JargonException;

	/**
	 * List all AVU metadata associated with the resource
	 * 
	 * @param resourceName
	 *            <code>String</code> containing the resource name
	 * @return <code>List</code> of {@link org.irods.jargon.core.domain.AVUData}
	 *         for this resource
	 * @throws JargonException
	 */
	List<AvuData> listResourceMetadata(String resourceName)
			throws JargonException;

	/**
	 * Retrieve a list of plain <code>String</code> with the resource names in
	 * the zone. These are sorted ascending.
	 * <p/>
	 * This is handy for generating resource lists in interfaces.
	 * 
	 * @return <code>List<String></code> of resource names in the zone
	 * @throws JargonException
	 */
	List<String> listResourceNames()
			throws JargonException;

	/**
	 * Retrieve a list of plain <code>String</code> with the resource names in
	 * the zone, followed by resourceGroupNames in the zone. These are sorted
	 * ascending.
	 * <p/>
	 * This is handy for generating resource lists in interfaces.
	 * 
	 * @return <code>List<String></code> of resource names in the zone
	 * @throws JargonException
	 */
	List<String> listResourceAndResourceGroupNames() throws JargonException;
}
