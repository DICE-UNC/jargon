package org.irods.jargon.core.query;

import java.util.Date;

import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.junit.Assert;
import org.junit.Test;

public class MetaDataAndDomainDataTest {

	@Test
	public void testAsAvu() throws Exception {
		MetaDataAndDomainData data = MetaDataAndDomainData.instance(MetadataDomain.COLLECTION, "id", "foo", 1,
				new Date(), new Date(), 1, "attrib", "value", "unit");
		AvuData actual = data.asAvu();
		Assert.assertEquals(data.getAvuAttribute(), actual.getAttribute());
		Assert.assertEquals(data.getAvuValue(), actual.getValue());
		Assert.assertEquals(data.getAvuUnit(), actual.getUnit());

	}

}
