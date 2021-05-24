/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.apiplugin.PluggableApiRequest;
import org.irods.jargon.core.apiplugin.PluggableApiResponse;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.BytesBuff;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author conwaymc
 *
 */
class ApiPluginExecutorImpl<I extends PluggableApiRequest, O extends PluggableApiResponse> extends IRODSGenericAO {

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

	public O callPluggableApi(int apiNumber, I request) throws JargonException {
		log.info("callPluggableApi())");

		if (apiNumber <= 0) {
			throw new IllegalArgumentException("invalid api number");
		}

		if (request == null) {
			throw new IllegalArgumentException("null input");
		}

		log.info("apiNumber:{}", apiNumber);
		log.info("input:{}", request);

		try {
			String jsonInput = mapper.writeValueAsString(request);
			log.debug("jsonInput:{}", jsonInput);
			BytesBuff bytesBuff = BytesBuff.instance(jsonInput, apiNumber);
			Tag response;
			ObjStat objStat;
			response = this.getIRODSAccessObjectFactory().getIrodsSession().currentConnection(this.getIRODSAccount())
					.irodsFunction(bytesBuff);

			log.debug("response from objStat: {}", response.parseTag());
			return null;

		} catch (JsonProcessingException e) {
			log.error("Invalid json", e);
			throw new JargonException("invalid json", e);
		}

	}

}
