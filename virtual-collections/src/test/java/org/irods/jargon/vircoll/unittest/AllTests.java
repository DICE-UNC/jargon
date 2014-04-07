package org.irods.jargon.vircoll.unittest;

import org.irods.jargon.vircoll.impl.CollectionBasedVirtualCollectionTest;
import org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollectionImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CollectionBasedVirtualCollectionTest.class,
		StarredFoldersVirtualCollectionImplTest.class })
public class AllTests {

}
