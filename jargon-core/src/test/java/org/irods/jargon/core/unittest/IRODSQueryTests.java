/**
 *
 */
package org.irods.jargon.core.unittest;

import org.irods.jargon.core.query.AVUQueryElementTest;
import org.irods.jargon.core.query.GenQuerySelectFieldTest;
import org.irods.jargon.core.query.IRODSGenQueryBuilderTest;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilderTest;
import org.irods.jargon.core.query.IRODSGenQueryTranslatorTest;
import org.irods.jargon.core.query.IRODSSimpleQueryResultSetTest;
import org.irods.jargon.core.query.SimpleQueryTest;
import org.irods.jargon.core.query.TranslatedIRODSQueryTest;
import org.irods.jargon.core.query.UserFilePermissionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSGenQueryTranslatorTest.class, GenQuerySelectFieldTest.class, TranslatedIRODSQueryTest.class,
		AVUQueryElementTest.class, SimpleQueryTest.class, IRODSSimpleQueryResultSetTest.class,
		UserFilePermissionTest.class, IRODSGenQueryBuilderTest.class, IRODSGenQueryFromBuilderTest.class })
public class IRODSQueryTests {

}
