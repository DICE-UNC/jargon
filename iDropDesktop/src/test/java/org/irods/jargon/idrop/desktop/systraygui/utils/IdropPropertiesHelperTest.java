
package org.irods.jargon.idrop.desktop.systraygui.utils;

import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikeconway
 */
public class IdropPropertiesHelperTest {

    public IdropPropertiesHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    /**
     * Test of loadIdropProperties method, of class IdropPropertiesHelper.
     */
    @Test
    public void testLoadIdropProperties() throws Exception {
        IdropPropertiesHelper instance = new IdropPropertiesHelper();
 
        Properties result = instance.loadIdropProperties();
        assertNotNull("idrop.properties not loaded", result);
   
    }

}