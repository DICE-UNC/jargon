package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.lang.Class;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
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
public class QueueManagerMasterTableModelTest {

    public QueueManagerMasterTableModelTest() {
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
     * Test of getColumnClass method, of class QueueManagerMasterTableModel.
     */
    @Test
    public void testGetColumnClass() {
        System.out.println("getColumnClass");
        int columnIndex = 0;

        LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
        List<LocalIRODSTransfer> localIRODSTransfers = new ArrayList<LocalIRODSTransfer>();


        QueueManagerMasterTableModel instance = new QueueManagerMasterTableModel(localIRODSTransfers);
        Class expResult = Date.class;
        Class result = instance.getColumnClass(columnIndex);
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getColumnName method, of class QueueManagerMasterTableModel.
     */
    @Test
    public void testGetColumnName() {
        System.out.println("getColumnName");
        int columnIndex = 0;
        LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
        List<LocalIRODSTransfer> localIRODSTransfers = new ArrayList<LocalIRODSTransfer>();
        QueueManagerMasterTableModel instance = new QueueManagerMasterTableModel(localIRODSTransfers);
        String expResult = "Start Date";
        String result = instance.getColumnName(columnIndex);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRowCount method, of class QueueManagerMasterTableModel.
     */
    @Test
    public void testGetRowCount() {
        System.out.println("getRowCount");
        LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
        List<LocalIRODSTransfer> localIRODSTransfers = new ArrayList<LocalIRODSTransfer>();
        QueueManagerMasterTableModel instance = new QueueManagerMasterTableModel(localIRODSTransfers);
        localIRODSTransfers.add(localIRODSTransfer);
        int expResult = 1;
        int result = instance.getRowCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnCount method, of class QueueManagerMasterTableModel.
     */
    @Test
    public void testGetColumnCount() {
        System.out.println("getColumnCount");
       LocalIRODSTransfer localIRODSTransfer = new LocalIRODSTransfer();
        List<LocalIRODSTransfer> localIRODSTransfers = new ArrayList<LocalIRODSTransfer>();
        QueueManagerMasterTableModel instance = new QueueManagerMasterTableModel(localIRODSTransfers);
        int expResult = 6;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
      
    }
}