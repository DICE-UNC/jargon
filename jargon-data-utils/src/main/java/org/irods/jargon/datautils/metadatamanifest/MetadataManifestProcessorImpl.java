/**
 *
 */
package org.irods.jargon.datautils.metadatamanifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.BulkAVUOperationResponse;
import org.irods.jargon.core.pub.BulkAVUOperationResponse.ResultStatus;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.datautils.metadatamanifest.MetadataManifest.FailureMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of a manifest processor that can manage AVUs at a given path
 * via a bulk operation
 *
 * @author mcc
 *
 */
public class MetadataManifestProcessorImpl extends AbstractJargonService implements MetadataManifestProcessor {

	public static final Logger log = LoggerFactory.getLogger(MetadataManifestProcessorImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.metadatamanifest.MetdataManifest#
	 * metadataManifestToJson(org.irods.jargon.datautils.metadatamanifest.
	 * MetadataManifest)
	 */
	@Override
	public String metadataManifestToJson(final MetadataManifest metadataManifest) throws JargonException {
		log.info("metadataManifestToJson()");
		if (metadataManifest == null) {
			throw new IllegalArgumentException("null metadataManifest");
		}
		log.info("metadataManifest:{}", metadataManifest);
		final ObjectMapper mapper = new ObjectMapper();

		// Object to JSON in String
		try {
			return mapper.writeValueAsString(metadataManifest);
		} catch (final JsonProcessingException e) {
			log.error("cannot write json for object:{}", metadataManifest, e);
			throw new JargonException("error writing metadataManifest to string", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.metadatamanifest.MetdataManifest#
	 * stringJsonToMetadataManifest(java.lang.String)
	 */
	@Override
	public MetadataManifest stringJsonToMetadataManifest(final String jsonString) throws JargonException {
		log.info("stringJsonToMetadataManifest()");
		if (jsonString == null || jsonString.isEmpty()) {
			throw new IllegalArgumentException("null or empty jsonString");
		}
		log.info("jsonString:{}", jsonString);
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, MetadataManifest.class);
		} catch (final IOException e) {
			log.error("cannot convert json to object:{}", jsonString, e);
			throw new JargonException("error writing json to metadataManifest", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.metadatamanifest.MetdataManifest#processManifest(
	 * org.irods.jargon.datautils.metadatamanifest.MetadataManifest)
	 */
	@Override
	public List<BulkAVUOperationResponse> processManifest(final MetadataManifest metadataManifest)
			throws JargonException {
		log.info("processManifest()");
		if (metadataManifest == null) {
			throw new IllegalArgumentException("metadataManifest is null");
		}

		final List<BulkAVUOperationResponse> responses = new ArrayList<>();

		// TODO: consider cache strategy for objStat data - mcc
		final CollectionAndDataObjectListAndSearchAO collectionSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());

		CollectionAO collectionAO = null;
		DataObjectAO dataObjectAO = null;
		String path;
		StringBuilder sb;

		ObjStat objStat = new ObjStat();
		AvuData avuData;

		log.info("metadataManifest:{}", metadataManifest);
		for (final MetadataManifestOperation operation : metadataManifest.getOperation()) {
			log.info("operation:{}", operation);

			// TODO: brittle, need to validate paths for / etc
			if (metadataManifest.getParentIrodsTargetPath().isEmpty()) {
				log.debug("using absolute path");
				path = operation.getIrodsPath();
			} else {
				log.debug("using relative path");
				sb = new StringBuilder();
				sb.append(metadataManifest.getParentIrodsTargetPath());
				sb.append("/");
				sb.append(operation.getIrodsPath());
				path = sb.toString();
			}

			// simple cache to avoid double reads of objStat

			if (!objStat.getAbsolutePath().equals(path)) {
				log.debug("obtaining objstat for path:{}", path);
				objStat = collectionSearchAO.retrieveObjectStatForPath(path);
			}

			avuData = AvuData.instance(operation.getAttribute(), operation.getValue(), operation.getUnit());

			try {
				if (objStat.isSomeTypeOfCollection()) {
					if (collectionAO == null) {
						collectionAO = getIrodsAccessObjectFactory().getCollectionAO(getIrodsAccount());
					}

					collectionAO.addAVUMetadata(path, avuData);

				} else {
					if (dataObjectAO == null) {
						dataObjectAO = getIrodsAccessObjectFactory().getDataObjectAO(getIrodsAccount());
					}

					dataObjectAO.addAVUMetadata(path, avuData);
				}

				responses.add(BulkAVUOperationResponse.instance(ResultStatus.OK, avuData, ""));

			} catch (final Exception e) {
				log.warn("error in avu operation, will check fail fast");

				if (metadataManifest.getFailureMode() == FailureMode.FAIL_FAST) {
					log.error("error in avu operation under fail_fast mode:{}", operation, e);
					throw e;
				} else { // TODO: trap for other modes? right now only ignore
					log.warn("log and ignore avu action", e);
					responses.add(BulkAVUOperationResponse.instance(ResultStatus.OTHER_ERROR, avuData, e.getMessage()));
				}

			}

		}

		return responses;
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public MetadataManifestProcessorImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

}
