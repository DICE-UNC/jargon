package org.irods.jargon.usertagging.unittest;

import org.irods.jargon.usertagging.FreeTaggingServiceImplTest;
import org.irods.jargon.usertagging.IRODSTaggingServiceTest;
import org.irods.jargon.usertagging.TaggingServiceFactoryImplTest;
import org.irods.jargon.usertagging.UserTagCloudServiceImplTest;
import org.irods.jargon.usertagging.domain.IRODSTagGroupingTest;
import org.irods.jargon.usertagging.domain.IRODSTagValueTest;
import org.irods.jargon.usertagging.domain.TagCloudEntryTest;
import org.irods.jargon.usertagging.domain.TagQuerySearchResultTest;
import org.irods.jargon.usertagging.domain.UserTagCloudViewTest;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { IRODSTagValueTest.class, IRODSTaggingServiceTest.class, IRODSTagGroupingTest.class, FreeTaggingServiceImplTest.class, TagCloudEntryTest.class,
	UserTagCloudViewTest.class, UserTagCloudServiceImplTest.class, TagQuerySearchResultTest.class, TaggingServiceFactoryImplTest.class, IRODSStarringServiceImplTest.class})
public class AllTests {

}
