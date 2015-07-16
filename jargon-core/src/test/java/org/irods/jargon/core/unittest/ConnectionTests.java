package org.irods.jargon.core.unittest;

import org.irods.jargon.core.connection.ClientServerNegotationPolicyFromPropertiesBuilderTest;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicyTest;
import org.irods.jargon.core.connection.ConnectionProgressStatusTest;
import org.irods.jargon.core.connection.DefaultPropertiesJargonConfigTest;
import org.irods.jargon.core.connection.DiscoveredServerPropertiesCacheTest;
import org.irods.jargon.core.connection.EnvironmentalInfoAccessorTest;
import org.irods.jargon.core.connection.IRODSAccountTest;
import org.irods.jargon.core.connection.IRODSServerPropertiesTest;
import org.irods.jargon.core.connection.IRODSSessionTest;
import org.irods.jargon.core.connection.IRODSSimpleConnectionTest;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManagerTest;
import org.irods.jargon.core.connection.PAMAuthTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSSimpleConnectionTest.class,
		EnvironmentalInfoAccessorTest.class,
		IRODSSimpleProtocolManagerTest.class, IRODSSessionTest.class,
		IRODSServerPropertiesTest.class,
		DefaultPropertiesJargonConfigTest.class,
		ConnectionProgressStatusTest.class, PAMAuthTest.class,
		DiscoveredServerPropertiesCacheTest.class, IRODSAccountTest.class,
		ClientServerNegotationPolicyFromPropertiesBuilderTest.class,
		ClientServerNegotiationPolicyTest.class })
public class ConnectionTests {

}
