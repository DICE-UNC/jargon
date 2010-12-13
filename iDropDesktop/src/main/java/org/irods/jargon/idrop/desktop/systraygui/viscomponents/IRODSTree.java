package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.Color;
import java.awt.Cursor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.desktop.systraygui.DeleteIRODSDialog;
import org.irods.jargon.idrop.desktop.systraygui.MoveIRODSFileToNewIRODSLocationDialog;
import org.irods.jargon.idrop.desktop.systraygui.NewIRODSDirectoryDialog;
import org.irods.jargon.idrop.desktop.systraygui.RenameIRODSDirectoryDialog;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * Swing JTree component for viewing iRODS server file system
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSTree extends JTree implements DropTargetListener, TreeWillExpandListener, TreeExpansionListener {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IRODSTree.class);
    protected iDrop idropParentGui = null;
    protected JPopupMenu m_popup = null;
    protected Action m_action;
    protected TreePath m_clickedPath;
    protected IRODSTree thisTree;
    private int highlightedRow = -1;
    private Rectangle dirtyRegion = null;
    private Color highlightColor = new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), 100);
    private boolean refreshingTree = false;

    public boolean isRefreshingTree() {
        synchronized (this) {
            return refreshingTree;
        }
    }

    public void setRefreshingTree(boolean refreshingTree) {
        synchronized (this) {
            this.refreshingTree = refreshingTree;
        }
    }

    public IRODSTree(TreeModel newModel, iDrop idropParentGui) {
        super(newModel);
        this.idropParentGui = idropParentGui;
        initializeMenusAndListeners();

        //this.setEditable(true);
    }

    public IRODSTree() {
        super();
    }

    public IRODSTree(iDrop idropParentGui) {
        super();
        this.idropParentGui = idropParentGui;
        initializeMenusAndListeners();
    }

    private void initializeMenusAndListeners() {
        setDragEnabled(true);
        setDropMode(javax.swing.DropMode.ON);
        setTransferHandler(new IRODSTreeTransferHandler(idropParentGui, "selectionModel"));
        setUpTreeMenu();
        setUpDropListener();
        addTreeExpansionListener(this);
        addTreeWillExpandListener(this);
    }

    /**
     * Set up context sensitive tree menu
     */
    private void setUpTreeMenu() {
        thisTree = this;
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

            public void actionPerformed(ActionEvent e) {

//                java.awt.EventQueue.invokeLater(new Runnable() {
//
//                    public void run() {

                log.info("adding new node");

                IRODSNode parent = (IRODSNode) m_clickedPath.getLastPathComponent();
                log.info("parent of new node is: {}", parent);
                CollectionAndDataObjectListingEntry dataEntry = (CollectionAndDataObjectListingEntry) parent.getUserObject();
                if (dataEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT) {
                    JOptionPane.showMessageDialog(thisTree,
                            "The selected item is not a folder, cannot create a new directory",
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                    log.info("new folder not created, the selected parent is not a collection");
                    return;
                }
                // show a dialog asking for the new directory name...
                NewIRODSDirectoryDialog newDirectoryDialog = new NewIRODSDirectoryDialog(idropParentGui, true, dataEntry.getPathOrName(), thisTree, parent);
                newDirectoryDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
                newDirectoryDialog.setVisible(true);
            }
            //   });
            //  thisTree.repaint();
        };
        m_popup.add(newAction);

        m_popup.addSeparator();

        Action a1 = new AbstractAction("Delete") {

            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("deleting a node");

                TreePath[] selects = thisTree.getSelectionPaths();
                DeleteIRODSDialog deleteDialog;

                if (selects.length == 1) {
                    IRODSNode toDelete = (IRODSNode) m_clickedPath.getLastPathComponent();
                    log.info("deleting a single node: {}", toDelete);
                    deleteDialog = new DeleteIRODSDialog(idropParentGui, true, thisTree, toDelete);
                } else {
                    List<IRODSNode> nodesToDelete = new ArrayList<IRODSNode>();
                    for (TreePath treePath : selects) {
                        nodesToDelete.add((IRODSNode) treePath.getLastPathComponent());
                    }
                    deleteDialog = new DeleteIRODSDialog(idropParentGui, true, thisTree, nodesToDelete);
                }

                deleteDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
                deleteDialog.setVisible(true);
            }
        };

        m_popup.add(a1);
        Action a2 = new AbstractAction("Rename") {

            public void actionPerformed(ActionEvent e) {
                log.info("renaming node");

                IRODSNode toRename = (IRODSNode) m_clickedPath.getLastPathComponent();
                log.info("node to rename  is: {}", toRename);
                CollectionAndDataObjectListingEntry dataEntry = (CollectionAndDataObjectListingEntry) toRename.getUserObject();

                //dialog uses absolute path, so munge it for files
                StringBuilder sb = new StringBuilder();
                if (dataEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
                    sb.append(dataEntry.getPathOrName());
                } else {
                    sb.append(dataEntry.getParentPath());
                    sb.append('/');
                    sb.append(dataEntry.getPathOrName());
                }

                // show a dialog asking for the new directory name...
                RenameIRODSDirectoryDialog renameDialog = new RenameIRODSDirectoryDialog(idropParentGui, true, sb.toString(), thisTree, toRename);
                renameDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
                renameDialog.setVisible(true);
            }
        };
        m_popup.add(a2);
        thisTree.add(m_popup);
        thisTree.addMouseListener(new PopupTrigger());

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

        // drop from explorer - human presentable name = application/x-java-url; class=java.net.URL
        // process a drop onto this tree
        log.info("drop event:{}", dtde);
        Point pt = dtde.getLocation();
        DropTargetContext dtc = dtde.getDropTargetContext();
        JTree tree = (JTree) dtc.getComponent();
        TreePath targetPath = tree.getClosestPathForLocation(pt.x, pt.y);
        IRODSNode targetNode = (IRODSNode) targetPath.getLastPathComponent();
        log.info("drop node is: {}", targetNode);
        IRODSFileSystemModel irodsFileSystemModel = (IRODSFileSystemModel) this.getModel();
        // irods file system model beneath the tree may not have cached the children of this targetNode, trigger the cache operation

        /*
        if (irodsFileSystemModel.isLeaf(targetNode)) {
        log.debug("drop rejected");
        dtde.rejectDrop();
        return;
        }

         */

        Transferable transferable = dtde.getTransferable();

        DataFlavor[] transferrableFlavors = transferable.getTransferDataFlavors();

        // see if this is a phymove gesture
        if (transferable.isDataFlavorSupported(IRODSTreeTransferable.localPhymoveFlavor)) {
            log.info("drop accepted, process as a move");
            dtde.acceptDrop(dtde.getDropAction());
            processPhymoveGesture(transferable, targetNode);
            return;
        }

        // not a phymove

        boolean accepted = false;

        for (DataFlavor flavor : transferrableFlavors) {
            log.debug("flavor mime type:{}", flavor.getMimeType());
            log.debug("flavor human presentable name:{}", flavor.getHumanPresentableName());
            if (flavor.isFlavorJavaFileListType()) {
                log.info("drop accepted...process drop as file list from desktop");
                dtde.acceptDrop(dtde.getDropAction());
                processDropOfFileList(transferable, targetNode);
                accepted = true;
                break;
            } else if (flavor.getMimeType().equals("application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel")) {
                log.info("drop accepted: process drop as serialized object");
                dtde.acceptDrop(dtde.getDropAction());
                processDropOfTreeSelectionModel(transferable, targetNode, flavor);
                accepted = true;
                break;
            } else {
                log.debug("flavor not processed: {}", flavor);
            }
        }

        if (!accepted) {
            log.info("drop rejected");
            dtde.rejectDrop();
        }

    }

    // handle a drop from the local file system
    private void processDropOfFileList(Transferable transferable, IRODSNode parent) throws IdropRuntimeException {

        log.info("process as drop of file list");

        final String sourceResource = idropParentGui.getIrodsAccount().getDefaultStorageResource();
        final iDrop idropGui = idropParentGui;
        final List<File> sourceFiles;
        CollectionAndDataObjectListingEntry putTarget = (CollectionAndDataObjectListingEntry) parent.getUserObject();
        final String targetIrodsFileAbsolutePath = putTarget.getPathOrName();

        try {
            // get the list of files
            sourceFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor getting data from transfer");
        } catch (IOException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("io exception getting data from transfer");
        }

        if (sourceFiles.isEmpty()) {
            log.error("no source files in transfer");
            throw new IdropRuntimeException("no source files in transfer");
        }


        StringBuilder sb = new StringBuilder();

        if (sourceFiles.size() > 1) {
            sb.append("Would you like to put multiple files");
            sb.append(" to iRODS at ");
            sb.append(putTarget.getPathOrName());
        } else {
            sb.append("Would you like to put the file  ");
            sb.append(sourceFiles.get(0).getAbsolutePath());
            sb.append(" to iRODS at ");
            sb.append(putTarget.getPathOrName());
        }

        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                sb.toString(),
                "Confirm a Put to iRODS ",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {

            // process the drop as a put

            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {

                    for (File transferFile : sourceFiles) {
                        log.info("initiating put transfer for source file:{}", transferFile.getAbsolutePath());
                        try {
                            idropGui.getTransferManager().enqueueAPut(transferFile.getAbsolutePath(), targetIrodsFileAbsolutePath, sourceResource, idropGui.getIrodsAccount());
                        } catch (JargonException ex) {
                            java.util.logging.Logger.getLogger(LocalFileTree.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            idropGui.showIdropException(ex);
                        }
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

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        if (null != dirtyRegion) {
            paintImmediately(dirtyRegion);
        }
    }
    // processes drop from local file tree

    private void processDropOfTreeSelectionModel(final Transferable transferable, final IRODSNode parent, final DataFlavor dataFlavor) {
        final List<File> sourceFiles = new ArrayList<File>();
        CollectionAndDataObjectListingEntry putTarget = (CollectionAndDataObjectListingEntry) parent.getUserObject();
        final String targetIrodsFileAbsolutePath;

        if (putTarget.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
            targetIrodsFileAbsolutePath = putTarget.getPathOrName();
        } else {
            targetIrodsFileAbsolutePath = putTarget.getParentPath();
        }

        final iDrop idropGui = idropParentGui;

        try {
            // get the list of files
            TreeSelectionModel transferableSelectionModel = (TreeSelectionModel) transferable.getTransferData(dataFlavor);
            TreePath[] treePaths = transferableSelectionModel.getSelectionPaths();

            for (TreePath treePath : treePaths) {
                LocalFileNode lastPathComponent = (LocalFileNode) treePath.getLastPathComponent();
                sourceFiles.add((File) lastPathComponent.getUserObject());
            }

        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor getting data from transfer");
        } catch (IOException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("io exception getting data from transfer");
        }

        if (sourceFiles.isEmpty()) {
            log.error("no source files in transfer");
            throw new IdropRuntimeException("no source files in transfer");
        }

        StringBuilder sb = new StringBuilder();

        if (sourceFiles.size() > 1) {
            sb.append("Would you like to put multiple files");
            sb.append(" to iRODS at ");
            sb.append(targetIrodsFileAbsolutePath);
        } else {
            sb.append("Would you like to put the file  ");
            sb.append(sourceFiles.get(0).getAbsolutePath());
            sb.append(" to iRODS at ");
            sb.append(targetIrodsFileAbsolutePath);
        }

        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                sb.toString(),
                "Confirm a Put to iRODS ",
                JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {

            // process the drop as a put

            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {

                    for (File transferFile : sourceFiles) {
                        log.info("process a put from source: {}", transferFile.getAbsolutePath());

                        String localSourceAbsolutePath = transferFile.getAbsolutePath();
                        String sourceResource = idropParentGui.getIrodsAccount().getDefaultStorageResource();
                        log.info("initiating put transfer");
                        try {
                            idropGui.getTransferManager().enqueueAPut(localSourceAbsolutePath, targetIrodsFileAbsolutePath, sourceResource, idropGui.getIrodsAccount());
                        } catch (JargonException ex) {
                            java.util.logging.Logger.getLogger(LocalFileTree.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            idropGui.showIdropException(ex);
                        }
                    }
                }
            });
        }

    }

    private void setUpDropListener() throws IdropRuntimeException {
        try {
            DropTarget dt = new DropTarget(this, this);
        } catch (Exception ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("too many event listeners");
        }
    }

    private void processPhymoveGesture(Transferable transferable, IRODSNode targetNode) {
        log.info("process as drop of file list");

        List<IRODSFile> sourceFiles;
        CollectionAndDataObjectListingEntry targetEntry = (CollectionAndDataObjectListingEntry) targetNode.getUserObject();
        if (targetEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT) {
            log.warn("attempt to move a file to a data object, must be a collection");
            idropParentGui.showMessageFromOperation("unable to move file, the target of the move is not a collection");
            return;
        }

        try {
            // get the list of files
            sourceFiles = (List<IRODSFile>) transferable.getTransferData(IRODSTreeTransferable.localPhymoveFlavor);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor getting data from transfer");
        } catch (IOException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("io exception getting data from transfer");
        }

        if (sourceFiles.isEmpty()) {
            log.error("no source files in transfer");
            throw new IdropRuntimeException("no source files in transfer");
        }

        String targetFileAbsolutePath = targetEntry.getPathOrName();
        MoveIRODSFileToNewIRODSLocationDialog moveIRODSFileOrDirectoryDialog;
        if (sourceFiles.size() == 1) {
            moveIRODSFileOrDirectoryDialog = new MoveIRODSFileToNewIRODSLocationDialog(idropParentGui, true, targetNode, this, sourceFiles.get(0), targetFileAbsolutePath);
        } else {
            moveIRODSFileOrDirectoryDialog = new MoveIRODSFileToNewIRODSLocationDialog(idropParentGui, true, targetNode, this, sourceFiles, targetFileAbsolutePath);
        }

        moveIRODSFileOrDirectoryDialog.setLocation((int) (idropParentGui.getLocation().getX() + idropParentGui.getWidth() / 2), (int) (idropParentGui.getLocation().getY() + idropParentGui.getHeight() / 2));
        moveIRODSFileOrDirectoryDialog.setVisible(true);

    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
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

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        log.debug("tree expansion event:{}", event);
        IRODSNode expandingNode = (IRODSNode) event.getPath().getLastPathComponent();
        // If I am refreshing the tree, then do not close the connection after each load.  It will be closed in the thing doing the refreshing
        try {
            expandingNode.lazyLoadOfChildrenOfThisNode(!isRefreshingTree());
        } catch (IdropException ex) {
            Logger.getLogger(IRODSTree.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("error expanding irodsNode");
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        }
    }
}
