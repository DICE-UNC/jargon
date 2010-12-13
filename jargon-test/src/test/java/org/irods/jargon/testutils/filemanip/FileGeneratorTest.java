/**
 *
 */
package org.irods.jargon.testutils.filemanip;

import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/16/2009
 */
public class FileGeneratorTest {
    private static Properties testProperties;
    private static ScratchFileUtils scratchFileUtils = null;
    private static AssertionHelper assertionHelper = null;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestingPropertiesHelper loader = new TestingPropertiesHelper();
        testProperties = loader.getTestProperties();
        scratchFileUtils = new ScratchFileUtils(testProperties);
        assertionHelper = new AssertionHelper();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testGenerateFileOfFixedLengthGivenName()
        throws Exception {
        //scratchFileUtils.
        String testingDir = "filegentest";
        String testingName = "fgenfixedgiven.dat";

        // make sure the path is there, and get it as an absolute path to tell
        // the file generator where to put the file
        StringBuilder filePathBuilder = new StringBuilder();

        filePathBuilder.append(testingDir);
        filePathBuilder.append('/');
        filePathBuilder.append(testingName);

        String absolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testingDir);
        FileGenerator.generateFileOfFixedLengthGivenName(absolutePath,
            testingName, 10);

        // make sure file generated
        boolean foundFile = scratchFileUtils.checkIfFileExistsInScratch(filePathBuilder.toString());
        TestCase.assertTrue("did not find the generated file", foundFile);
    }

    /**
     * Test method for
     * {@link org.irods.jargon.test.utils.FileGenerator#generateFileOfFixedLengthAndRandomExtension(java.lang.String, int)}
     * .
     */
    @Test
    public void testGenerateRandomFileName() throws Exception {
        String fileName = FileGenerator.generateRandomFileName(6);

        int dotPos = fileName.indexOf('.');
        String namePart = fileName.substring(0, dotPos);
        TestCase.assertTrue(
            "length of file name before extension should be 6, instead got:" +
            namePart, namePart.length() == 6);
    }

    /**
     * Test method for
     * {@link org.irods.jargon.test.utils.FileGenerator#generateRandomNumber(int, int)}
     * .
     */
    @Test
    public void testGenerateRandomNumber() throws Exception {
        int nbr = FileGenerator.generateRandomNumber(10, 20);
        TestCase.assertTrue(
            "did not generate a proper random number, generated:" + nbr,
            (nbr >= 10) && (nbr <= 20));
    }

    /**
     * Test method for
     * {@link org.irods.jargon.test.utils.FileGenerator#generateRandomNumber(int, int)}
     * .
     */
    @Test
    public void testGenerateRandomString() throws Exception {
        String str = FileGenerator.generateRandomString(10);

        TestCase.assertTrue(
            "did not generate a proper random string, generated:" + str,
            str.length() == 10);
    }

    @Test
    public void testGenerateRandomExtension() throws Exception {
        String extension = FileGenerator.generateRandomExtension();
        boolean extFound = false;

        for (int i = 0; i < FileGenerator.fileExtensions.size(); i++) {
            if (FileGenerator.fileExtensions.get(i).equals(extension)) {
                extFound = true;

                break;
            }
        }

        TestCase.assertTrue("did not generate a valid extension, generated:" +
            extension, extFound);
    }
    
    
}
