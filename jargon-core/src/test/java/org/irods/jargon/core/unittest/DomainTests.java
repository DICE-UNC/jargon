package org.irods.jargon.core.unittest;

import org.irods.jargon.core.protovalues.AuditActionEnumTest;
import org.irods.jargon.core.protovalues.UserTypeEnumTest;
import org.irods.jargon.core.pub.domain.AvuDataTest;
import org.irods.jargon.core.pub.domain.CollectionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AvuDataTest.class, UserTypeEnumTest.class,
	CollectionTest.class, AuditActionEnumTest.class })
public class DomainTests {

}
