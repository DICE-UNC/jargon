package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.irods.jargon.transferengine.domain.LocalIRODSTransferItem;

/**
 * Model for a table viewing queue detail data
 * @author Mike Conway - DICE (www.irods.org)
 */
public class QueueManagerDetailTableModel extends AbstractTableModel {

        private final List<LocalIRODSTransferItem>  localIRODSTransferItems;


    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }


        // translate indexes to object values

        // 0 = isError

        if (columnIndex == 0) {
            return Boolean.class;
        }

        // 1 = message

        if (columnIndex == 1) {
            return String.class;
        }

        // 2 = isFile

        if (columnIndex == 2) {
            return Boolean.class;
        }

       // 3 = source

        if (columnIndex == 3) {
            return String.class;
        }

        // 4 = target

        if (columnIndex == 4) {
            return String.class;
        }


        // 5 = date

        if (columnIndex == 5) {
            return Date.class;
        }

   
        throw new IdropRuntimeException("unknown column");
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

    // 0 = isError

        if (columnIndex == 0) {
            return "Error";
        }

        // 1 = message

        if (columnIndex == 1) {
            return "Message";
        }

        // 2 = isFile

        if (columnIndex == 2) {
            return "File";
        }

       // 3 = source

        if (columnIndex == 3) {
            return "Source";
        }

        // 4 = target

        if (columnIndex == 4) {
            return "Destination";
        }


        // 5 = date

        if (columnIndex == 5) {
            return "Transfer Date";
        }


        throw new IdropRuntimeException("unknown column");
    }

    public QueueManagerDetailTableModel(final List<LocalIRODSTransferItem> localIRODSTransferItems) {
        if (localIRODSTransferItems == null) {
            throw new IdropRuntimeException("null localIRODSTransfer");
        }

        this.localIRODSTransferItems = localIRODSTransferItems;

    }

    @Override
    public int getRowCount() {
        return localIRODSTransferItems.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (rowIndex >= getRowCount()) {
            throw new IdropRuntimeException("row unavailable, out of bounds");
        }

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        LocalIRODSTransferItem localIRODSTransferItem = localIRODSTransferItems.get(rowIndex);

        // translate indexes to object values

       // 0 = isError

        if (columnIndex == 0) {
            return localIRODSTransferItem.isError();
        }

        // 1 = message

        if (columnIndex == 1) {
            return localIRODSTransferItem.getErrorMessage();
        }

        // 2 = isFile

        if (columnIndex == 2) {
            return localIRODSTransferItem.isFile();
        }

       // 3 = source

        if (columnIndex == 3) {
            return localIRODSTransferItem.getSourceFileAbsolutePath();
        }

        // 4 = target

        if (columnIndex == 4) {
            return localIRODSTransferItem.getTargetFileAbsolutePath();
        }


        // 5 = date

        if (columnIndex == 5) {
            return localIRODSTransferItem.getTransferredAt();
        }

        throw new IdropRuntimeException("unknown column");

    }
}
