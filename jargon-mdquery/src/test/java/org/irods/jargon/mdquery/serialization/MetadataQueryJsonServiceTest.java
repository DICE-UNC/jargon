package org.irods.jargon.mdquery.serialization;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.MetadataQueryElement;
import org.junit.Test;

public class MetadataQueryJsonServiceTest {

	@Test
	public void testMetadataQueryToJson() {
		MetadataQuery metadataQuery = new MetadataQuery();
		metadataQuery.setPathHint("/a/path");
		List<String> values = new ArrayList<String>();
		values.add("val1");

		MetadataQueryElement metadataElement = new MetadataQueryElement();
		metadataElement.setAttributeName("name1");
		metadataElement.setOperator(AVUQueryOperatorEnum.EQUAL);
		metadataElement.setValue(values);
		metadataQuery.getMetadataQueryElements().add(metadataElement);

		metadataElement = new MetadataQueryElement();
		metadataElement.setAttributeName("name2");
		metadataElement.setOperator(AVUQueryOperatorEnum.EQUAL);
		metadataElement.setValue(values);
		metadataQuery.getMetadataQueryElements().add(metadataElement);

		MetadataQueryJsonService metadataQueryJsonService = new MetadataQueryJsonService();
		String actual = metadataQueryJsonService
				.jsonFromMetadataQuery(metadataQuery);
		Assert.assertNotNull(actual);
		Assert.assertFalse(actual.isEmpty());
		System.out.println(actual);

	}

}
