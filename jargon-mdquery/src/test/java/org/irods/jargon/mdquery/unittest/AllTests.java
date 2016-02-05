package org.irods.jargon.mdquery.unittest;

import org.irods.jargon.mdquery.serialization.MetadataQueryJsonServiceTest;
import org.irods.jargon.mdquery.service.MetadataQueryServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MetadataQueryJsonServiceTest.class,
		MetadataQueryServiceImplTest.class })
public class AllTests {

}
