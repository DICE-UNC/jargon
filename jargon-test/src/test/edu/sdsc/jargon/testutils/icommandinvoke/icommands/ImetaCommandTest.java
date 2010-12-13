package edu.sdsc.jargon.testutils.icommandinvoke.icommands;


import java.util.Properties;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.sdsc.jargon.testutils.IRODSTestSetupUtilities;
import edu.sdsc.jargon.testutils.TestingPropertiesHelper;
import edu.sdsc.jargon.testutils.filemanip.FileGenerator;
import edu.sdsc.jargon.testutils.filemanip.ScratchFileUtils;
import edu.sdsc.jargon.testutils.icommandinvoke.IcommandInvoker;
import edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import edu.sdsc.jargon.testutils.icommandinvoke.icommands.ImetaCommand.MetaObjectType;

public class ImetaCommandTest {
	 private static Properties testingProperties = new Properties();
	    private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	    public static final String IRODS_TEST_SUBDIR_PATH = "IMetaAddCommandTest";
	    private static ScratchFileUtils scratchFileUtils = null;
	    private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	    
	    
	    /**
	     * @throws java.lang.Exception
	     */
	    @BeforeClass
	    public static void setUpBeforeClass() throws Exception {
	        testingProperties = testingPropertiesHelper.getTestProperties();
	        scratchFileUtils = new ScratchFileUtils(testingProperties);
	        irodsTestSetupUtilities = new IRODSTestSetupUtilities();
	        irodsTestSetupUtilities.initializeIrodsScratchDirectory();
	        irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	    }


	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAddAVUToDataObject() throws Exception {
		 String testFileName = "testAddAvu.txt";
		 String expectedAttribName = "testattrib1";
		 String expectedAttribValue = "testvalue1";
	        IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);

	        // generate testing file
	        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
	        String absPathToFile = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
	            20);

	        IputCommand iputCommand = new IputCommand();

	        iputCommand.setLocalFileName(absPathToFile);
	        iputCommand.setIrodsFileName(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH ));
	        
	        iputCommand.setForceOverride(true);

	        IcommandInvoker invoker = new IcommandInvoker(invocationContext);
	        invoker.invokeCommandAndGetResultAsString(iputCommand);
	        
	        // file is put, add an AVU
	        ImetaAddCommand imetaAdd = new ImetaAddCommand();
	        imetaAdd.setAttribName(expectedAttribName);
	        imetaAdd.setAttribValue(expectedAttribValue);
	        imetaAdd.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
	        imetaAdd.setObjectPath(iputCommand.getIrodsFileName() + '/' + testFileName);
	        invoker.invokeCommandAndGetResultAsString(imetaAdd);
	        
	        // now get back the avu data and make sure it's there
	        ImetaListCommand imetaList = new ImetaListCommand();
	        imetaList.setAttribName(expectedAttribName);
	        imetaList.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
	        imetaList.setObjectPath(imetaAdd.getObjectPath());
	        String metaValues = invoker.invokeCommandAndGetResultAsString(imetaList);
	        TestCase.assertTrue("did not find expected attrib name", metaValues.indexOf(expectedAttribName) > -1);
	        TestCase.assertTrue("did not find expected attrib value", metaValues.indexOf(expectedAttribValue) > -1);
	        
	}
	
	@Test
	public void testAddAVUThenDeleteAVU() throws Exception {
		String testFileName = "testAddRemoveAvu.txt";
		 String expectedAttribName = "testattrib1";
		 String expectedAttribValue = "testvalue1";
	        IrodsInvocationContext invocationContext = testingPropertiesHelper.buildIRODSInvocationContextFromTestProperties(testingProperties);

	        // generate testing file
	        String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
	        String absPathToFile = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
	            20);

	        IputCommand iputCommand = new IputCommand();

	        iputCommand.setLocalFileName(absPathToFile);
	        iputCommand.setIrodsFileName(testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH ));
	        
	        iputCommand.setForceOverride(true);

	        IcommandInvoker invoker = new IcommandInvoker(invocationContext);
	        invoker.invokeCommandAndGetResultAsString(iputCommand);
	        
	        // file is put, add an AVU
	        ImetaAddCommand imetaAdd = new ImetaAddCommand();
	        imetaAdd.setAttribName(expectedAttribName);
	        imetaAdd.setAttribValue(expectedAttribValue);
	        imetaAdd.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
	        imetaAdd.setObjectPath(iputCommand.getIrodsFileName() + '/' + testFileName);
	        invoker.invokeCommandAndGetResultAsString(imetaAdd);
	        
	        // now get back the avu data and make sure it's there
	        ImetaListCommand imetaList = new ImetaListCommand();
	        imetaList.setAttribName(expectedAttribName);
	        imetaList.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
	        imetaList.setObjectPath(imetaAdd.getObjectPath());
	        String metaValues = invoker.invokeCommandAndGetResultAsString(imetaList);
	        TestCase.assertTrue("did not find expected attrib name", metaValues.indexOf(expectedAttribName) > -1);
	        TestCase.assertTrue("did not find expected attrib value", metaValues.indexOf(expectedAttribValue) > -1);

	        // now delete
	        ImetaRemoveCommand imetaRemove = new ImetaRemoveCommand();
	        imetaRemove.setAttribName(expectedAttribName);
	        imetaRemove.setAttribValue(expectedAttribValue);
	        imetaRemove.setObjectPath(imetaAdd.getObjectPath());
	        imetaRemove.setMetaObjectType(MetaObjectType.DATA_OBJECT_META);
	        invoker.invokeCommandAndGetResultAsString(imetaRemove);
	        
	        // look up AVU, should be gone
	       
	        metaValues = invoker.invokeCommandAndGetResultAsString(imetaList);
	        TestCase.assertFalse("found removed attrib name", metaValues.indexOf(expectedAttribName) > -1);		
	}
	


}
