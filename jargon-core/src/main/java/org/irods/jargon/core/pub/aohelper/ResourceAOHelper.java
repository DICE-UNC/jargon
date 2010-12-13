/**
 * 
 */
package org.irods.jargon.core.pub.aohelper;

import org.irods.jargon.core.query.RodsGenQueryEnum;

/**
 * Helper functions for ResourceAO access object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ResourceAOHelper extends AOHelper {

	/**
	 * Default constructor
	 */
	public ResourceAOHelper() {
	}

	/**
	 * Build a string with selects for resource
	 * 
	 * @return <code>String</code> with a select statement to get resource data
	 *         via GenQuery. "SELECT" is included.
	 */
	public String buildResourceSelects() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_R_RESC_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_ZONE_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_TYPE_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_CLASS_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_LOC.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_VAULT_PATH.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_FREE_SPACE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_FREE_SPACE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_INFO.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_COMMENT.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_MODIFY_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_R_RESC_STATUS.getName());
		return query.toString();
	}

}
