package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import org.junit.Ignore;
import java.io.File;
import java.util.List;
import java.util.Properties;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.desktop.systraygui.utils.TreeUtils;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for IRODSFileSystemModelOld
 * @author mikeconway
 */
public class IRODSFileSystemModelTest {

    private static Properties testingProperties = new Properties();
    private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
    private static ScratchFileUtils scratchFileUtils = null;
    public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileSystemModelTest";
    private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
    private static AssertionHelper assertionHelper = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
        testingProperties = testingPropertiesLoader.getTestProperties();
        scratchFileUtils = new ScratchFileUtils(testingProperties);
        irodsTestSetupUtilities = new IRODSTestSetupUtilities();
        irodsTestSetupUtilities.initializeIrodsScratchDirectory();
        irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
        assertionHelper = new AssertionHelper();
    }

    /**
     * Test of getRoot method, of class IRODSFileSystemModelOld.
     */
    @Test
    public void testGetRoot() throws Exception {
        // build the model and return back the root IRODSNode
        String subdirPrefix = "testListCollectionsUnderPath";
        int count = 2;

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + subdirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdir();
        irodsFile.close();

        String myTarget = "";

        for (int i = 0; i < count; i++) {
            myTarget = targetIrodsCollection + "/c" + ( 10000 + i )
                    + subdirPrefix;
            irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
            irodsFile.mkdir();
            irodsFile.close();
        }

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(targetIrodsCollection, 0);
        IRODSNode rootNode = new IRODSNode(entries.get(0), irodsAccount);

        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(rootNode, irodsAccount);

        irodsFileSystem.close();

        Assert.assertNotNull(null, irodsFileSystemModel);
        IRODSNode testRootNode = (IRODSNode) irodsFileSystemModel.getRoot();
        TestCase.assertEquals("root IRODSNode not properly stored", rootNode.toString(), testRootNode.toString());
    }

    /**
     * Test of getChildCount method, of class IRODSFileSystemModelOld.
     */
    @Test
    public void testGetChildCount() throws Exception {
        String subdirPrefix = "testGetChildCount";
        int count = 2;

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + subdirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdirs();
        irodsFile.close();

        String myTarget = "";

        for (int i = 0; i < count; i++) {
            myTarget = targetIrodsCollection + "/c" + ( 10000 + i )
                    + subdirPrefix;
            irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
            irodsFile.mkdirs();
            irodsFile.close();
        }

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(targetIrodsCollection, 0);
        CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();
        root.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
        root.setParentPath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH));
        root.setPathOrName(targetIrodsCollection);

        IRODSNode testNode = new IRODSNode(root, irodsAccount);
        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(testNode, irodsAccount);
        irodsFileSystem.close();

        int result = irodsFileSystemModel.getChildCount(irodsFileSystemModel.getRoot());
        TestCase.assertEquals("wrong number of children returned", count, result);

    }

    /**
     * Test of getChildCount method, of class IRODSFileSystemModelOld.
     */
    @Test
    public void testGetChildOfChild() throws Exception {
        String parentDirPrefix = "testGetChildOfChild";
        String childDirPrefix = "childOf";


        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + parentDirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdirs();
        irodsFile.close();

        irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection + "/" + childDirPrefix);
        irodsFile.mkdirs();
        irodsFile.close();

        irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection + "/" + childDirPrefix + "/" + childDirPrefix);
        irodsFile.mkdirs();
        irodsFile.close();

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(targetIrodsCollection, 0);
        CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();
        root.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
        root.setParentPath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH));
        root.setPathOrName(parentDirPrefix);

        IRODSNode irodsNode = new IRODSNode(root, irodsAccount);
        irodsFileSystem.close();
        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(irodsNode, irodsAccount);
        int childCount = irodsFileSystemModel.getChildCount(irodsFileSystemModel.getRoot());
        TestCase.assertEquals("child count should be 1", 1, childCount);

        IRODSNode child1 = (IRODSNode) irodsFileSystemModel.getChild(irodsFileSystemModel.getRoot(), 0);
        TestCase.assertNotNull("null child IRODSNode", child1);

        IRODSNode child2 = (IRODSNode) irodsFileSystemModel.getChild(child1, 0);
        TestCase.assertNotNull("child of child not found", child2);

     
    }

    /**
     * Test of isLeaf method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testIsLeafWhenEmptyCollection() throws Exception {
        String subdirPrefix = "testIsLeafWhenCollection";
        int count = 2;

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + subdirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdirs();
        irodsFile.close();

        String myTarget = "";

        for (int i = 0; i < count; i++) {
            myTarget = targetIrodsCollection + "/c" + ( 10000 + i )
                    + subdirPrefix;
            irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
            irodsFile.mkdirs();
            irodsFile.close();
        }

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(targetIrodsCollection, 0);
        CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();
        root.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
        root.setParentPath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH));
        root.setPathOrName(targetIrodsCollection);

        IRODSNode testRootNode = new IRODSNode(root, irodsAccount);
        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(testRootNode, irodsAccount);
        irodsFileSystem.close();

        IRODSNode rootNode = (IRODSNode) irodsFileSystemModel.getRoot();
        IRODSNode firstChild = (IRODSNode) irodsFileSystemModel.getChild(rootNode, 0);

        boolean result = firstChild.isLeaf();
        TestCase.assertTrue("child should be a leaf", result);
        TestCase.assertTrue("this is a collection, so it does allow children", firstChild.getAllowsChildren());

    }

    /**
     * Test of getIndexOfChild method, of class IRODSFileSystemModelOld.
     */
    @Test
    public void testGetIndexOfChild() throws Exception {
        String rootCollection = "testGetIndexOfChild";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH);

        FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath, "testcoll", 3, 4, 2, "file", ".doc", 10, 7, 1, 3);

        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
        IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
        DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(
                irodsAccount);
        File localFile = new File(localCollectionAbsolutePath);

        dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
          CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(irodsCollectionRootAbsolutePath + "/" + rootCollection, 0);
        CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();
        root.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
        root.setParentPath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH));
        root.setPathOrName(root.getParentPath() + '/' + rootCollection);

                IRODSNode rootNode = new IRODSNode(root, irodsAccount);

        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(rootNode, irodsAccount);

        // get the children of the root IRODSNode, which should be the directories I just put into irods
        IRODSNode rootNodeInModel = (IRODSNode) irodsFileSystemModel.getRoot();

        int countOfChildren = irodsFileSystemModel.getChildCount(rootNodeInModel);
        int childrenNodesFound = 0;

        for (int i = 0; i < countOfChildren; i++) {

            IRODSNode childAsNode = (IRODSNode) irodsFileSystemModel.getChild(rootNodeInModel, i);
            childrenNodesFound++;
            //System.out.println("child is:" + childAsNode );
        }

        TestCase.assertEquals("did not find IRODSNode for counted nodes", countOfChildren, childrenNodesFound);

    }

    /**
     * Test of valueForPathChanged method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testValueForPathChanged() {
        System.out.println("valueForPathChanged");
        TreePath path = null;
        Object value = null;
        IRODSFileSystemModel instance = null;
        instance.valueForPathChanged(path, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTreeModelListener method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testAddTreeModelListener() {
        System.out.println("addTreeModelListener");
        TreeModelListener listener = null;
        IRODSFileSystemModel instance = null;
        instance.addTreeModelListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeTreeModelListener method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testRemoveTreeModelListener() {
        System.out.println("removeTreeModelListener");
        TreeModelListener listener = null;
        IRODSFileSystemModel instance = null;
        instance.removeTreeModelListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getChild method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testGetChild() {
        System.out.println("getChild");
        Object parent = null;
        int index = 0;
        IRODSFileSystemModel instance = null;
        Object expResult = null;
        Object result = instance.getChild(parent, index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

     /**
     * Test of getIndexOfChild method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testTreePathForIrodsPath() throws Exception {
        String rootCollection = "testTreePathForIrodsPath";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH);

        FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath, "testcoll", 3, 4, 2, "file", ".doc", 10, 7, 1, 3);

        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
        IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
        DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(
                irodsAccount);
        File localFile = new File(localCollectionAbsolutePath);

        dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
          CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(irodsCollectionRootAbsolutePath + "/" + rootCollection, 0);
        CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();
        root.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
        root.setParentPath(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH));
        root.setPathOrName(root.getParentPath() + '/' + rootCollection);

        IRODSNode testRootNode = new IRODSNode(root, irodsAccount);
        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(testRootNode, irodsAccount);

        // get the children of the root IRODSNode, which should be the directories I just put into irods
        IRODSNode rootNodeInModel = (IRODSNode) irodsFileSystemModel.getRoot();

        int countOfChildren = irodsFileSystemModel.getChildCount(rootNodeInModel);

        for (int i = 0; i < countOfChildren; i++) {

            IRODSNode childAsNode = (IRODSNode) irodsFileSystemModel.getChild(rootNodeInModel, i);
            // build the children nodes if they exist by asking for a child count
            irodsFileSystemModel.getChildCount(childAsNode);
            System.out.println("child is:" + childAsNode );
        }

        JTree irodsFileTree = new JTree();
        irodsFileTree.setModel(irodsFileSystemModel);
        //irodsFileTree.getModel();

        String[] irodsPathElements = irodsCollectionRootAbsolutePath.split("/");

        // get the first child IRODSNode and get its child for a test case

        IRODSNode rootNode = (IRODSNode) irodsFileSystemModel.getRoot();
        IRODSNode childOfRoot = (IRODSNode) rootNode.getChildAt(0);
        IRODSNode childOfChildOfRoot = (IRODSNode) childOfRoot.getChildAt(0);

        TestCase.assertNotNull("there should have been a child IRODSNode planted in test setup", childOfChildOfRoot);

        CollectionAndDataObjectListingEntry  targetChildListingEntry = (CollectionAndDataObjectListingEntry) childOfChildOfRoot.getUserObject();
        String targetChildPath = targetChildListingEntry.getPathOrName();
        System.out.println("targetChildPath:" + targetChildPath);

        // now try and get the IRODSNode from the model based on the string path...
        TreePath treePath = TreeUtils.buildTreePathForIrodsAbsolutePath(irodsFileTree, targetChildPath);
        System.out.println("derived tree path:" +  treePath);

        int foundRow = irodsFileTree.getRowForPath(treePath);
        TestCase.assertFalse("did not get a row for a valid path", foundRow == -1);
    }


     /**
     * Test of getIndexOfChild method, of class IRODSFileSystemModelOld.
     */
    @Ignore
    public void testTreePathForIrodsPathWhenTreeRootIsFileRoot() throws Exception {
        String rootCollection = "testTreePathForIrodsPathWhenTreeRootIsFileRoot";
        String localCollectionAbsolutePath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
                + '/' + rootCollection);

        String irodsCollectionRootAbsolutePath = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH);

        FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath, "testcoll", 3, 4, 2, "file", ".doc", 10, 7, 1, 3);

        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
        IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
        DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(
                irodsAccount);
        File localFile = new File(localCollectionAbsolutePath);

        dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
          CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        List<CollectionAndDataObjectListingEntry> entries = collectionAO.listCollectionsUnderPath(irodsCollectionRootAbsolutePath + "/" + rootCollection, 0);
        CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();
        root.setObjectType(CollectionAndDataObjectListingEntry.ObjectType.COLLECTION);
        root.setParentPath("");
        root.setPathOrName("/");

        IRODSNode testRootNode = new IRODSNode(root,irodsAccount);
        IRODSFileSystemModel irodsFileSystemModel = new IRODSFileSystemModel(testRootNode, irodsAccount);

        // get the children of the root IRODSNode, which should be the directories I just put into irods
        IRODSNode rootNodeInModel = (IRODSNode) irodsFileSystemModel.getRoot();

        int countOfChildren = irodsFileSystemModel.getChildCount(rootNodeInModel);

        for (int i = 0; i < countOfChildren; i++) {
            IRODSNode childAsNode = (IRODSNode) irodsFileSystemModel.getChild(rootNodeInModel, i);
            // build the children nodes if they exist by asking for a child count
            irodsFileSystemModel.getChildCount(childAsNode);
            System.out.println("child is:" + childAsNode );
        }

        JTree irodsFileTree = new JTree();
        irodsFileTree.setModel(irodsFileSystemModel);
        //irodsFileTree.getModel();

        String[] irodsPathElements = irodsCollectionRootAbsolutePath.split("/");

        // get the first child IRODSNode and get its child for a test case

        IRODSNode rootNode = (IRODSNode) irodsFileSystemModel.getRoot();
        IRODSNode childOfRoot = (IRODSNode) rootNode.getChildAt(0);
        IRODSNode childOfChildOfRoot = (IRODSNode) childOfRoot.getChildAt(0);

        TestCase.assertNotNull("there should have been a child IRODSNode planted in test setup", childOfChildOfRoot);

        CollectionAndDataObjectListingEntry  targetChildListingEntry = (CollectionAndDataObjectListingEntry) childOfChildOfRoot.getUserObject();
        String targetChildPath = targetChildListingEntry.getPathOrName();
        System.out.println("targetChildPath:" + targetChildPath);


        // now try and get the IRODSNode from the model based on the string path...
        TreePath treePath = TreeUtils.buildTreePathForIrodsAbsolutePath(irodsFileTree, targetChildPath);
        System.out.println("derived tree path:" +  treePath);

        irodsFileTree.expandPath(treePath);
        int foundRow = irodsFileTree.getRowForPath(treePath);
        TestCase.assertFalse("did not get a row for a valid path", foundRow == -1);
    }

}
