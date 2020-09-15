/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author conwaymc
 *
 */
class ApiPluginExecutorImpl extends IRODSGenericAO {

	private static Logger log = LoggerFactory.getLogger(ApiPluginExecutorImpl.class);
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public ApiPluginExecutorImpl(IRODSSession irodsSession, IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	public <InputType, OutputType> OutputType callPluggableApi(int apiNumber, InputType input) throws JargonException {
		log.info("callPluggableApi())");
		if (apiNumber <= 0) {
			throw new IllegalArgumentException("invalid api number");
		}
		if (input == null) {
			throw new IllegalArgumentException("null input");
		}

		log.info("apiNumber:{}", apiNumber);
		log.info("input:{}", input);

		try {
			String jsonInput = mapper.writeValueAsString(input);
			log.debug("jsonInput:{}", jsonInput);
			return null;

		} catch (JsonProcessingException e) {
			log.error("Invalid json", e);
			throw new JargonException("invalid json", e);
		}

	}

}
