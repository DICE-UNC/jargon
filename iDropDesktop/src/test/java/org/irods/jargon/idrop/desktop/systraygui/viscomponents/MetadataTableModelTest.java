/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.lang.Class;
import java.util.ArrayList;
import java.util.List;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
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
public class MetadataTableModelTest {

    private static List<MetaDataAndDomainData> metadataAndDomainData = new ArrayList<MetaDataAndDomainData>();

    public MetadataTableModelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        MetaDataAndDomainData metadata = MetaDataAndDomainData.instance(MetadataDomain.DATA, "1", "abspath", "attribute", "value", "units");
       metadataAndDomainData.add(metadata);
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
     * Test of getColumnClass method, of class MetadataTableModel.
     */
    @Test
    public void testGetColumnClass() {
        System.out.println("getColumnClass");
        int columnIndex = 0;

        MetadataTableModel instance = new MetadataTableModel(metadataAndDomainData);
        Class expResult = String.class;
        Class result = instance.getColumnClass(columnIndex);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getColumnName method, of class MetadataTableModel.
     */
    @Test
    public void testGetColumnName() {
        System.out.println("getColumnName");
        int columnIndex = 0;

        MetadataTableModel instance = new MetadataTableModel(metadataAndDomainData);
        String expResult = "ID";
        String result = instance.getColumnName(columnIndex);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getRowCount method, of class MetadataTableModel.
     */
    @Test
    public void testGetRowCount() {
        System.out.println("getRowCount");
        MetadataTableModel instance = new MetadataTableModel(metadataAndDomainData);
        int expResult = 1;
        int result = instance.getRowCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnCount method, of class MetadataTableModel.
     */
    @Test
    public void testGetColumnCount() {
        System.out.println("getColumnCount");
        MetadataTableModel instance = new MetadataTableModel(metadataAndDomainData);
        int expResult = 5;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getValueAt method, of class MetadataTableModel.
     */
    @Test
    public void testGetValueAt() {
        System.out.println("getValueAt");
        int rowIndex = 0;
        int columnIndex = 0;
        MetadataTableModel instance = new MetadataTableModel(metadataAndDomainData);
        Object expResult = "1";
        Object result = instance.getValueAt(rowIndex, columnIndex);
        assertEquals(expResult, result);
      
    }

}