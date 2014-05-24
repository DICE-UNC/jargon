package org.irods.jargon.dataprofile;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.MetaDataAndDomainData;

public interface DataTypeResolutionService {

	/**
	 * Given a data object and associated metadata, determine the MIME type.
	 * This will look first for a special AVU value that is standard in iRODS,
	 * and then will use the file name extension to derive a mime type.
	 * <p/>
	 * Note that this resolution scheme is provisional, and may be extended to
	 * standard mechanisms on iRODS, or using file contents based resolution, or
	 * to accommodate extended file types.
	 * 
	 * @param dataObject
	 *            {@link DataObject}
	 * @param metadata
	 *            <code>List</code> of {@link MetaDataAndDomaimData} with AVUs
	 *            associated with the data object. This is done in this manner
	 *            anticipating that the AVUs are already available, preventing
	 *            unnecessary look ups of iRODS data. Other signatures can be
	 *            added later to obtain those AVUs by query
	 * @return <code>String</code> with a resolved file type as a MIME value
	 * @throws JargonException
	 */
	String resolveDataTypeWithProvidedAvuAndDataObject(DataObject dataObject,
			List<MetaDataAndDomainData> metadata) throws JargonException;

	/**
	 * Determine the data type based on file extension using Tika
	 * 
	 * @param dataObject
	 *            {@link DataObject} to be resolved
	 * @return <code>String</code> version of the MIME type
	 * 
	 * @throws JargonException
	 */
	String determineMimeTypeViaTika(DataObject dataObject)
			throws JargonException;

}