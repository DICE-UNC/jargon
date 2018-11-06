/**
 * 
 */
package org.irods.jargon.pool.conncache.functtest;

import java.util.concurrent.Callable;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Client {@code Callable} that can access a conn pool
 * 
 * @author conwaymc
 *
 */
public class PoolValidatorClient implements Callable<PoolCallResult> {

	private IRODSAccount irodsAccount;
	private IRODSAccessObjectFactory accessObjectFactory;

	/**
	 * 
	 */
	public PoolValidatorClient(IRODSAccount clientAccount, IRODSAccessObjectFactory accessObjectFactory) {
		this.irodsAccount = clientAccount;
		this.accessObjectFactory = accessObjectFactory;
	}

	@Override
	public PoolCallResult call() throws Exception {
		long finish = 0;
		PoolCallResult result = new PoolCallResult();

		try {
			EnvironmentalInfoAO ao = accessObjectFactory.getEnvironmentalInfoAO(irodsAccount);
			long start = System.currentTimeMillis();
			ao.getIRODSServerCurrentTime();
			finish = System.currentTimeMillis() - start;
			accessObjectFactory.closeSession();
			result.setSuccess(true);

		} catch (Exception e) {
			result.setSuccess(false);
		}
		result.setTime(finish);
		return result;
	}

}
