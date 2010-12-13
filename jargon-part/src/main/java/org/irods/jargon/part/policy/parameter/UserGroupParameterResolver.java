/**
 * 
 */
package org.irods.jargon.part.policy.parameter;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.part.exception.PartException;
import org.irods.jargon.part.policydriven.PolicyManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserGroupParameterResolver extends AbstractDynamicPropertyResolver {

	public final static Logger log = LoggerFactory
	.getLogger(UserGroupParameterResolver.class);
	
	/**
	 * Default constructor.
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @throws PartException
	 */
	protected UserGroupParameterResolver(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) throws PartException {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policy.parameter.DynamicPropertyResolver#resolve()
	 */
	@Override
	public DynamicPropertyValues resolve() throws PartException {
		log.info("resolve() for userGroup");
		
		UserGroupAO userGroupAO;
		try {
			userGroupAO = this.getIrodsAccessObjectFactory().getUserGroupAO(this.getIrodsAccount());
			List<UserGroup> userGroups = userGroupAO.findUserGroupsForUser(this.getIrodsAccount().getUserName());
			List<String> userGroupNames = new ArrayList<String>();
			
			for (UserGroup userGroup : userGroups) {
				userGroupNames.add(userGroup.getUserGroupName());
			}

			return new DynamicPropertyValues("USER_GROUP", userGroupNames);
			
		} catch (JargonException e) {
			log.error("exception listing user groups for user", e);
			throw new PartException(e);
		}
		
		
	}

}
