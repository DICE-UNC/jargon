/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.io.File;
import java.util.Properties;
import junit.framework.TestCase;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mikeconway
 */
public class LocalFileSystemModelTest {

    private static Properties testingProperties = new Properties();
    private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
    private static ScratchFileUtils scratchFileUtils = null;
    public static final String IRODS_TEST_SUBDIR_PATH = "LocalFileSystemModelTest";
    private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

    public LocalFileSystemModelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
        testingProperties = testingPropertiesLoader.getTestProperties();
        scratchFileUtils = new ScratchFileUtils(testingProperties);
        scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreate() throws Exception {
        String testFileName = "testPutOverwriteFileNotInIRODS.txt";
        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
        String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);
        File testFile = new File(testFileName);
        LocalFileNode localFileNode = new LocalFileNode(testFile);
        LocalFileSystemModel localFileSystemModel = new LocalFileSystemModel(localFileNode);
        TestCase.assertNotNull("null local file system model, could not create", localFileSystemModel);
    }
}
