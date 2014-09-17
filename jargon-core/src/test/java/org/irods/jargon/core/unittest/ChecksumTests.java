package org.irods.jargon.core.unittest;

import org.irods.jargon.core.checksum.ChecksumManagerImplTest;
import org.irods.jargon.core.checksum.LocalChecksumComputerFactoryImplTest;
import org.irods.jargon.core.checksum.MD5LocalChecksumComputerStrategyTest;
import org.irods.jargon.core.checksum.SHA256LocalChecksumComputerStrategyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for checksum computation code
 * 
 * @author Mike Conway - DICE
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ MD5LocalChecksumComputerStrategyTest.class,
		SHA256LocalChecksumComputerStrategyTest.class,
		LocalChecksumComputerFactoryImplTest.class,
		ChecksumManagerImplTest.class })
public class ChecksumTests {

}
