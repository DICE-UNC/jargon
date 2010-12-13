/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Color;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.CollectionViewHolder;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * Panel for the parent collection 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IdropParentCollectionPanel extends IdropDefaultCenterPanel {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IdropParentCollectionPanel.class);
    private static final Color BACKGROUND_COLOR = new Color(210, 245, 227);

    public IdropParentCollectionPanel(iDrop iDropParentForm, CollectionViewHolder collectionViewHolder) throws IdropException {
        super(iDropParentForm, collectionViewHolder, BACKGROUND_COLOR);
    }

    @Override
    protected void setUpFolderIcon(Color backgroundColor) {
        getPnlCollectionIcon().setBackground(backgroundColor);
        getPnlCollectionIcon().add(IconHelper.getFolderOpenIcon());
    }

    @Override
      protected void buildPathLabel() {
        String collectionAbsolutePath = getCollectionViewHolder().getCollection().getPathOrName();
        getLblSeriesAbsolutePath().setText(collectionAbsolutePath.substring(collectionAbsolutePath.lastIndexOf("/")));
    }
}
