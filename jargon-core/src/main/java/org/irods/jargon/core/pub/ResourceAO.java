package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidResourceException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;

public interface ResourceAO extends IRODSAccessObject {

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
	 * @return <code>List</code> of {@link AvuData} for this resource
	 * @throws JargonException
	 */
	List<AvuData> listResourceMetadata(String resourceName)
			throws JargonException;

	/**
	 * Retrieve a list of plain <code>String</code> with the resource names in
	 * the zone. These are sorted ascending.
	 * <p/>
	 * This is handy for generating resource lists in interfaces. For iRODS 4+,
	 * it will only list resources that have no parent, appropriate for
	 * addressing the top of a composable resource tree where the children
	 * should not be directly accessed.
	 *
	 * @return <code>List<String></code> of resource names in the zone
	 * @throws JargonException
	 */
	List<String> listResourceNames() throws JargonException;

	/**
	 * Retrieve a list of plain <code>String</code> with the resource names in
	 * the zone, followed by resourceGroupNames in the zone. These are sorted
	 * ascending.
	 * <p/>
	 * This is handy for generating resource lists in interfaces. For iRODS 4+,
	 * it will only list resources that have no parent, appropriate for
	 * addressing the top of a composable resource tree where the children
	 * should not be directly accessed.
	 *
	 * @return <code>List<String></code> of resource names in the zone
	 * @throws JargonException
	 */
	List<String> listResourceAndResourceGroupNames() throws JargonException;

	/**
	 * Add AVU metadata for this resource
	 *
	 * @param resourceName
	 *            <code>String</code> with the name of the resource
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 * @throws InvalidResourceException
	 *             when resource is missing
	 * @throws DuplicateDataException
	 *             when an AVU already exists. Note that iRODS (at least at 2.5)
	 *             is inconsistent, where a duplicate will only be detected if
	 *             units are not blank
	 */
	void addAVUMetadata(String resourceName, AvuData avuData)
			throws InvalidResourceException, DuplicateDataException,
			JargonException;

	/**
	 * Set AVU metadata for this resource. Be aware setting a metadata forces
	 * just this one attribute name to exist (it will delete all the possibly
	 * existing ones)
	 * 
	 * @param resourceName
	 *            <code>String</code> with the name of the resource
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 * @throws InvalidResourceException
	 *             when resource is missing
	 */
	void setAVUMetadata(String resourceName, AvuData avuData)
			throws InvalidResourceException, JargonException;

	/**
	 * Remove Resource AVU data, silently ignore if metadata is not found.
	 *
	 * @param resourceName
	 *            <code>String</code> with the name of the resource
	 * @param avuData
	 *            {@link org.irods.jargon.core.pub.domain.AvuData}
	 * @throws JargonException
	 * @throws InvalidResourceException
	 *             when resource is missing
	 */
	void deleteAVUMetadata(String resourceName, AvuData avuData)
			throws InvalidResourceException, JargonException;

	/**
	 * Add a new resource
	 *
	 * @param resource
	 *            {@link Resource} to be added
	 * @throws DuplicateDataException
	 * @throws JargonException
	 */
	void addResource(final Resource resource) throws DuplicateDataException,
			JargonException;

	/**
	 * Modify a resource
	 *
	 * @param resource
	 *            {@link Resource} to be modified
	 * @param what
	 *            what is modified among type, status, comment, info, context
	 * @throws JargonException
	 */
	void modifyResource(final Resource resource, String what)
			throws JargonException;

	void deleteResource(final String resourceName) throws Exception;

	/**
	 * Add the child resource to the parent resource
	 *
	 * @param parent
	 *            <code>String</code> with the parent resource
	 * @param child
	 *            <code>String</code> with the child resource
	 * @param optionalContext
	 *            <code>String</code> that is blank if not used, with an
	 *            optional context string
	 * @throws JargonException
	 */
	void addChildToResource(String parent, String child, String optionalContext)
			throws JargonException;

	/**
	 * Remove the given child from the resource
	 *
	 * @param parent
	 *            <code>String</code> with the parent resource name
	 * @param child
	 *            <code>String</code> with the child resource name to be removed
	 * @throws InvalidResourceException
	 * @throws JargonException
	 */
	void removeChildFromResource(String parent, String child)
			throws InvalidResourceException, JargonException;
}
