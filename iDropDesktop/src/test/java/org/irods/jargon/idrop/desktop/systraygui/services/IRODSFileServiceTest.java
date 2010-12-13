/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.services;

import org.junit.Ignore;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import junit.framework.TestCase;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

/**
 *
 * @author mikeconway
 */
public class IRODSFileServiceTest {

    private static Properties testingProperties = new Properties();
    private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
    private static ScratchFileUtils scratchFileUtils = null;
    public static final String IRODS_TEST_SUBDIR_PATH = "IRODSFileServiceTest";
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
     * Test of getCollectionsUnderParentCollection method, of class IRODSFileService.
     */
    @Ignore
    public void testGetCollectionsUnderParentCollection() throws Exception {
        System.out.println("getCollectionsUnderParentCollection");
        String parentCollectionAbsolutePath = "";
        IRODSFileService instance = null;
        List expResult = null;
        List result = instance.getCollectionsUnderParentCollection(parentCollectionAbsolutePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStringFromSourcePaths method, of class IRODSFileService.
     */
    @Ignore
    public void testGetStringFromSourcePaths() throws Exception {
        System.out.println("getStringFromSourcePaths");
        List<String> sourcePaths = null;
        IRODSFileService instance = null;
        String expResult = "";
        String result = instance.getStringFromSourcePaths(sourcePaths);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of runIRODSRule method, of class IRODSFileService.
     */
    @Ignore
    public void testRunIRODSRule() throws Exception {
        System.out.println("runIRODSRule");
        String irodsRule = "";
        IRODSFileService instance = null;
        IRODSRuleExecResult expResult = null;
        IRODSRuleExecResult result = instance.runIRODSRule(irodsRule);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getResources method, of class IRODSFileService.
     */
    @Ignore
    public void testGetResources() throws Exception {
        System.out.println("getResources");
        IRODSFileService instance = null;
        List expResult = null;
        List result = instance.getResources();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVirusStatusForParentCollection method, of class IRODSFileService.
     */
    @Test
    public void testGetVirusStatusForParentCollection() throws Exception {
        System.out.println("getVirusStatusForParentCollection");
        String virusAvuAttrib = "PolicyDrivenService:PolicyProcessingResultAttribute:VirusScan";

        String subdirPrefix = "testGetVirusStatusForParentCollection";
        int count = 2;

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + subdirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdirs();
        irodsFile.close();

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        AvuData virusScanError = AvuData.instance(virusAvuAttrib, "fail", "");

        String myTarget = "";

        for (int i = 0; i < count; i++) {
            myTarget = targetIrodsCollection + "/c" + (10000 + i)
                    + subdirPrefix;
            irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
            irodsFile.mkdirs();
            collectionAO.addAVUMetadata(irodsFile.getAbsolutePath(), virusScanError);
            irodsFile.close();
        }

        List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

        queryElements.add(AVUQueryElement.instanceForValueQuery(
                AVUQueryElement.AVUQueryPart.ATTRIBUTE,
                AVUQueryOperatorEnum.EQUAL, virusAvuAttrib));

        StringBuilder sb = new StringBuilder();
        sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
        sb.append(" LIKE ");
        sb.append("'");
        sb.append(targetIrodsCollection);
        sb.append("%");
        sb.append("'");

        List<MetaDataAndDomainData> metadata = collectionAO.findMetadataValuesByMetadataQueryWithAdditionalWhere(queryElements, sb.toString());
        irodsFileSystem.close();

        TestCase.assertEquals("wrong number of entries returned", 2, metadata.size());


    }

    /**
     * Test of getVirusStatusForParentCollection method, of class IRODSFileService.
     */
    @Test
    public void testGetFixityForParentCollection() throws Exception {
        System.out.println("testGetFixityForParentCollection");
        String fixityAvuAttrib = "PolicyDrivenService:PolicyProcessingResultAttribute:FixityCheck";

        String subdirPrefix = "testGetFixityForParentCollection";
        int count = 2;

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + subdirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdirs();
        irodsFile.close();

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        AvuData virusScanError = AvuData.instance(fixityAvuAttrib, "fail", "");

        String myTarget = "";

        for (int i = 0; i < count; i++) {
            myTarget = targetIrodsCollection + "/c" + (10000 + i)
                    + subdirPrefix;
            irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
            irodsFile.mkdirs();
            collectionAO.addAVUMetadata(irodsFile.getAbsolutePath(), virusScanError);
            irodsFile.close();
        }

        List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();

        queryElements.add(AVUQueryElement.instanceForValueQuery(
                AVUQueryElement.AVUQueryPart.ATTRIBUTE,
                AVUQueryOperatorEnum.EQUAL, fixityAvuAttrib));

        StringBuilder sb = new StringBuilder();
        sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
        sb.append(" LIKE ");
        sb.append("'");
        sb.append(targetIrodsCollection);
        sb.append("%");
        sb.append("'");

        List<MetaDataAndDomainData> metadata = collectionAO.findMetadataValuesByMetadataQueryWithAdditionalWhere(queryElements, sb.toString());
        irodsFileSystem.close();

        TestCase.assertEquals("wrong number of entries returned", 2, metadata.size());


    }

      @Test
    public void testGetMetadataForCollection() throws Exception {
        System.out.println("testGetMetadataForCollection");
        String virusAvuAttrib = "PolicyDrivenService:PolicyProcessingResultAttribute:VirusScan";

        String subdirPrefix = "testGetVirusStatusForParentCollection";
        int count = 2;

        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
        IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

        String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
                testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
                + subdirPrefix);
        IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
        irodsFile.mkdirs();
        irodsFile.close();

        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
        AvuData virusScanError = AvuData.instance(virusAvuAttrib, "fail", "");

        String myTarget = "";

        for (int i = 0; i < count; i++) {
            myTarget = targetIrodsCollection + "/c" + (10000 + i)
                    + subdirPrefix;
            irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
            irodsFile.mkdirs();
            collectionAO.addAVUMetadata(irodsFile.getAbsolutePath(), virusScanError);
            irodsFile.close();
        }

        irodsFileSystem.close();

        iDrop idrop = Mockito.mock(iDrop.class);

        IRODSFileService irodsFileService = new IRODSFileService(irodsAccount, irodsFileSystem);
        List<MetaDataAndDomainData> metadataAndDomainData = irodsFileService.getMetadataForCollection(irodsFile.getAbsolutePath());

        TestCase.assertEquals("wrong number of entries returned", 1, metadataAndDomainData.size());


    }
}
