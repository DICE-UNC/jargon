/**
 *
 */
package edu.sdsc.jargon.testutils;

import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.jargon.testutils.icommandinvoke.IrodsInvocationContext;
import static edu.sdsc.jargon.testutils.TestingPropertiesHelper.*;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import java.util.Properties;


/**
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/16/2009
 */
public class TestingPropertiesHelperTest {
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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

    /**
     * Can I load the properties?
     * Test method for {@link org.irods.jargon.test.utils.IrodsTestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetTestProperties() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        TestCase.assertTrue("did not successfuly load test properties",
            props.size() > 0);
    }

    /**
     * Can I look up the data directory?
     * Test method for {@link org.irods.jargon.test.utils.IrodsTestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetDataDirectoryValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.GENERATED_FILE_DIRECTORY_KEY);
        TestCase.assertNotNull("did not look up file directory key", dataDir);
    }

    /**
     * Can I look up the irods user?
     * Test method for {@link org.irods.jargon.test.utils.IrodsTestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetIrodsUserValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_USER_KEY);
        TestCase.assertNotNull("did not look up irods user key", dataDir);
    }

    @Test
    public void testGetIrodsSecondaryUserValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY);
        TestCase.assertNotNull("did not look up irods secondary user key", dataDir);
    }


    /**
     * Can I look up the irods password?
     * Test method for {@link org.TestingPropertiesHelper.jargon.test.utils.TestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetIrodsPasswordValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_PASSWORD_KEY);
        TestCase.assertNotNull("did not look up irods password key", dataDir);
    }

    @Test
    public void testGetIrodsSecondaryPasswordValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_SECONDARY_PASSWORD_KEY);
        TestCase.assertNotNull("did not look up irods secondary password key", dataDir);
    }

    /**
     * Can I look up the irods host?
     * Test method for {@link org.TestingPropertiesHelper.jargon.test.utils.TestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetIrodsHostValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_HOST_KEY);
        TestCase.assertNotNull("did not look up irods host key", dataDir);
    }

    /**
     * Can I look up the irods port?
     * Test method for {@link org.TestingPropertiesHelper.jargon.test.utils.TestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetIrodsPortValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_PORT_KEY);
        TestCase.assertNotNull("did not look up irods port key", dataDir);
    }

    /**
     * Can I look up the irods subdir?
      */
    @Test
    public void testGetIrodsSubdir() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object scratchDir = props.get(TestingPropertiesHelper.IRODS_SCRATCH_DIR_KEY);
        TestCase.assertNotNull("did not look up irods scratch dir", scratchDir);
    }


    /**
     * Can I look up the irods zone?
     * Test method for {@link org.TestingPropertiesHelper.jargon.test.utils.TestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetIrodsZoneValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_ZONE_KEY);
        TestCase.assertNotNull("did not look up irods zone key", dataDir);
    }

    /**
     * Can I look up the irods zone?
     * Test method for {@link org.TestingPropertiesHelper.jargon.test.utils.TestingPropertiesLoader#getTestProperties()}.
     */
    @Test
    public void testGetIrodsResourceValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
        TestCase.assertNotNull("did not look up irods resource key", dataDir);
    }

    @Test
    public void testGetIrodsSecondaryResourceValue() throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = testingProperties.getTestProperties();
        Object dataDir = props.get(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY);
        TestCase.assertNotNull("did not look up irods secondary resource key", dataDir);
    }

    @Test
    public void testBuildUriFromTestPropertiesForFileInUserDir()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_USER_KEY, "user");
        props.put(IRODS_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_SCRATCH_DIR_KEY, "scratch");

        String testFile = "test.txt";
        URI returnUri = testingProperties.buildUriFromTestPropertiesForFileInUserDir(props,
                "test.txt");
        TestCase.assertEquals("did not create valid URI from properties",
            "irods://user.zone:password@host:1234/zone/home/user/scratch/test.txt",
            returnUri.toString());
    }

    @Test
    public void testBuildUriFromTestPropertiesForFileInSecondaryUserDir()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_SECONDARY_USER_KEY, "user");
        props.put(IRODS_SECONDARY_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_SECONDARY_RESOURCE_KEY, "test1-resc2");
        props.put(IRODS_SCRATCH_DIR_KEY, "scratch");

        String testFile = "test.txt";
        URI returnUri = testingProperties.buildUriFromTestPropertiesForFileInSecondaryUserDir(props,
                "test.txt");
        TestCase.assertEquals("did not create valid URI from properties",
            "irods://user.zone:password@host:1234/zone/home/user/scratch/test.txt",
            returnUri.toString());
    }

    @Test
    public void testBuildIRODSAccountFromTestProperties()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_USER_KEY, "user");
        props.put(IRODS_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_RESOURCE_KEY, "resource");

        IRODSAccount account = testingProperties.buildIRODSAccountFromTestProperties(props);
        TestCase.assertEquals("no user in IRODS Account", "user",
            account.getUserName());
        TestCase.assertEquals("no password", "password", account.getPassword());
        TestCase.assertEquals("no zone", "zone", account.getZone());
        TestCase.assertEquals("no host", "host", account.getHost());
        TestCase.assertEquals("no port", 1234, account.getPort());
        TestCase.assertEquals("no resource", "resource",
            account.getDefaultStorageResource());
    }

    @Test
    public void testBuildIRODSAdminAccountFromTestProperties()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_ADMIN_USER_KEY, "admin");
        props.put(IRODS_ADMIN_PASSWORD_KEY, "adminpassword");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_RESOURCE_KEY, "resource");

        IRODSAccount account = testingProperties.buildIRODSAdminAccountFromTestProperties(props);
        TestCase.assertEquals("no user in IRODS Account", "admin",
            account.getUserName());
        TestCase.assertEquals("no password", "adminpassword", account.getPassword());
        TestCase.assertEquals("no zone", "zone", account.getZone());
        TestCase.assertEquals("no host", "host", account.getHost());
        TestCase.assertEquals("no port", 1234, account.getPort());
        TestCase.assertEquals("no resource", "resource",
            account.getDefaultStorageResource());
    }

    @Test
    public void testBuildIRODSAccountFromSecondaryTestProperties()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_SECONDARY_USER_KEY, "user");
        props.put(IRODS_SECONDARY_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_SECONDARY_RESOURCE_KEY, "resource");

        IRODSAccount account = testingProperties.buildIRODSAccountFromSecondaryTestProperties(props);
        TestCase.assertEquals("no user in IRODS Account", "user",
            account.getUserName());
        TestCase.assertEquals("no password", "password", account.getPassword());
        TestCase.assertEquals("no zone", "zone", account.getZone());
        TestCase.assertEquals("no host", "host", account.getHost());
        TestCase.assertEquals("no port", 1234, account.getPort());
        TestCase.assertEquals("no resource", "resource",
            account.getDefaultStorageResource());
    }

    @Test
    public void testBuildIRODSInvocationContextFromSecondaryTestProperties()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_SECONDARY_USER_KEY, "user");
        props.put(IRODS_SECONDARY_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_SECONDARY_RESOURCE_KEY, "resource");
        props.put(IRODS_SCRATCH_DIR_KEY, "scratch");

        IrodsInvocationContext context = testingProperties.buildIRODSInvocationContextFromSecondaryTestProperties(props);
        TestCase.assertEquals("no user in IRODS Account", "user",
            context.getIrodsUser());
        TestCase.assertEquals("no password", "password", context.getIrodsPassword());
        TestCase.assertEquals("no zone", "zone", context.getIrodsZone());
        TestCase.assertEquals("no host", "host", context.getIrodsHost());
        TestCase.assertEquals("no port", 1234, context.getIrodsPort());
        TestCase.assertEquals("no resource", "resource", context.getIrodsResource());
        TestCase.assertEquals("no irods scratch dir", "scratch", context.getIrodsScratchDir());

    }

    @Test
    public void testBuildIRODSInvocationContextFromTestProperties()
        throws Exception {
        TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_USER_KEY, "user");
        props.put(IRODS_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_RESOURCE_KEY, "resource");
        props.put(IRODS_SCRATCH_DIR_KEY, "scratch");

        IrodsInvocationContext context = testingProperties.buildIRODSInvocationContextFromTestProperties(props);
        TestCase.assertEquals("no user in IRODS Account", "user",
            context.getIrodsUser());
        TestCase.assertEquals("no password", "password", context.getIrodsPassword());
        TestCase.assertEquals("no zone", "zone", context.getIrodsZone());
        TestCase.assertEquals("no host", "host", context.getIrodsHost());
        TestCase.assertEquals("no port", 1234, context.getIrodsPort());
        TestCase.assertEquals("no resource", "resource", context.getIrodsResource());
        TestCase.assertEquals("no irods scratch dir", "scratch", context.getIrodsScratchDir());

    }


    @Test
    public void testBuildIRODSCollectionPathFromTestProperties() throws Exception {
    	TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_USER_KEY, "user");
        props.put(IRODS_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_RESOURCE_KEY, "resource");
        props.put(IRODS_SCRATCH_DIR_KEY, "test-scratch");

        String testDescendingCollection = "hithere";
        String actualCollectionPath = testingProperties.buildIRODSCollectionAbsolutePathFromTestProperties(props, testDescendingCollection);
    	String expectedCollectionPath = "/zone/home/user/test-scratch/hithere";
    	TestCase.assertEquals("did not correctly formulate irods path", expectedCollectionPath, actualCollectionPath);
    }

    @Test
    public void testBuildIRODSCollectionPathFromSecondaryTestProperties() throws Exception {
    	TestingPropertiesHelper testingProperties = new TestingPropertiesHelper();
        Properties props = new Properties();
        props.put(IRODS_SECONDARY_USER_KEY, "user");
        props.put(IRODS_SECONDARY_PASSWORD_KEY, "password");
        props.put(IRODS_HOST_KEY, "host");
        props.put(IRODS_PORT_KEY, "1234");
        props.put(IRODS_ZONE_KEY, "zone");
        props.put(IRODS_SECONDARY_RESOURCE_KEY, "resource");
        props.put(IRODS_SCRATCH_DIR_KEY, "test-scratch");

        String testDescendingCollection = "hithere";
        String actualCollectionPath = testingProperties.buildIRODSCollectionAbsolutePathFromSecondaryTestProperties(props, testDescendingCollection);
    	String expectedCollectionPath = "/zone/home/user/test-scratch/hithere";
    	TestCase.assertEquals("did not correctly formulate irods path", expectedCollectionPath, actualCollectionPath);
    }
}
