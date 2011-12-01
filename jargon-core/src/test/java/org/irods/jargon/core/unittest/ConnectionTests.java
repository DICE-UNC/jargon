package org.irods.jargon.core.unittest;

import org.irods.jargon.core.connection.ConnectionProgressStatusTest;
import org.irods.jargon.core.connection.ConnectionProxyDefinitionTest;
import org.irods.jargon.core.connection.DefaultPropertiesJargonConfigTest;
import org.irods.jargon.core.connection.EnvironmentalInfoAccessorTest;
import org.irods.jargon.core.connection.IRODSServerPropertiesTest;
import org.irods.jargon.core.connection.IRODSSessionTest;
import org.irods.jargon.core.connection.IRODSSimpleConnectionTest;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSSimpleConnectionTest.class,
		EnvironmentalInfoAccessorTest.class,
		IRODSSimpleProtocolManagerTest.class,
		ConnectionProxyDefinitionTest.class, IRODSSessionTest.class,
		IRODSServerPropertiesTest.class,
		DefaultPropertiesJargonConfigTest.class,
		ConnectionProgressStatusTest.class })
public class ConnectionTests {

}
