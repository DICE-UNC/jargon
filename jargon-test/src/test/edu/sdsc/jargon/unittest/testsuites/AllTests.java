package edu.sdsc.jargon.unittest.testsuites;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Main test suite to run all Jargon unit tests
 * @author Mike Conway, DICE
 * @since 10/10/2009
 *
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
  TestingUtilitiesTest.class, 
  IRODSTests.class, 
  IRODSPackingInstructionTests.class,
  ICommandInvokerTests.class,
  LocalFileTests.class,
  GeneralFileSystemTests.class,
  IRODSMidLevelServicesTest.class,
  IRODSQueryTests.class,
  IRODSAccessObjectTests.class
})

public class AllTests {

}
