/**
 *
 */
package org.irods.jargon.dataprofile;

import java.io.IOException;
import java.util.List;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.usertagging.tags.UserTaggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to identify the data type of an iRODS file based on a series of
 * resolution methods
 * <p/>
 * This should be considered a provisional solution while we work to standardize
 * treatment of MIME types as a standard part of iRODS
 *
 * @author Mike Conway - DICE
 *
 */
public class DataTypeResolutionServiceImpl extends AbstractJargonService implements DataTypeResolutionService {

	public static final String APPLICATION_IRODS_RULE = "application/irods-rule";
	public static final Logger log = LoggerFactory.getLogger(DataTypeResolutionServiceImpl.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.dataprofile.DataTypeResolutionService#
	 * resolveDataTypeWithProvidedAvuAndDataObject
	 * (org.irods.jargon.core.pub.domain.DataObject, java.util.List)
	 */
	@Override
	public String resolveDataTypeWithProvidedAvuAndDataObject(final DataObject dataObject,
			final List<MetaDataAndDomainData> metadata) throws JargonException {

		log.info("resolveDataTypeWithProvidedAvuAndDataObject()");

		if (dataObject == null) {
			throw new IllegalArgumentException("null dataObject");
		}

		if (metadata == null) {
			throw new IllegalArgumentException("null metadata");
		}

		// prefer AVU MIME type

		String mimeType = searchAvusForMimeType(metadata);

		if (mimeType != null) {
			log.info("found mime type in AVU:{}", mimeType);
			return mimeType;
		}

		log.info("checking for known irods types - interim code...");
		/*
		 * This is provisional code to be refactor in
		 * https://github.com/DICE-UNC/jargon-extensions/issues/11
		 */

		mimeType = determimeMimeTypeOfIrodsObjects(dataObject);

		log.info("no mime type in AVU, use Tika to derive based on file extenstion");

		if (mimeType == null) {
			log.info("not a known irods type, try tika");
			mimeType = determineMimeTypeViaTika(dataObject);
		}

		if (mimeType == null) {
			log.info("no mime type found via tika");
			return "";
		}

		log.info("mime type from Tika:{}", mimeType);
		return mimeType;

	}

	/**
	 * Temporary shim for first cloud browser release
	 *
	 * @param dataObject
	 * @return
	 */
	private String determimeMimeTypeOfIrodsObjects(final DataObject dataObject) {

		String extension = LocalFileUtils.getFileExtension(dataObject.getDataName());
		if (extension == null || extension.isEmpty()) {
			return null;
		}

		if (extension.equals(".r")) {
			log.info("irods rule detected in:{}", dataObject.getDataName());
			return APPLICATION_IRODS_RULE;
		} else {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.dataprofile.DataTypeResolutionService#
	 * determineMimeTypeViaTika(org.irods.jargon.core.pub.domain.DataObject)
	 */
	@Override
	public String determineMimeTypeViaTika(final DataObject dataObject) throws JargonException {
		AutoDetectParser parser = new AutoDetectParser();
		Detector detector = parser.getDetector();
		Metadata md = new Metadata();
		md.add(TikaMetadataKeys.RESOURCE_NAME_KEY, dataObject.getDataName());
		MediaType mediaType;
		try {
			mediaType = detector.detect(null, md);
		} catch (IOException e) {
			throw new JargonException("io exception determining file type by extension", e);
		}
		return mediaType.toString();
	}

	private String searchAvusForMimeType(final List<MetaDataAndDomainData> metadata) {

		for (MetaDataAndDomainData metadataValue : metadata) {
			if (metadataValue.getAvuUnit().equals(UserTaggingConstants.MIME_TYPE_AVU_UNIT)) {
				return metadataValue.getAvuAttribute();
			}
		}
		return null;

	}

	/**
	 *
	 */
	public DataTypeResolutionServiceImpl() {
		super();
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public DataTypeResolutionServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

}
