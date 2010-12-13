package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.desktop.systraygui.services.IRODSFileService;
import org.irods.jargon.idrop.desktop.systraygui.utils.ColorHelper;
import org.irods.jargon.idrop.desktop.systraygui.IdropDefaultCenterPanel;
import org.irods.jargon.idrop.desktop.systraygui.IdropFilePanel;
import org.irods.jargon.idrop.desktop.systraygui.IdropAbstractCenterPanel;
import org.irods.jargon.idrop.desktop.systraygui.IdropFilePanel;
import org.irods.jargon.idrop.desktop.systraygui.IdropParentCollectionPanel;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 */
public class StagingViewManager implements TreeSelectionListener, TreeExpansionListener {

    private final iDrop idropGui;
    private final IRODSAccount stagingResourceAccount;
    private final JTree stagingResourceTree;
    public static org.slf4j.Logger log = LoggerFactory.getLogger(StagingViewManager.class);

    private StagingViewManager(final iDrop idropGui, final IRODSAccount irodsAccount, final JTree stagingResourceTree) throws IdropException {

        if (idropGui == null) {
            throw new IdropException("idropGui is null");
        }

        if (irodsAccount == null) {
            throw new IdropException("null irodsAccount");
        }

        if (stagingResourceTree == null) {
            throw new IdropException("null stagingResourceTree");
        }

        this.idropGui = idropGui;
        this.stagingResourceAccount = irodsAccount;
        this.stagingResourceTree = stagingResourceTree;

    }

    public static StagingViewManager instance(final iDrop idropGui, final IRODSAccount irodsAccount, final JTree stagingResourceTree) throws IdropException {
        return new StagingViewManager(idropGui, irodsAccount, stagingResourceTree);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)  {
        log.debug("tree selection event: {}", e);
        //Returns the last path element of the selection.
        //This method is useful only when the selection model allows a single selection.

        if (!( stagingResourceTree.getLastSelectedPathComponent() instanceof IRODSNode )) {
            log.info("last selected is not a Node");
            return;
        }

        final StagingViewManager theManager = this;
        final IRODSNode node = (IRODSNode) stagingResourceTree.getLastSelectedPathComponent();
        CollectionAndDataObjectListingEntry entry = (CollectionAndDataObjectListingEntry) node.getUserObject();

        if (entry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT) {
            log.debug("click on data object ignored, does not alter panel view");
            return;
        }

        try {
            buildPanelsFromTreeModel(node, theManager);
        } catch (IdropException ex) {
            Logger.getLogger(StagingViewManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("idropException processing tree selection event", ex);
        }

    }

    /**
     * Method used to set up the iDrop gui center panel to depict the root of the present staging tree
     */
    public void initializeStagingViewToRootOfTree() throws IdropException {
         final StagingViewManager theManager = this;

        TreePath expandedTreePath = new TreePath(stagingResourceTree.getModel().getRoot());
        final IRODSNode expandedNode = (IRODSNode) expandedTreePath.getLastPathComponent();

        buildPanelsFromTreeModel(expandedNode, theManager);

    }

    protected void clearCenterPanel() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                log.debug("removing old target collections from view");
                idropGui.getPnlCollectionsInTargetCollection().removeAll();
                idropGui.getPnlCollectionsInTargetCollection().validate();
                idropGui.getPnlCollectionParent().removeAll();
                idropGui.getPnlCollectionParent().validate();
                idropGui.getScrollCollectionsInTargetCollection().validate();
            }
        });
    }

    public IdropParentCollectionPanel createParentCollectionPanel(final CollectionAndDataObjectListingEntry collection, final CollectionTransferHandler collectionTransferHandler) throws IdropException {

        IdropParentCollectionPanel idropParentCollectionPanel = new IdropParentCollectionPanel(idropGui, new CollectionViewHolder(collection, this));
        idropParentCollectionPanel.setTransferHandler(collectionTransferHandler);
        return idropParentCollectionPanel;
    }

    private void buildPanelsFromTreeModel(final IRODSNode expandedNode, final StagingViewManager theManager) throws IdropException {
        // the expanded node has cached children, use this cache of children to build the collection panels
        final CollectionAndDataObjectListingEntry selectedPath = (CollectionAndDataObjectListingEntry) expandedNode.getUserObject();
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                stagingResourceTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                final CollectionTransferHandler collectionTransferHandler = new CollectionTransferHandler();

                final ColorHelper colorHelper = new ColorHelper();
                List<IdropAbstractCenterPanel> newPanels = new ArrayList<IdropAbstractCenterPanel>();

                CollectionAndDataObjectListingEntry childListingEntry;
                List<IRODSNode> children;
                try {
                    children = expandedNode.getChildren();
                } catch (IdropException ex) {
                    Logger.getLogger(StagingViewManager.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                }
                for (IRODSNode childNode : children) {
                    childListingEntry = (CollectionAndDataObjectListingEntry) childNode.getUserObject();
                    log.debug("processing child listing entry:{}", childListingEntry);
                    try {
                        if (childListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
                            processCollectionType(childListingEntry, theManager, colorHelper, collectionTransferHandler, newPanels);
                        } else {
                            processDataObjectType(childListingEntry, theManager, colorHelper, collectionTransferHandler, newPanels);
                        }
                    }
                    catch (IdropException idropException) {
                        log.error("error building center panel", idropException);
                        throw new IdropRuntimeException(idropException);
                    }
                }
                idropGui.refreshCollectionsScroll(newPanels, (CollectionAndDataObjectListingEntry) expandedNode.getUserObject());
                stagingResourceTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        log.debug("tree expansion event:{}", event);
        final StagingViewManager theManager = this;
        TreePath expandedTreePath = event.getPath();
        final IRODSNode expandedNode = (IRODSNode) expandedTreePath.getLastPathComponent();
        try {
            buildPanelsFromTreeModel(expandedNode, theManager);
        } catch (Exception ex) {
            Logger.getLogger(StagingViewManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("Exception in treeExpanded event", ex);
        }
    }

    private void processCollectionType(CollectionAndDataObjectListingEntry childListingEntry, StagingViewManager theManager, ColorHelper colorHelper, CollectionTransferHandler collectionTransferHandler, List<IdropAbstractCenterPanel> newPanels) throws IdropException {
        CollectionViewHolder collectionViewHolder = new CollectionViewHolder(childListingEntry, theManager);
        IdropDefaultCenterPanel iDropCollectionPanel = new IdropDefaultCenterPanel(idropGui, collectionViewHolder, colorHelper.getNextColor());
        iDropCollectionPanel.setTransferHandler(collectionTransferHandler);
        newPanels.add(iDropCollectionPanel);
    }

    private void processDataObjectType(CollectionAndDataObjectListingEntry childListingEntry, StagingViewManager theManager, ColorHelper colorHelper, CollectionTransferHandler collectionTransferHandler, List<IdropAbstractCenterPanel> newPanels) throws IdropException {
        CollectionViewHolder collectionViewHolder = new CollectionViewHolder(childListingEntry, theManager);
        IdropFilePanel iDropFilePanel = new IdropFilePanel(idropGui, collectionViewHolder, colorHelper.getNextColor());
        iDropFilePanel.setTransferHandler(collectionTransferHandler);
        newPanels.add(iDropFilePanel);
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        //  throw new UnsupportedOperationException("Not supported yet.");
    }
}
