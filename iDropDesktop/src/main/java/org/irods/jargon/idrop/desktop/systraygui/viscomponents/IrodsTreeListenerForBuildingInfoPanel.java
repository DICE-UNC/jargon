/*
 *A listener for iRODS tree selected
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate handling of info panel.  This object will listen to tree selection events in the iDROP
 * iRODS tree, and initialize the info panel.
 *
 *
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IrodsTreeListenerForBuildingInfoPanel implements TreeSelectionListener, TreeExpansionListener {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IrodsTreeListenerForBuildingInfoPanel.class);
    private final iDrop idrop;

    public IrodsTreeListenerForBuildingInfoPanel(final iDrop idrop) throws IdropException {
        if (idrop == null) {
            throw new IdropException("null iDrop");
        }

        this.idrop = idrop;

    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {


        if (!(idrop.getIrodsTree().getLastSelectedPathComponent() instanceof IRODSNode)) {
            log.info("last selected is not a Node");
            return;
        }

        final IRODSNode node = (IRODSNode) idrop.getIrodsTree().getLastSelectedPathComponent();
        try {
            identifyNodeTypeAndInitializeInfoPanel(node);
        } catch (IdropException ex) {
            Logger.getLogger(IrodsTreeListenerForBuildingInfoPanel.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("exception processing valueChanged() event for IRODSNode selection");
        }

    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        /*TreePath expandedTreePath = event.getPath();
        IRODSNode expandedNode = (IRODSNode) expandedTreePath.getLastPathComponent();
        try {
            identifyNodeTypeAndInitializeInfoPanel(expandedNode);
        } catch (IdropException ex) {
            Logger.getLogger(IrodsTreeListenerForBuildingInfoPanel.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("exception processing treeExpanded() event for IRODSNode selection");
        }*/
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        // operation not needed, left for interface contract
    }

    /**
     * Look at the kind of irods node and handle appropriately
     * @param irodsNode
     * @throws IdropException
     */
    public  void identifyNodeTypeAndInitializeInfoPanel(final IRODSNode irodsNode) throws IdropException {
    
        if (!idrop.getToggleIrodsDetails().isSelected()) {
            return;
        }
        
        if (irodsNode.isLeaf()) {
            log.info("selected node is a leaf, get a data object");
            buildDataObjectFromSelectedIRODSNodeAndGiveToInfoPanel(irodsNode);
        } else {
            log.info("selected node is a collection, get a collection object");
            buildCollectionFromSelectedIRODSNodeAndGiveToInfoPanel(irodsNode);
        }
    }

    /**
     * When a selected node in the iRODS tree is a data object, put the data object info in the info panel
     * @param irodsNode
     */
    private void buildDataObjectFromSelectedIRODSNodeAndGiveToInfoPanel(final IRODSNode irodsNode) throws IdropException {
        try {
            CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = (CollectionAndDataObjectListingEntry) irodsNode.getUserObject();
            log.info("will be getting a data object based on entry in IRODSNode:{}", irodsNode);
            DataObjectAO dataObjectAO = idrop.getIrodsFileSystem().getIRODSAccessObjectFactory().getDataObjectAO(idrop.getIrodsAccount());
            DataObject dataObject = dataObjectAO.findByCollectionNameAndDataName(collectionAndDataObjectListingEntry.getParentPath(), collectionAndDataObjectListingEntry.getPathOrName());
            idrop.initializeInfoPanel(dataObject);
        } catch (Exception e) {
            log.error("error building data object for: {}", irodsNode);
            throw new IdropException(e);
        }
    }

    /**
     * When a selected node in the iRODS tree is a collection, put the collection info into the info panel
     * @param irodsNode
     */
    private void buildCollectionFromSelectedIRODSNodeAndGiveToInfoPanel(final IRODSNode irodsNode) throws IdropException {
         try {
            CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = (CollectionAndDataObjectListingEntry) irodsNode.getUserObject();
            log.info("will be getting a collection based on entry in IRODSNode:{}", irodsNode);
            CollectionAO collectionAO = idrop.getIrodsFileSystem().getIRODSAccessObjectFactory().getCollectionAO(idrop.getIrodsAccount());
            Collection collection = collectionAO.findByAbsolutePath(collectionAndDataObjectListingEntry.getPathOrName());
            idrop.initializeInfoPanel(collection);
        } catch (Exception e) {
            log.error("error building collection objectt for: {}", irodsNode);
            throw new IdropException(e);
        
        }
    }
}
