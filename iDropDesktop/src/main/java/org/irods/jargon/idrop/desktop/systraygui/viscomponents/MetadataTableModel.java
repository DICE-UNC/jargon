package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.irods.jargon.transferengine.domain.LocalIRODSTransfer;
import org.slf4j.LoggerFactory;

/**
 * Model for a table viewing metadata
 * @author Mike Conway - DICE (www.irods.org)
 */
public class MetadataTableModel extends AbstractTableModel {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(MetadataTableModel.class);

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        // translate indexes to object values
        // 0 = collectionId

        if (columnIndex == 0) {
            return String.class;
        }

        // 1 = collection abs path

        if (columnIndex == 1) {
            return String.class;
        }

        // 2 = attribute

        if (columnIndex == 2) {
            return String.class;
        }

        // 3 = value

        if (columnIndex == 3) {
            return String.class;
        }

        // 4 = units

        if (columnIndex == 4) {
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

        // 0 = id

        if (columnIndex == 0) {
            return "ID";
        }

        // 1 = abs path

        if (columnIndex == 1) {
            return "Absolute Path";
        }

        // 2 = attribute

        if (columnIndex == 2) {
            return "Attribute";
        }

        // 3 = value

        if (columnIndex == 3) {
            return "Value";
        }

        // 4 = units

        if (columnIndex == 4) {
            return "Units";
        }

        throw new IdropRuntimeException("unknown column");
    }
    private List<MetaDataAndDomainData> metadataAndDomainData = null;

    public MetadataTableModel(final List<MetaDataAndDomainData> metadataAndDomainData) {
        if (metadataAndDomainData == null) {
            throw new IdropRuntimeException("null metadataAndDomainData");
        }

        this.metadataAndDomainData = metadataAndDomainData;
    }

    @Override
    public int getRowCount() {
        return metadataAndDomainData.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (rowIndex >= getRowCount()) {
            throw new IdropRuntimeException("row unavailable, out of bounds");
        }

        if (columnIndex >= getColumnCount()) {
            throw new IdropRuntimeException("column unavailable, out of bounds");
        }

        MetaDataAndDomainData metadataEntry = metadataAndDomainData.get(rowIndex);

        // translate indexes to object values

        // 0 = id

        if (columnIndex == 0) {
            return metadataEntry.getDomainObjectId();
        }

        // 1 = abs path

        if (columnIndex == 1) {
            return metadataEntry.getDomainObjectUniqueName();
        }

        // 2 = attribute

        if (columnIndex == 2) {
            return metadataEntry.getAvuAttribute();
        }

        // 3 = value

        if (columnIndex == 3) {
            return metadataEntry.getAvuValue();
        }

        // 4 = units

        if (columnIndex == 4) {
            return metadataEntry.getAvuUnit();
        }


        throw new IdropRuntimeException("unknown column");

    }
}
