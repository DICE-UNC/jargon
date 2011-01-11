package org.irods.jargon.core.unittest;

import org.irods.jargon.core.query.IRODSQueryTest;
import org.irods.jargon.core.query.IRODSQueryTranslatorTest;
import org.irods.jargon.core.query.TranslatedIRODSQueryTest;
import org.irods.jargon.core.remoteexecute.RemoteExecuteServiceImplTest;
import org.irods.jargon.core.security.IRODSPasswordUtilitiesTest;
import org.irods.jargon.core.utils.LocalFileUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConnectionTests.class, IRODSQueryTest.class,
		IRODSQueryTranslatorTest.class, TranslatedIRODSQueryTest.class,
		ProtocolTests.class, AOTests.class, FileTests.class, RuleTests.class,
		PackingInstructionTests.class, DomainTests.class, TransferTests.class,
		LocalFileUtilsTest.class, RemoteExecuteServiceImplTest.class,
		IRODSPasswordUtilitiesTest.class })
public class AllTests {

}
