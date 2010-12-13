/**
 *
 */
package org.irods.jargon.core.unittest;

import org.irods.jargon.core.query.AVUQueryElementTest;
import org.irods.jargon.core.query.IRODSQueryResultRowTest;
import org.irods.jargon.core.query.IRODSQueryTranslatorTest;
import org.irods.jargon.core.query.SelectFieldTest;
import org.irods.jargon.core.query.TranslatedIRODSQueryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { IRODSQueryTranslatorTest.class,
// IRODSQueryResultRowTest.class, TODO: fix mockito issues
		SelectFieldTest.class, TranslatedIRODSQueryTest.class, AVUQueryElementTest.class })
public class IRODSQueryTests {

}
