/**
 * 
 */
package org.irods.jargon.mdquery.serialization;

import java.io.IOException;

import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.MetadataQueryException;
import org.irods.jargon.mdquery.MetadataQueryRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Service to create and parse JSON representations of metadata queries
 * 
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryJsonService {

	public static final Logger log = LoggerFactory
			.getLogger(MetadataQueryJsonService.class);

	private final ObjectMapper objectMapper;

	public MetadataQueryJsonService() {
		this.objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.ALWAYS);
	}

	/**
	 * Create a <code>String</code>-ified JSON representation of the metadata
	 * query
	 * 
	 * @param metadataQuery
	 *            {@link MetadataQuery} transfer object
	 * @return <code>String</code> with JSON representing the metadata query
	 */
	public String jsonFromMetadataQuery(final MetadataQuery metadataQuery) {
		if (metadataQuery == null) {
			throw new IllegalArgumentException("null or empty metadataQuery");
		}
		try {
			return objectMapper.writeValueAsString(metadataQuery);
		} catch (JsonProcessingException e) {
			log.error("error creating JSON from metadata query", e);
			throw new MetadataQueryRuntimeException("error creating JSON", e);
		}
	}

	/**
	 * Turn JSON back to a metadata query
	 * 
	 * @param jsonString
	 *            <code>String</code> that is a valid JSON representation of a
	 *            metadata query
	 * @return {@link MetadataQuery} based on the JSON serialization
	 * @throws MetadataQueryException
	 */
	public MetadataQuery metadataQueryFromJson(final String jsonString)
			throws MetadataQueryException {
		if (jsonString == null || jsonString.isEmpty()) {
			throw new IllegalArgumentException("null or empty jsonString");
		}

		try {
			return objectMapper.readValue(jsonString, MetadataQuery.class);
		} catch (IOException e) {
			log.error("exception reading JSON:{}", jsonString, e);
			throw new MetadataQueryException("exception reading json", e);
		}

	}
}
