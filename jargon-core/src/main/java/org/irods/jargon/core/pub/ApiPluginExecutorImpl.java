/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.BytesBuff;
import org.irods.jargon.core.packinstr.IRodsPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author conwaymc
 *
 */
class ApiPluginExecutorImpl extends IRODSGenericAO implements ApiPluginExecutor {

	private static Logger log = LoggerFactory.getLogger(ApiPluginExecutorImpl.class);

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public ApiPluginExecutorImpl(IRODSSession irodsSession, IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/**
	 * Make a function call using a regular packing instruction as a request and
	 * expecting JSON as a result
	 * 
	 * @param apiNumber {@code int} with the API number
	 * @param irodsPI   {@link IRodsPI}
	 * @return a {@code String} with serialized JSON
	 * @throws JargonException {@link JargonException} for upstream iRODS errors
	 */
	@Override
	public PluggableApiCallResult callPluggableApi(int apiNumber, IRodsPI irodsPI) throws JargonException {

		log.info("callPluggableApi()");

		if (apiNumber <= 0) {
			throw new IllegalArgumentException("apiNumber is not > 0");
		}

		if (irodsPI == null) {
			throw new IllegalArgumentException("irodsPi is null");
		}

		PluggableApiCallResult response = this.getIRODSProtocol().irodsFunctionWithPluggableResult(irodsPI);
		log.debug("pluggable result string:{}", response);
		return response;

	}

	/**
	 * Make a function call using a serialized JSON request and expecting JSON as a
	 * result
	 * 
	 * @param apiNumber {@code int} with the API number
	 * @param request   {@code String}
	 * @return a {@code String} with serialized JSON
	 * @throws JargonException {@link JargonException} for upstream iRODS errors
	 */
	@Override
	public PluggableApiCallResult callPluggableApi(int apiNumber, String request) throws JargonException {
		log.info("callPluggableApi())");

		if (apiNumber <= 0) {
			throw new IllegalArgumentException("invalid api number");
		}

		if (request == null) {
			throw new IllegalArgumentException("null input");
		}

		log.info("apiNumber:{}", apiNumber);
		log.info("input:{}", request);

		log.debug("request:{}", request);
		BytesBuff bytesBuff = BytesBuff.instance(request, apiNumber);

		PluggableApiCallResult response = this.getIRODSAccessObjectFactory().getIrodsSession()
				.currentConnection(this.getIRODSAccount()).readPluggableApiMessage();

		log.debug("response from pluggable api call: {}", response);
		return response;
	}
}
