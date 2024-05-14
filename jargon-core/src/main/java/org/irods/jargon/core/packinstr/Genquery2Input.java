package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a packing instruction for GenQuery2 API operations.
 */
public class Genquery2Input extends AbstractIRODSPackingInstruction {
	
	public static final String PI_TAG = "Genquery2Input_PI";
	
	public static final int GENQUERY2_API_NBR = 10221;

	private String queryString = "";
	private String zone = "";
	private int sqlOnly = 0;
	private int columnMappings = 0;
	
	/**
	 * Creates a packing instruction for querying the catalog.
	 * 
	 * @param queryString The GenQuery2 string to execute.
	 * @param zone        The zone to execute the query against.
	 * @return {@link Genquery2Input}
	 */
	public static Genquery2Input instanceForQuery(final String queryString, final String zone) {
		Genquery2Input input = new Genquery2Input();
		input.setApiNumber(GENQUERY2_API_NBR);
		input.queryString = queryString;
		input.zone = zone;
		return input;
	}

	/**
	 * Creates a packing instruction for generating SQL from the passed GenQuery2
	 * string.
	 * 
	 * @param queryString The GenQuery2 string to convert to SQL.
	 * @param zone        The zone to execute the operation against.
	 * @return {@link Genquery2Input}
	 */
	public static Genquery2Input instanceForSqlOnly(final String queryString, final String zone) {
		Genquery2Input input = new Genquery2Input();
		input.setApiNumber(GENQUERY2_API_NBR);
		input.queryString = queryString;
		input.zone = zone;
		input.sqlOnly = 1;
		return input;
	}

	/**
	 * Creates a packing instruction for retrieving the GenQuery2 column mappings
	 * supported by the iRODS Provider.
	 * 
	 * @param zone The zone to execute the operation against.
	 * @return {@link Genquery2Input}
	 */
	public static Genquery2Input instanceForColumnMappings(final String zone) {
		Genquery2Input input = new Genquery2Input();
		input.setApiNumber(GENQUERY2_API_NBR);
		input.zone = zone;
		input.columnMappings = 1;
		return input;
	}
	
	public String getQueryString() {
		return queryString;
	}

	public String getZone() {
		return zone;
	}

	public int getSqlOnlyValue() {
		return sqlOnly;
	}

	public int getColumnMappingsValue() {
		return columnMappings;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		return new Tag(PI_TAG, new Tag[] {
			new Tag("query_string", getQueryString()),
			new Tag("zone", getZone()),
			new Tag("sql_only", getSqlOnlyValue()),
			new Tag("column_mappings", getColumnMappingsValue())
		});
	}

}
