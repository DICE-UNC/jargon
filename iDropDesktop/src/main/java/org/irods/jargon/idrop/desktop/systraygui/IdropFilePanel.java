package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.CollectionViewHolder;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IdropFilePanel extends IdropDefaultCenterPanel {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IdropFilePanel.class);
    private final String fileParentCollectionAbsolutePath;
    private final String fileName;

    public IdropFilePanel(final iDrop iDropParentForm, final CollectionViewHolder collectionViewHolder, final Color backgroundColor) throws IdropException {

        super(iDropParentForm, collectionViewHolder, backgroundColor);
        this.fileName = collectionViewHolder.getCollection().getPathOrName();
        this.fileParentCollectionAbsolutePath = collectionViewHolder.getCollection().getParentPath();
        buildPathLabel();
    }

    @Override
    protected void setUpFolderIcon(Color backgroundColor) {
        getPnlCollectionIcon().setBackground(backgroundColor);
        getPnlCollectionIcon().add(IconHelper.getFileIcon());
    }

    @Override
    protected void setUpReplicationDialog() {
        ReplicationDialog replicationDialog = new ReplicationDialog(getiDropParentForm(), true,
                this.fileParentCollectionAbsolutePath, this.fileName);
        Point panelLocation = this.getLocation();
        replicationDialog.setLocation(new Point((int) panelLocation.getX() + 30, (int) panelLocation.getY()));
        replicationDialog.setVisible(true);
    }

    @Override
     protected void processClickOnFolderIcon(java.awt.event.MouseEvent evt) {
        log.info("mouse action on file icon, not presently used");
    }

    @Override
    protected void displayMetadataViewDialog(MouseEvent evt) {
        MetadataViewDialog metadataViewDialog = new MetadataViewDialog(this.getiDropParentForm(), this.getiDropParentForm().getIrodsAccount(),
               this.fileParentCollectionAbsolutePath, this.fileName);
        metadataViewDialog.setLocation(evt.getX(), evt.getY());
        metadataViewDialog.setVisible(true);
    }

    @Override
     protected void buildPathLabel() {
        getLblSeriesAbsolutePath().setText(fileName);
    }

}
