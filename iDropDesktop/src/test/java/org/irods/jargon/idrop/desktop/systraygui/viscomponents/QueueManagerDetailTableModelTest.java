/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.ArrayList;
import java.util.Date;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;
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
public class QueueManagerDetailTableModelTest {

    private static LocalIRODSTransfer localIRODSTransfer = null;
    private static LocalIRODSTransferItem localIRODSTransferItem = null;
    private static QueueManagerDetailTableModel detailTableModel = null;

    public QueueManagerDetailTableModelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        localIRODSTransfer = new LocalIRODSTransfer();
        localIRODSTransfer.setCreatedAt(new Date());
        localIRODSTransfer.setGlobalException("");
        localIRODSTransfer.setId(new Long(1));
        localIRODSTransfer.setIrodsAbsolutePath("/hello/there");
        localIRODSTransfer.setTransferEnd(new Date());
        localIRODSTransfer.setTransferErrorStatus("OK");
        localIRODSTransfer.setTransferHost("host");
        localIRODSTransfer.setTransferPort(1247);

        LocalIRODSTransferItem transferItem = new LocalIRODSTransferItem();
        transferItem.setError(false);
        transferItem.setFile(true);
        transferItem.setLocalIRODSTransfer(localIRODSTransfer);
        transferItem.setSourceFileAbsolutePath("source");
        transferItem.setTargetFileAbsolutePath("target");
        transferItem.setTransferredAt(new Date());

        localIRODSTransfer.getLocalIRODSTransferItems().add(transferItem);
        localIRODSTransferItem = transferItem;

        detailTableModel = new QueueManagerDetailTableModel(new ArrayList(localIRODSTransfer.getLocalIRODSTransferItems()));
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
     * Test of getColumnClass method, of class QueueManagerDetailTableModel.
     */
    @Test
    public void testGetColumnClass() {
        System.out.println("getColumnClass");
        int columnIndex = 0;
        Class expResult = Boolean.class;
        Class result = detailTableModel.getColumnClass(columnIndex);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnName method, of class QueueManagerDetailTableModel.
     */
    @Test
    public void testGetColumnName() {
        System.out.println("getColumnName");
        int columnIndex = 4;
        String expResult = "Destination";
        String result = detailTableModel.getColumnName(columnIndex);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRowCount method, of class QueueManagerDetailTableModel.
     */
    @Test
    public void testGetRowCount() {
        System.out.println("getRowCount");
        int expResult = 1;
        int result = detailTableModel.getRowCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnCount method, of class QueueManagerDetailTableModel.
     */
    @Test
    public void testGetColumnCount() {
        System.out.println("getColumnCount");
        int expResult = 6;
        int result = detailTableModel.getColumnCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValueAt method, of class QueueManagerDetailTableModel.
     */
    @Test
    public void testGetValueAt() {
        System.out.println("getValueAt");
        int rowIndex = 0;
        int columnIndex = 3;
        Object expResult = localIRODSTransferItem.getSourceFileAbsolutePath();
        Object result = detailTableModel.getValueAt(rowIndex, columnIndex);
        assertEquals(expResult, result);
    }

}