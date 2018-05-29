package org.irods.jargon.core.pub.aohelper;

import static java.util.Arrays.asList;
import static org.irods.jargon.core.pub.aohelper.CollectionAOHelper.buildCollectionListEntryFromResultSetRowForCollectionQuery;
import static org.irods.jargon.core.pub.aohelper.CollectionAOHelper.buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQuerySelectField;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.junit.Test;

public final class CollectionAOHelperTest {

	@Test
	public void testBuildCollectionListEntryFromResultSetRowForCollectionQuery() throws JargonException {
		final String[] columnNames = { "0", "1", "2", "3", "4", "5", "6", "7" };
		final String[] columnValues = { "/path/to", "/path/to/collection", "1000000000", "1000000000", "10000", "user",
				"zone", "linkPoint" };
		final IRODSQueryResultRow row = IRODSQueryResultRow.instance(asList(columnValues), asList(columnNames));
		final CollectionAndDataObjectListingEntry entry = buildCollectionListEntryFromResultSetRowForCollectionQuery(
				row, 0);
		assertEquals("set incorrect collection type", SpecColType.LINKED_COLL, entry.getSpecColType());
	}

	@Test
	public void testBuildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry()
			throws GenQueryBuilderException {
		final IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(false, null);
		buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry(builder);
		final List<GenQuerySelectField> selectFields = builder.exportIRODSQueryFromBuilder(1)
				.convertToTranslatedIRODSGenQuery().getSelectFields();
		int numTypeCols = 0;
		for (GenQuerySelectField field : selectFields) {
			final String typeCol = RodsGenQueryEnum.COL_COLL_TYPE.getName();
			if (field.getSelectFieldColumnName().equals(typeCol)) {
				numTypeCols++;
			}
		}
		assertEquals("should be 1 and only 1 type column", 1, numTypeCols);
	}
}
