/**
 *
 */
package org.irods.jargon.core.pub.aohelper;

import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.RodsGenQueryEnum;

/**
 * @author Mike Conway - DICE
 *
 */
public class UserGroupAOHelper {

	/**
	 * Add appropriate select statements to the provided builder to query the user
	 * group data in the iCAT
	 *
	 * @param builder {@link IRODSGenQueryBuilder} to which the selects will be
	 *                added
	 * @throws GenQueryBuilderException {@link GenQueryBuilderException}
	 */
	public static void buildSelectsByAppendingToBuilder(final IRODSGenQueryBuilder builder)
			throws GenQueryBuilderException {
		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE);

	}

}
