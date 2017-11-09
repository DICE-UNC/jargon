/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.aohelper.AOHelper;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.RodsGenQueryEnum;

/**
 * Helper functions for ResourceAO access object.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
class ResourceAOHelper extends AOHelper {

	/**
	 * Default constructor
	 *
	 * @throws JargonException
	 */
	protected ResourceAOHelper(final IRODSAccount irodsAccount, final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

	}

	/**
	 * Build a string with selects for resource
	 *
	 * @return {@code String} with a select statement to get resource data via
	 *         GenQuery. "SELECT" is included.
	 * @throws GenQueryBuilderException
	 */
	protected IRODSGenQueryBuilder buildResourceSelectsClassic() throws GenQueryBuilderException {
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_ZONE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_TYPE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CLASS_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_LOC)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_VAULT_PATH)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_INFO)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_COMMENT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_MODIFY_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_STATUS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_PARENT)
				// .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_CHILDREN)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_CONTEXT);

		return builder;
	}

	/**
	 * Build selects for resource
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder}
	 */
	protected void buildResourceSelects(final IRODSGenQueryBuilder builder) throws JargonException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_ZONE_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_TYPE_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CLASS_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_LOC)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_VAULT_PATH)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_INFO)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_COMMENT)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CREATE_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_MODIFY_TIME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_STATUS);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}

	}

	/**
	 * Build genquery selects for a resource query against the new composable
	 * resource tree
	 *
	 * @return
	 * @throws GenQueryBuilderException
	 */
	IRODSGenQueryBuilder buildResourceSelectsComposable() throws GenQueryBuilderException {
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_ZONE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_TYPE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CLASS_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_LOC)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_VAULT_PATH)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_FREE_SPACE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_INFO)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_COMMENT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_MODIFY_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_STATUS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_PARENT)
				// .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_CHILDREN)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_R_RESC_CONTEXT);

		return builder;

	}

	List<String> formatImmediateChildren(final String childrenString) {

		if (childrenString == null) {
			throw new IllegalArgumentException("null childrenString");
		}

		List<String> immediateChildren = new ArrayList<String>();

		// empty list if no children
		if (childrenString.isEmpty()) {
			return immediateChildren;
		}

		String[] items = childrenString.split(";");

		for (String item : items) {
			if (item.isEmpty()) {
				continue;
			}

			int idxBracket = item.indexOf("{");

			if (idxBracket > -1) {
				immediateChildren.add(item.substring(0, idxBracket));
			} else {
				immediateChildren.add(item);
			}

		}

		return immediateChildren;

	}

}
