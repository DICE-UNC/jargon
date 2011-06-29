package org.irods.jargon.datautils.unittest;

import org.irods.jargon.datautils.datacache.DataCacheServiceImplTest;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesServiceImplTest;
import org.irods.jargon.datautils.tree.FileTreeDiffEntryTest;
import org.irods.jargon.datautils.tree.FileTreeDiffUtilityTest;
import org.irods.jargon.datautils.tree.FileTreeNodeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FileTreeDiffEntryTest.class, FileTreeNodeTest.class, FileTreeDiffUtilityTest.class, SynchPropertiesServiceImplTest.class, DataCacheServiceImplTest.class })
public class AllTests {

}
