package org.irods.jargon.core.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains an index of extensible meta-data attributes for this query. This
 * object is thread-safe.
 *
 * Refer to the README.TXT in the /modules subdirectory of the main IRODS
 * download. Provides bi-directional lookup of extensible meta-data.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ExtensibleMetaDataMapping {

	private static Logger log = LoggerFactory
			.getLogger(ExtensibleMetaDataMapping.class);

	// Map will be wrapped immutable at construction time
	private Map<String, String> extensibleMappings = new HashMap<String, String>();

	/**
	 * Create an object to hold a mapping of extensible metadata columns and
	 * values. Note that this method will override the stored mappings from a
	 * previous construction, therefore, the access methods are synchronized.
	 *
	 * @param extensibleMappings
	 *            <code>Map<String,String><code> containing
	 * @return {@link ExtensibleMetaDataMapping}
	 * @throws JargonException
	 */
	public synchronized static ExtensibleMetaDataMapping instance(
			final Map<String, String> extensibleMappings)
			throws JargonException {

		log.debug("cacheing and returning fresh extensibleMetaDataMapping");
		Map<String, String> copiedExtensibleMappings = new HashMap<String, String>(
				extensibleMappings);
		return new ExtensibleMetaDataMapping(
				Collections.unmodifiableMap(copiedExtensibleMappings));
	}

	private ExtensibleMetaDataMapping(
			final Map<String, String> extensibleMappings)
			throws JargonException {
		if (extensibleMappings == null || extensibleMappings.size() == 0) {
			throw new JargonException("null or empty extensible mappings");
		}
		this.extensibleMappings = extensibleMappings;
	}

	/**
	 * Given a column name (which maps to the ext_col_names_t structure in the
	 * IRODS extendediCat.h), return the numeric value which should be sent in
	 * an IRODS query.
	 *
	 * Note that method can return {@code null}
	 *
	 * @param columnName
	 *            <code>String<code> containing the column name of the extensible metadata
	 * @return {@code String} containing the corresponding index, or
	 *         {@code null} if no match is found.
	 */
	public String getIndexFromColumnName(final String columnName) {
		String index = extensibleMappings.get(columnName);
		return index;
	}

	/**
	 * For a given numeric index, get the equivalent column name.
	 *
	 * Note that method can return {@code null}
	 *
	 * @param index
	 *            {@code String} containing the value of the extensible
	 *            metadata numeric index.
	 * @return {@code String} with the extensible metaata column name, or
	 *         {@code null} if not found.
	 */
	public String getColumnNameFromIndex(final String index) {
		String columnName = null;
		String foundIntValue = "";
		for (String key : extensibleMappings.keySet()) {
			foundIntValue = extensibleMappings.get(key);
			if (foundIntValue.equals(index)) {
				columnName = key;
				break;
			}
		}
		return columnName;
	}

}
