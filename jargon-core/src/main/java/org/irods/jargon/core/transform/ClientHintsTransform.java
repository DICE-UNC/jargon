/**
 * 
 */
package org.irods.jargon.core.transform;

import java.io.IOException;

import org.irods.jargon.core.exception.dataformat.InvalidDataException;
import org.irods.jargon.core.pub.domain.ClientHints;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Transformation of iRODS client hints API
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class ClientHintsTransform {

	public ClientHints clientHintsFromIrodsJson(final String irodsJson) throws InvalidDataException {
		if (irodsJson == null || irodsJson.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsJson");
		}

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			ClientHints clientHints = objectMapper.readValue(irodsJson, ClientHints.class);
			return clientHints;
		} catch (IOException e) {
			throw new InvalidDataException("exception translating client hints JSON to ClientHints", e);
		}

	}

}
