/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.desktop.systraygui.DeleteLocalFileDialog;
import org.irods.jargon.idrop.desktop.systraygui.NewLocalDirectoryDialog;
import org.irods.jargon.idrop.desktop.systraygui.RenameLocalDirectoryDialog;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.exceptions.IdropException;

/**
 * JTree for viewing local file system, includes DnD support from StagingViewTree.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class LocalFileTree extends JTree implements DropTargetListener, TreeWillExpandListener {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(LocalFileTree.class);
    private iDrop idropParentGui = null;
    protected JPopupMenu m_popup = null;
    protected Action m_action;
    protected TreePath m_clickedPath;
    protected LocalFileTree thisTree;
    private int highlightedRow = -1;
    private Rectangle dirtyRegion = null;
    private Color highlightColor = new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), 100);

    public LocalFileTree(TreeModel newModel, iDrop idropParentGui) {
        super(newModel);
        this.setCellRenderer(new DefaultTreeCellRenderer());
        setUpDropListener();
        this.idropParentGui = idropParentGui;
        setUpTreeMenu();

    }

    public LocalFileTree() {
        super();
        setUpDropListener();
        this.thisTree = this;
        setUpTreeMenu();
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

        // process a drop onto this tree
        log.info("drop event:{}", dtde);
        Point pt = dtde.getLocation();
        DropTargetContext dtc = dtde.getDropTargetContext();
        JTree tree = (JTree) dtc.getComponent();
        TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
        LocalFileNode nodeThatWasDropTarget = (LocalFileNode) parentpath.getLastPathComponent();
        final File nodeThatWasDropTargetAsFile = (File) nodeThatWasDropTarget.getUserObject();
        log.info("drop node is: {}", nodeThatWasDropTargetAsFile);
        LocalFileSystemModel fileSystemModel = (LocalFileSystemModel) this.getModel();

        Transferable transferable = dtde.getTransferable();

        DataFlavor[] transferrableFlavors = transferable.getTransferDataFlavors();

        for (DataFlavor flavor : transferrableFlavors) {
            log.debug("flavor mime type:{}", flavor.getMimeType());
            if (flavor.isFlavorJavaFileListType()) {
                log.info("process drop as file list");
                dtde.acceptDrop(dtde.getDropAction());
                processDropAfterAcceptingDataFlavor(transferable, nodeThatWasDropTargetAsFile);
                break;
            } else if (flavor.getMimeType().equals("application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel")) {
                log.info("process drop as serialized object");
                dtde.acceptDrop(dtde.getDropAction());
                processDropFromSerializedObjectType(transferable, nodeThatWasDropTargetAsFile);
                break;
            } else if (flavor.getMimeType().equals("application/x-java-jvm-local-objectref; class=org.irods.jargon.idrop.desktop.systraygui.viscomponents.DefaultFileRepresentationPanel")) {
                dtde.acceptDrop(dtde.getDropAction());
                try {
                    processDropFromFileRepresentationPanel(transferable, nodeThatWasDropTargetAsFile, flavor);
                } catch (IdropException ex) {
                    Logger.getLogger(LocalFileTree.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                }
            } else {
                log.debug("flavor not processed: {}", flavor);
            }
        }
    }

    private void processDropAfterAcceptingDataFlavor(Transferable transferable, File nodeThatWasDropTargetAsFile) throws IdropRuntimeException {

        final iDrop idropGui = idropParentGui;
        final List<File> sourceFiles;

        try {
            // get the list of files
            sourceFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException ex) {
            throw new IdropRuntimeException("unsupported flavor getting data from transfer");
        } catch (IOException ex) {
            throw new IdropRuntimeException("io exception getting data from transfer");
        }

        if (sourceFiles.isEmpty()) {
            log.error("no source files in transfer");
            throw new IdropRuntimeException("no source files in transfer");
        }

        final String tempTargetLocalFileAbsolutePath;

        if (nodeThatWasDropTargetAsFile.isDirectory()) {
            tempTargetLocalFileAbsolutePath = nodeThatWasDropTargetAsFile.getAbsolutePath();
        } else {
            log.info("drop target was a file, use the parent collection name for the transfer");
            tempTargetLocalFileAbsolutePath = nodeThatWasDropTargetAsFile.getParent();
        }

        StringBuilder sb = new StringBuilder();

        if (sourceFiles.size() == 1) {
            sb.append("Would you like to copy the remote file ");
            sb.append(sourceFiles.get(0).getAbsolutePath());
            sb.append(" to ");
            sb.append(tempTargetLocalFileAbsolutePath);
        } else {
            sb.append("Would you like to copy multiple files to ");
            sb.append(tempTargetLocalFileAbsolutePath);

        }

        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                sb.toString(),
                "Confirm a Get ",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {

            // process the drop as a get

            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        for (File transferFile : sourceFiles) {

                            if (transferFile instanceof IRODSFile) {
                                log.info("initiating a transfer of iRODS file:{}", transferFile.getAbsolutePath());
                                log.info("transfer to local file:{}", tempTargetLocalFileAbsolutePath);
                                idropGui.getTransferManager().enqueueAGet(transferFile.getAbsolutePath(), tempTargetLocalFileAbsolutePath, "", idropGui.getIrodsAccount());
                            } else {
                                log.info("process a local to local move with source...not yet implemented : {}", transferFile.getAbsolutePath());
                            }
                        }
                    } catch (JargonException ex) {
                        java.util.logging.Logger.getLogger(LocalFileTree.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        idropGui.showIdropException(ex);
                        throw new IdropRuntimeException(ex);
                    }
                }
            });

        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

        Point location = dtde.getLocation();
        int closestRow = this.getClosestRowForLocation((int) location.getX(), (int) location.getY());
        boolean highlighted = false;

        Graphics g = getGraphics();

        // row changed

        if (highlightedRow != closestRow) {
            if (null != dirtyRegion) {
                paintImmediately(dirtyRegion);
            }

            for (int j = 0; j < getRowCount(); j++) {
                if (closestRow == j) {

                    Rectangle firstRowRect = getRowBounds(closestRow);
                    this.dirtyRegion = firstRowRect;
                    g.setColor(highlightColor);

                    g.fillRect((int) dirtyRegion.getX(), (int) dirtyRegion.getY(), (int) dirtyRegion.getWidth(), (int) dirtyRegion.getHeight());
                    highlightedRow = closestRow;
                }
            }

        }

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        if (null != dirtyRegion) {
            paintImmediately(dirtyRegion);
        }
    }

    /**
     * Utility method takes an <code>Enumeration</code> of tree paths, such as would be returned
     * by calling <code>getExpandedDescendants()</code> on the local file tree.  This method will
     * go through the tree paths and expand the nodes.  Note that the nodes are lazily computed, so
     * this method triggers that lazy access.
     * @param currentPaths <code>Enumeration<TreePath></code> with the previously expanded nodes
     * @throws IdropException
     */
    public void expandTreeNodesBasedOnListOfPreviouslyExpandedNodes(final Enumeration<TreePath> currentPaths) throws IdropException {

        log.info("expandTreeNodes()");

        if (currentPaths == null) {
            throw new IdropException("null currentPaths");
        }

        TreePath treePath = null;
        while (currentPaths.hasMoreElements()) {
            treePath = currentPaths.nextElement();
            log.debug("expanding treePath: {}", treePath);
            this.findNodeInTreeGivenATreePathAndExpand(treePath);
        }

    }

    /**
     * Given a treePath, find that path in the tree model.  In searching, the lazy loading
     * behavior of the child nodes is triggered and the tree is expanded to the node.
     * @param treePath <code>TreePath</code> that should be looked up in the tree.
     * @return {@link LocalFileNode} that is the treeNode at the given path.
     * @throws IdropException
     */
    private LocalFileNode findNodeInTreeGivenATreePathAndExpand(final TreePath treePath) throws IdropException {

        if (treePath == null) {
            throw new IdropException("treePath is null");
        }

        log.debug("findNodeInTreeGivenATreePath:{}", treePath);
        LocalFileNode currentTreeNode = (LocalFileNode) this.getModel().getRoot();

        TreePath intermediateTreePath = new TreePath(currentTreeNode);
        boolean rootNodeSkippedInPathElement = false;

        // walk down the treeModel (which had been refreshed), and load and expand each path
        for (Object pathElement : treePath.getPath()) {
            if (!rootNodeSkippedInPathElement) {
                rootNodeSkippedInPathElement = true;
                continue;
            }

            currentTreeNode = matchTreePathToANodeAndExpandLazyChildren(currentTreeNode, pathElement);

            // if null is returned, this means I did not find a matching node, this is ignored
            if (currentTreeNode == null) {
                log.info("no matching node found for {}, stopping search for this tree path", pathElement);
                return null;
            } else {

                // found a node, expand the tree down to this node
                intermediateTreePath = intermediateTreePath.pathByAddingChild(currentTreeNode);
                log.debug("found a node, expanding down to:{}", intermediateTreePath);
                this.expandPath(intermediateTreePath);
            }
        }

        return currentTreeNode;

    }

    /**
     * Given a nodeThatWasDropTargetAsFile node in the tree, search the children for the given path
     * @param localFileNode {@link LocalFileNode} that is the nodeThatWasDropTargetAsFile node that should contain a child node
     * with the given path
     * @param pathElementIAmSearchingFor <code>Object</code> that is the <code>TreePath</code> of the child I am
     * searching for within the given nodeThatWasDropTargetAsFile.
     * @return {@link LocalFileNode} that is the matching child node, or null if no matching child node was discovered.
     * @throws IdropException
     */
    private LocalFileNode matchTreePathToANodeAndExpandLazyChildren(final LocalFileNode localFileNode, final Object pathElementIAmSearchingFor) throws IdropException {

        if (localFileNode == null) {
            throw new IdropException("localFileNode is null");
        }

        LocalFileNode matchedChildNode = null;

        // trigger loading of children so I can search
        localFileNode.lazyLoadOfChildrenOfThisNode();

        LocalFileNode childNode = null;
        Enumeration<LocalFileNode> childNodeEnumeration = localFileNode.children();

        while (childNodeEnumeration.hasMoreElements()) {
            childNode = childNodeEnumeration.nextElement();
            if (childNode.equals(pathElementIAmSearchingFor)) {
                log.debug("found a matching node:{}", childNode);
                matchedChildNode = childNode;
                break;
            }
        }

        // either I'm matched, or I didn't find the child (in which case null is returned).
        return matchedChildNode;

    }

    private void processDropFromSerializedObjectType(Transferable transferable, File parent) {
        log.debug("processing as a drop of a serialized object");
    }

    private void setUpDropListener() throws IdropRuntimeException {
        try {
            DropTarget dt = new DropTarget(this, this);
        } catch (Exception ex) {
            throw new IdropRuntimeException("exception setting up drop listener", ex);
        }
    }

    private void setUpTreeMenu() {
        this.thisTree = this;
        m_popup = new JPopupMenu();
        m_action = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (m_clickedPath == null) {
                    return;
                }

                if (thisTree.isExpanded(m_clickedPath)) {
                    thisTree.collapsePath(m_clickedPath);
                } else {
                    thisTree.expandPath(m_clickedPath);
                }
            }
        };

        m_popup.add(m_action);

        Action newAction = new AbstractAction("New Folder") {

            @Override
            public void actionPerformed(ActionEvent e) {

                java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {

                        log.info("adding new node");
                        LocalFileNode parentNode = (LocalFileNode) m_clickedPath.getLastPathComponent();
                        File parentFile = (File) parentNode.getUserObject();

                        NewLocalDirectoryDialog newLocalDirectoryDialog = new NewLocalDirectoryDialog(idropParentGui, true, parentFile.getAbsolutePath(), thisTree, parentNode);
                        newLocalDirectoryDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
                        newLocalDirectoryDialog.setVisible(true);

                    }
                });
                //  thisTree.repaint();
            }
        };

        m_popup.add(newAction);

        m_popup.addSeparator();

        Action a1 = new AbstractAction("Delete") {

            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("deleting local node node");
                LocalFileNode parentNode = (LocalFileNode) m_clickedPath.getLastPathComponent();
                File parentFile = (File) parentNode.getUserObject();

                DeleteLocalFileDialog deleteLocalFileDialog = new DeleteLocalFileDialog(idropParentGui, true, parentFile.getAbsolutePath(), thisTree, parentNode);
                deleteLocalFileDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
                deleteLocalFileDialog.setVisible(true);

            }
        };
        m_popup.add(a1);


        Action a2 = new AbstractAction("Rename") {

            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("renaming node");

                LocalFileNode parentNode = (LocalFileNode) m_clickedPath.getLastPathComponent();
                File parentFile = (File) parentNode.getUserObject();

                RenameLocalDirectoryDialog renameLocalDirectoryDialog = new RenameLocalDirectoryDialog(idropParentGui, true, parentFile.getAbsolutePath(), thisTree, parentNode);
                renameLocalDirectoryDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
                renameLocalDirectoryDialog.setVisible(true);

            }
        };
        m_popup.add(a2);
        thisTree.add(m_popup);
        thisTree.addMouseListener(new PopupTrigger());
        thisTree.addTreeWillExpandListener(thisTree);

    }

    /**
     * Tree expansion is used to lazily load children of the selected nodeThatWasDropTargetAsFile
     * @param event
     * @throws ExpandVetoException
     */
    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        log.debug("tree expansion event:{}", event);
        LocalFileNode expandingNode = (LocalFileNode) event.getPath().getLastPathComponent();
        expandingNode.lazyLoadOfChildrenOfThisNode();
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    private void processDropFromFileRepresentationPanel(Transferable transferable, File nodeThatWasDropTargetAsFile, DataFlavor dataFlavor) throws IdropException {

        DefaultFileRepresentationPanel fileRepresentationPanel;
        try {
            Object transferObject = transferable.getTransferData(dataFlavor);
            if (!(transferObject instanceof DefaultFileRepresentationPanel)) {
                log.error("unable to cast transferable as a DefaultFileRepresentationPanel");
                throw new IdropRuntimeException("unable to cast transferable as a DefaultFileRepresentationPanel");
            }

            fileRepresentationPanel = (DefaultFileRepresentationPanel) transferObject;

        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(LocalFileTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException(ex);
        } catch (IOException ex) {
            Logger.getLogger(LocalFileTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException(ex);

        }

        CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry = fileRepresentationPanel.getCollectionAndDataObjectListingEntry();
        log.info("drag of filePanel for:{}", collectionAndDataObjectListingEntry);


        final String sourceAbsolutePath;
        if (collectionAndDataObjectListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
            log.info("get of collection");
            sourceAbsolutePath = collectionAndDataObjectListingEntry.getPathOrName();
        } else {
            log.info("get of data object");
            sourceAbsolutePath = collectionAndDataObjectListingEntry.getParentPath() + "/" + collectionAndDataObjectListingEntry.getPathOrName();
        }

        String tempTargetLocalFileAbsolutePath;

        if (nodeThatWasDropTargetAsFile.isDirectory()) {
            tempTargetLocalFileAbsolutePath = nodeThatWasDropTargetAsFile.getAbsolutePath();
        } else {
            log.info("drop target was a file, use the parent collection name for the transfer");
            tempTargetLocalFileAbsolutePath = nodeThatWasDropTargetAsFile.getParent();
        }

        final String targetLocalFileAbsolutePath = tempTargetLocalFileAbsolutePath;

        StringBuilder sb = new StringBuilder();
        sb.append("Would you like to copy the remote file ");
        sb.append(sourceAbsolutePath);
        sb.append(" to ");
        sb.append(targetLocalFileAbsolutePath);
        final iDrop idropGui = idropParentGui;

        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                sb.toString(),
                "Confirm a Get ",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {

            // process the drop as a get

            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {

                    log.info("initiating get transfer");
                    try {
                        idropGui.getTransferManager().enqueueAGet(sourceAbsolutePath, targetLocalFileAbsolutePath, "", idropGui.getIrodsAccount());
                    } catch (JargonException ex) {
                        java.util.logging.Logger.getLogger(LocalFileTree.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        idropGui.showIdropException(ex);
                    }
                }
            });

        }

    }

    class PopupTrigger extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int x = e.getX();
                int y = e.getY();
                TreePath path = thisTree.getPathForLocation(x, y);
                if (path != null) {
                    if (thisTree.isExpanded(path)) {
                        m_action.putValue(Action.NAME, "Collapse");
                    } else {
                        m_action.putValue(Action.NAME, "Expand");
                    }
                    m_popup.show(thisTree, x, y);
                    m_clickedPath = path;
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int x = e.getX();
                int y = e.getY();
                TreePath path = thisTree.getPathForLocation(x, y);
                if (path != null) {
                    if (thisTree.isExpanded(path)) {
                        m_action.putValue(Action.NAME, "Collapse");
                    } else {
                        m_action.putValue(Action.NAME, "Expand");
                    }
                    m_popup.show(thisTree, x, y);
                    m_clickedPath = path;
                }
            }
        }
    }
}
