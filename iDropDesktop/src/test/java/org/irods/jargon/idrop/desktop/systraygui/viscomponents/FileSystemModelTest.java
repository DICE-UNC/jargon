/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.io.File;
import java.util.Properties;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikeconway
 */
public class FileSystemModelTest {

    private static Properties testingProperties = new Properties();
    private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
    private static ScratchFileUtils scratchFileUtils = null;
    public static final String IRODS_TEST_SUBDIR_PATH = "FileSystemModelTest";
    private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
    private static AssertionHelper assertionHelper = null;
    private static IRODSAccount irodsAccount = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
        testingProperties = testingPropertiesLoader.getTestProperties();
        scratchFileUtils = new ScratchFileUtils(testingProperties);
        irodsTestSetupUtilities = new IRODSTestSetupUtilities();
        irodsTestSetupUtilities.initializeIrodsScratchDirectory();
        irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
        assertionHelper = new AssertionHelper();
        irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
    }

    public FileSystemModelTest() {
    }

    /**
     * Test of getRoot method, of class FileSystemModel.
     */
    @Test
    public void testGetRoot() throws Exception {
        String testRoot = IRODS_TEST_SUBDIR_PATH + "/testGetRoot";
        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testRoot);
        FileSystemModel instance = new FileSystemModel(new File(absPath));
        Object expResult = new File(absPath);
        Object result = instance.getRoot();
        assertEquals(expResult, result);
    }

    /**
     * Test of getChild method, of class FileSystemModel.
     */
    @Test
    public void testGetChild() {
        String testRoot = IRODS_TEST_SUBDIR_PATH + "/testGetRoot";
        String childRoot = testRoot + "/hitherechild1";
        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testRoot);
        String otherDir = scratchFileUtils.createAndReturnAbsoluteScratchPath(childRoot);
        FileSystemModel instance = new FileSystemModel(new File(absPath));
        Object expResult = new File(otherDir);
        Object result = instance.getChild(instance.getRoot(), 0);
        assertEquals(expResult, result);
    }

    /**
     * Test of getChildCount method, of class FileSystemModel.
     */
    @Test
    public void testGetChildCount() {
        String testRoot = IRODS_TEST_SUBDIR_PATH + "/testGetRoot";
        String childRoot = testRoot + "/hitherechild1";
        String childRoot2 = testRoot + "/hitherechild2";

        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testRoot);
        String otherDir = scratchFileUtils.createAndReturnAbsoluteScratchPath(childRoot);
        String otherDir2 = scratchFileUtils.createAndReturnAbsoluteScratchPath(childRoot2);

        FileSystemModel instance = new FileSystemModel(new File(absPath));
        int expected = 2;
        int result = instance.getChildCount(instance.getRoot());
        assertEquals(expected, result);
    }

    /**
     * Test of isLeaf method, of class FileSystemModel.
     */
    @Test
    public void testIsLeaf() throws Exception {
         String testRoot = IRODS_TEST_SUBDIR_PATH + "/testIsLeaf";
         String testFileName = "atestfile.txt";
        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testRoot);
        String testFile = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);
        FileSystemModel instance = new FileSystemModel(new File(absPath));
        Object result = instance.getChild(instance.getRoot(), 0);
        assertEquals(true, instance.isLeaf(result));
    }

    /**
     * Test of getIndexOfChild method, of class FileSystemModel.
     */
    @Test
    public void testGetIndexOfChild() {
          String testRoot = IRODS_TEST_SUBDIR_PATH + "/testGetIndexOfChild";
        String childRoot = testRoot + "/hitherechild1";
        String childRoot2 = testRoot + "/hitherechild2";

        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(testRoot);
        String otherDir = scratchFileUtils.createAndReturnAbsoluteScratchPath(childRoot);
        String otherDir2 = scratchFileUtils.createAndReturnAbsoluteScratchPath(childRoot2);

        FileSystemModel instance = new FileSystemModel(new File(absPath));
        File instance2 = new File(otherDir2);
        File parent = new File(absPath);
        int expected = 1;
        int result = instance.getIndexOfChild(parent, instance2);
        assertEquals(expected, result);
    }

    /**
     * Test of valueForPathChanged method, of class FileSystemModel.
     */
    @Test
    public void testValueForPathChanged() {
        System.out.println("valueForPathChanged");
        TreePath path = null;
        Object value = null;
        FileSystemModel instance = null;
        //instance.valueForPathChanged(path, value);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of addTreeModelListener method, of class FileSystemModel.
     */
    @Test
    public void testAddTreeModelListener() {
        System.out.println("addTreeModelListener");
        TreeModelListener listener = null;
        FileSystemModel instance = null;
        //instance.addTreeModelListener(listener);
        // TODO review the generated test code and remove the default call to fail.
       // fail("The test case is a prototype.");
    }

    /**
     * Test of removeTreeModelListener method, of class FileSystemModel.
     */
    @Test
    public void testRemoveTreeModelListener() {
        System.out.println("removeTreeModelListener");
        TreeModelListener listener = null;
        FileSystemModel instance = null;
       // instance.removeTreeModelListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
