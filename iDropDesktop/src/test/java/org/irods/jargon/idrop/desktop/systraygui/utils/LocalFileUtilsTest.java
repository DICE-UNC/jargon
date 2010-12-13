/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.idrop.desktop.systraygui.utils;

import java.util.List;
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
public class LocalFileUtilsTest {

    public LocalFileUtilsTest() {
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
     * Test of listFileRootsForSystem method, of class LocalFileUtils.
     */
    @Test
    public void testListFileRootsForSystem() {
        System.out.println("listFileRootsForSystem");
        List expResult = null;
        List result = LocalFileUtils.listFileRootsForSystem();
        assertNotNull(result);
       
    }

}