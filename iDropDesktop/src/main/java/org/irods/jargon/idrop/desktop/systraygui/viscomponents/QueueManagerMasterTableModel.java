package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.slf4j.LoggerFactory;

/** * Model for a table viewing queue master data
 * @author Mike Conway - DICE (www.irods.org)
 */
public class QueueManagerMasterTableModel extends DefaultTableModel {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(QueueManagerMasterTableModel.class);

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }


        // translate indexes to object values

        // 0 = start date

        if (columnIndex == 0) {
            return Date.class;
        }

        // 1 = status

        if (columnIndex == 1) {
            return String.class;
        }

        // 2 = state

        if (columnIndex == 2) {
            return String.class;
        }

        // 3 = global error

        if (columnIndex == 3) {
            return String.class;
        }


        // source

        if (columnIndex == 4) {
            return String.class;
        }

        // 5 = target path

        if (columnIndex == 5) {
            return String.class;
        }



        throw new IdropRuntimeException("unknown column");
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }


        // translate indexes to object values

        // 0 = start date

        if (columnIndex == 0) {
            return "Start Date";
        }

        // 1 = status

        if (columnIndex == 1) {
            return "Status";
        }

        // 2 = state

        if (columnIndex == 2) {
            return "Error Status";
        }

        // 3 = Type

        if (columnIndex == 3) {
            return "Operation";
        }

        // 5 = local path

        if (columnIndex == 4) {
            return "Source";
        }

        // 6 = target path

        if (columnIndex == 5) {
            return "Destination";
        }


        throw new IdropRuntimeException("unknown column");
    }
    private List<LocalIRODSTransfer> localIRODSTransfers = null;

    public QueueManagerMasterTableModel(final List<LocalIRODSTransfer> localIRODSTransfers) {
        if (localIRODSTransfers == null) {
            throw new IdropRuntimeException("null localIRODSTransfers");
        }

        this.localIRODSTransfers = localIRODSTransfers;
    }

    @Override
    public synchronized int getRowCount() {
        if (localIRODSTransfers == null) {
            return 0;
        } else {
            return localIRODSTransfers.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {

        if (rowIndex >= getRowCount()) {
            throw new IdropRuntimeException("row unavailable, out of bounds");
        }

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        LocalIRODSTransfer localIRODSTransfer = localIRODSTransfers.get(rowIndex);

        // translate indexes to object values

        // 0 = start date

        if (columnIndex == 0) {
            return localIRODSTransfer.getTransferStart();
        }

        // 1 = status

        if (columnIndex == 1) {
            return localIRODSTransfer.getTransferState();
        }

        // 2 = state

        if (columnIndex == 2) {
            return localIRODSTransfer.getTransferErrorStatus();
        }

        // 3 =operation

        if (columnIndex == 3) {
            return localIRODSTransfer.getTransferType();
        }

        // 4 = source path

        if (columnIndex == 4) {
            if (localIRODSTransfer.getTransferType().equals(LocalIRODSTransfer.TRANSFER_TYPE_GET)) {
                return localIRODSTransfer.getIrodsAbsolutePath();

            } else if (localIRODSTransfer.getTransferType().equals(LocalIRODSTransfer.TRANSFER_TYPE_PUT)) {
                return localIRODSTransfer.getLocalAbsolutePath();

            } else if (localIRODSTransfer.getTransferType().equals(LocalIRODSTransfer.TRANSFER_TYPE_REPLICATE)) {
                return localIRODSTransfer.getIrodsAbsolutePath();
            } else {
                log.error("unable to build details for transfer with transfer type of:{}", localIRODSTransfer.getTransferType());
                return "";
            }
        }

        // 5 = target path

        if (columnIndex == 5) {
             if (localIRODSTransfer.getTransferType().equals(LocalIRODSTransfer.TRANSFER_TYPE_GET)) {
                return localIRODSTransfer.getLocalAbsolutePath();
            } else if (localIRODSTransfer.getTransferType().equals(LocalIRODSTransfer.TRANSFER_TYPE_PUT)) {
                return localIRODSTransfer.getIrodsAbsolutePath();
            } else if (localIRODSTransfer.getTransferType().equals(LocalIRODSTransfer.TRANSFER_TYPE_REPLICATE)) {
                return "";
            } else {
                log.error("unable to build details for transfer with transfer type of:{}", localIRODSTransfer.getTransferType());
                return "";
            }
        }

        throw new IdropRuntimeException("unknown column");

    }

    public synchronized LocalIRODSTransfer getTransferAtRow(final int rowIndex) {
        if (localIRODSTransfers == null) {
            log.warn("attempt to access a null model");
            return null;
        }

        if (rowIndex >= localIRODSTransfers.size()) {
            log.warn("attempt to access a row that does not exist");
            return null;
        }

        return localIRODSTransfers.get(rowIndex);
    }
}
