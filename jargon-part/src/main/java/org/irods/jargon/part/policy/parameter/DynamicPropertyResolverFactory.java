/**
 * 
 */
package org.irods.jargon.part.policy.parameter;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.part.exception.PartException;

/**
 * Factory that creates resolvers for various dynamic properties.  The property type must be registered in this class to be incorporated in a policy
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DynamicPropertyResolverFactory {
	
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;
	
	public static final String USER_GROUP_PARAMETER = "USER_GROUP";
	
	/**
	 * Create a factory to create various DynamicPropertyResolvers.  These are used to take symbolic parameters and to return values derived from within iRODS.
	 * @param irodsAccessObjectFactory <code>IRODSAccessObjectFactory</code> that will be used to create objects that interact with iRODS.
	 * @param irodsAccount <code>IRODSAccount</code> with connection information.
	 * @return instance of <code>DynamicPropertyResolverFactory</code>
	 * @throws PartException
	 */
	public static DynamicPropertyResolverFactory instance(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws PartException {
		return new DynamicPropertyResolverFactory(irodsAccessObjectFactory, irodsAccount);
	}
	
	
	private DynamicPropertyResolverFactory(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws PartException {
		
		if (irodsAccessObjectFactory == null) {
			throw new PartException("null irodsAccessObjectFactory");
		}
		
		if (irodsAccount == null) {
			throw new PartException("null irodsAccount");
		}
		
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
		
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}
	
	/**
	 * Given a dynamic parameter name, return an instance that can resolve the parameter by access iRODS data.
	 * @param dynamicParameterName <code>String</code> with the dynamic parameter name that is transformed into data values by accessing iRODS.
	 * @return {@link org.irods.jargon.part.policy.parameter.DynamicPropertyResolver} that access iRODS to create the property list
	 * @throws PartException
	 */
	public DynamicPropertyResolver getInstance(final String dynamicParameterName) throws PartException {
		
		if (dynamicParameterName == null || dynamicParameterName.isEmpty()) {
			throw new PartException("null or empty dynamicParameterName");
		}
		
		if (dynamicParameterName.equals(USER_GROUP_PARAMETER)) {
			return new UserGroupParameterResolver(getIrodsAccessObjectFactory(), getIrodsAccount());
		} else {
			throw new PartException("unable to find a resolver for property:" + dynamicParameterName);
		}
		
	}
	
	

}
