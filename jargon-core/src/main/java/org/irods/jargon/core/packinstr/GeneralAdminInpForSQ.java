package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;

public class GeneralAdminInpForSQ extends AbstractIRODSPackingInstruction {

	private static final String PI_TAG = "generalAdminInp_PI";
	private static final String ARG0 = "arg0";
	private static final String ARG1 = "arg1";
	private static final String ARG2 = "arg2";
	private static final String ARG3 = "arg3";
	private static final String ARG4 = "arg4";
	private static final String ARG5 = "arg5";
	private static final String ARG6 = "arg6";
	private static final String ARG7 = "arg7";
	private static final String ARG8 = "arg8";
	private static final String ARG9 = "arg9";
	private static final String BLANK = "";
	private static final String ADMIN_OBJ = "specificQuery";
	private static final String SQ_ADD = "add";
	private static final String SQ_RM = "rm";

	private String sqlQuery = "";
	private String alias = "";
	private String action;

	/**
	 * Generate the packing instruction suitable for creating a Specific Query
	 * iadmin asq
	 *
	 * @param specificQuery
	 *            {@link org.irods.jargon.core.pub.domain.SpecificQueryDefinition}
	 *            to be added to iRODS.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static final GeneralAdminInpForSQ instanceForAddSpecificQuery(
			final SpecificQueryDefinition specificQuery) throws JargonException {

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"null SpecificQueryDefinition object");
		}
		String query = specificQuery.getSql();
		String alias = specificQuery.getAlias();

		GeneralAdminInpForSQ generalAdminInpForSQ = new GeneralAdminInpForSQ(
				query, alias, SQ_ADD);
		return generalAdminInpForSQ;
	}

	/**
	 * Generate the packing instruction suitable for removing a Specific Query
	 * (iadmin rsq)
	 * <p>
	 * NOTE: This method has the same effect as removing the Specific Query by
	 * Alias. The server api does not accept both arguments for remove, and if
	 * an SQL query string is provided for remove, it may have the affect of
	 * removing more than this Specific Query object. <br>
	 * Use GeneralAdminInpForSQ.instanceForRemoveAllSpecificQueryBySQL for
	 * explicitly removing Specific Queries by SQL query.
	 *
	 * @param specificQuery
	 *            {@link org.irods.jargon.core.pub.domain.SpecificQueryDefinition}
	 *            to be removed from iRODS.
	 * @return {@link GeneralAdminInpForSQ}
	 * @throws JargonException
	 *
	 **/
	public static final GeneralAdminInpForSQ instanceForRemoveSpecificQuery(
			final SpecificQueryDefinition specificQuery) throws JargonException {

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"null SpecificQueryDefinition object");
		}

		return instanceForRemoveSpecificQueryByAlias(specificQuery.getAlias());
	}

	/**
	 * Generate the packing instruction suitable for removing a Specific Query
	 * (iadmin rsq)
	 *
	 * @param alias
	 *            {@code String} with the Specific Query alias identifier.
	 * @return {@link GeneralAdminInpForSQ}
	 * @throws JargonException
	 *
	 **/
	public static final GeneralAdminInpForSQ instanceForRemoveSpecificQueryByAlias(
			final String alias) throws JargonException {

		if ((alias == null) || (alias.isEmpty())) {
			throw new IllegalArgumentException("null or missing alias name");
		}

		GeneralAdminInpForSQ generalAdminInpForSQ = new GeneralAdminInpForSQ(
				BLANK, alias, SQ_RM);
		return generalAdminInpForSQ;
	}

	/**
	 * Generate the packing instruction suitable for removing a Specific Query
	 * (iadmin rsq)
	 *
	 * <p>
	 * NOTE: This method will remove all Specific Queries whose SQL query
	 * strings match the one provided
	 *
	 * @param query
	 *            {@code String} with the Specific Query SQL query
	 *            identifier.
	 * @return {@link GeneralAdminInpForSQ}
	 * @throws JargonException
	 *
	 **/
	public static final GeneralAdminInpForSQ instanceForRemoveAllSpecificQueryBySQL(
			final String query) throws JargonException {

		if ((query == null) || (query.isEmpty())) {
			throw new IllegalArgumentException("null or missing SQL query");
		}

		GeneralAdminInpForSQ generalAdminInpForSQ = new GeneralAdminInpForSQ(
				query, BLANK, SQ_RM);
		return generalAdminInpForSQ;
	}

	private GeneralAdminInpForSQ(final String query, final String alias,
			final String action) {

		// if this is an asq require both query and alias
		// or if it is an rsq just query OR alias is required

		if (action == SQ_ADD) {
			if (query == null || query.isEmpty()) {
				throw new IllegalArgumentException(
						"null or missing SQL statement");
			}

			if (alias == null || alias.isEmpty()) {
				throw new IllegalArgumentException("null or missing alias");
			}
		} else if (action == SQ_RM) {
			if ((query == null || query.isEmpty())
					&& (alias == null || alias.isEmpty())) {
				throw new IllegalArgumentException(
						"either sqlQuery or alias is required");
			}
		}

		setApiNumber(GeneralAdminInp.GEN_ADMIN_INP_API_NBR);
		sqlQuery = query;
		this.alias = alias;
		this.action = action;
	}

	@Override
	public Tag getTagValue() throws JargonException {

		String arg2 = getSqlQuery();
		String arg3 = getAlias();

		// need to shuffle things a bit for the specific query remove function
		// which
		// allows either an sql query or alias name as arg2 and it does not use
		// arg3 at all
		// the server api code decides whether arg2 is a query or alias
		// if it is a query it will remove all matching queries whether or not a
		// unique alias
		// is supplied!

		if (action.equals(SQ_RM)) {

			// check to see if this is remove by alias - if so put alias name in
			// arg2
			if (arg2.equals(BLANK)) {
				arg2 = arg3;
				arg3 = BLANK;
			}
		}

		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(ARG0, getAction()),
				new Tag(ARG1, ADMIN_OBJ), new Tag(ARG2, arg2),
				new Tag(ARG3, arg3), new Tag(ARG4, BLANK),
				new Tag(ARG5, BLANK), new Tag(ARG6, BLANK),
				new Tag(ARG7, BLANK), new Tag(ARG8, BLANK),
				new Tag(ARG9, BLANK) });

		return message;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(final String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(final String alias) {
		this.alias = alias;
	}

	public String getAction() {
		return action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

}
