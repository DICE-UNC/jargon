/**
 *
 */
package edu.sdsc.jargon.unittest.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import edu.sdsc.jargon.testutils.AssertionHelperTest;
import edu.sdsc.jargon.testutils.IRODSTestSetupUtilitiesTest;
import edu.sdsc.jargon.testutils.TestingPropertiesHelperTest;
import edu.sdsc.jargon.testutils.filemanip.FileGeneratorTest;
import edu.sdsc.jargon.testutils.filemanip.ScratchFileUtilsTest;

/**
 * Test suite for testing-centric utlities contained within this library
 * @author Mike Conway, DICE
 * @since 10/10/2009
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  TestingPropertiesHelperTest.class, 
  FileGeneratorTest.class, 
  ScratchFileUtilsTest.class,
  IRODSTestSetupUtilitiesTest.class,
  AssertionHelperTest.class
})
public class TestingUtilitiesTest {
	
}
