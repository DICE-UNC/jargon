/**
 *
 */
package edu.sdsc.jargon.unittest.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import edu.sdsc.jargon.testutils.icommandinvoke.icommands.IlsCommandTest;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.ImetaCommandTest;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.ImkdirCommandTest;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.IputCommandTest;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.IreplCommandTest;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.IrmCommandTest;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.UsersCommandTest;


/**
 * Test suite for icommand wrapper libraries
 * @author Mike Conway, DICE
 * @since 10/10/2009
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({IlsCommandTest.class,
    ImkdirCommandTest.class,
    IputCommandTest.class,
    IrmCommandTest.class,
    IlsCommandTest.class,
    UsersCommandTest.class,
    ImkdirCommandTest.class,
    ImetaCommandTest.class,
    IreplCommandTest.class,
    ImetaCommandTest.class
})
public class ICommandInvokerTests {
}
