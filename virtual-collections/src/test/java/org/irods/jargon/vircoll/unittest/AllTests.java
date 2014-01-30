package org.irods.jargon.vircoll.unittest;

import org.irods.jargon.vircoll.impl.CollectionBasedVirtualCollectionTest;
import org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollectionImplTest;
import org.irods.jargon.vircoll.impl.VirtualCollectionFactoryImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CollectionBasedVirtualCollectionTest.class,
		StarredFoldersVirtualCollectionImplTest.class,
		VirtualCollectionFactoryImplTest.class })
public class AllTests {

}
