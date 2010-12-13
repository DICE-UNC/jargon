package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import org.irods.jargon.idrop.desktop.systraygui.IdropDefaultCenterPanel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.irods.jargon.idrop.desktop.systraygui.IdropAbstractCenterPanel;
import org.irods.jargon.idrop.desktop.systraygui.IdropFilePanel;
import org.irods.jargon.idrop.desktop.systraygui.IdropParentCollectionPanel;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * Swing DnD transfer handler handles drop onto collection components
 * @author Mike Conway - DICE (www.irods.org)
 */
public class CollectionTransferHandler extends TransferHandler {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(CollectionTransferHandler.class);
    public DataFlavor treeDataFlavor;

    public CollectionTransferHandler() {
        super();
        try {
            treeDataFlavor = new DataFlavor(javax.swing.tree.TreeSelectionModel.class, "application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel");
        } catch (Exception ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {

        throw new UnsupportedOperationException("wtf");
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {

        log.info("info for transfer:{}", info);
        log.info("tree data for comparison:{}", treeDataFlavor);
        log.debug("compared to file list flavor:{}", DataFlavor.javaFileListFlavor);

        DataFlavor[] dataFlavors = info.getDataFlavors();
        for (DataFlavor dataFlavor : dataFlavors) {
            log.info("data flavor:{}", dataFlavor);
            log.debug("rep class:{}", dataFlavor.getDefaultRepresentationClassAsString());
            log.debug("mime type:{}", dataFlavor.getMimeType());
            if (dataFlavor.getMimeType().equals("application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel")) {
                log.info("can import as a tree selection model");
                return true;
            } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                log.info("can import as a list of files");
                return true;
            }
        }

        log.info("did not find data flavor, cannot import");
        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {

        log.debug("importData()");
        Transferable transferable = support.getTransferable();
        log.debug("drop event on collection");

        DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();

        for (DataFlavor dataFlavor : dataFlavors) {

            if (dataFlavor.getMimeType().equals("application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel")) {
                log.info("processing from drop of java tree");
                processDropFromTree(transferable, dataFlavors, support);
                return true;
            } else {
                log.info("this transfer is for a file list from desktop");
                processDesktopDrop(support);
                return true;
            }
        }
        return false;

    }

    /**
     * This transfer has been marked as able to import, and has been classified by data flavor as
     * coming from a JTree.
     * @param transferable
     * @param dataFlavors
     * @param support
     * @throws IdropRuntimeException
     */
    private void processDropFromTree(final Transferable transferable, final DataFlavor[] dataFlavors, final TransferSupport support) throws IdropRuntimeException {
        log.debug("processDropFromTree()");

        TreeSelectionModel fileTree;
        try {
            fileTree = (TreeSelectionModel) transferable.getTransferData(dataFlavors[0]);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor in drop operation", ex);
        } catch (IOException ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor in drop operation", ex);
        }

        // grab a ref to the collectionPanel that is the drop target

        IdropAbstractCenterPanel folderPanel = (IdropAbstractCenterPanel) support.getComponent();

        String dropTargetPath;

         if (folderPanel instanceof IdropParentCollectionPanel) {
             log.info("drop on a parent collection panel");
            IdropParentCollectionPanel idropParentCollectionPanel = (IdropParentCollectionPanel) folderPanel;
            dropTargetPath = idropParentCollectionPanel.getCollectionViewHolder().getCollection().getPathOrName();
            processDropOnParentCollectionPanel(support, dropTargetPath, fileTree);
        } else if (folderPanel instanceof IdropFilePanel) {
            log.info("drop on a file panel");
            IdropFilePanel idropFilePanel = (IdropFilePanel) folderPanel;
            dropTargetPath = idropFilePanel.getCollectionViewHolder().getCollection().getParentPath();
            processDropOnCollectionPanel(support, dropTargetPath, fileTree);
        } else if (folderPanel instanceof IdropDefaultCenterPanel) {
           log.info("drop on a collection panel");
            IdropDefaultCenterPanel idropCollectionPanel = (IdropDefaultCenterPanel) folderPanel;
            dropTargetPath = idropCollectionPanel.getCollectionViewHolder().getCollection().getPathOrName();
            processDropOnCollectionPanel(support, dropTargetPath, fileTree);
        } else {
            log.error("unknown panel type, cannot cast and determine target path");
            throw new IdropRuntimeException("unknown collection panel type");
        }
    }

    /**
     * Something has been dropped on the center file/collection panel, classify the source and process any drop gestures
     * @param support
     * @param dropTargetPath
     * @param fileTree
     * @throws IdropRuntimeException
     */
    private void processDropOnCollectionPanel(TransferSupport support, String dropTargetPath, TreeSelectionModel fileTree) throws IdropRuntimeException {
        log.debug("processDropOnCollectionPanel()");



        IdropDefaultCenterPanel collectionPanel = (IdropDefaultCenterPanel) support.getComponent();
        log.info("dropTargetPath: {}", dropTargetPath);
        List<String> sourcePaths = new ArrayList<String>();
        TreePath[] selectionPaths = fileTree.getSelectionPaths();
        Object[] path = null;
        StringBuilder pathBuilder = null;
        for (TreePath selectionPath : selectionPaths) {
            path = selectionPath.getPath();
            pathBuilder = new StringBuilder();
            int pathCtr = 0;
            for (Object pathElement : path) {
                pathBuilder.append(pathElement);
                if (pathCtr == 0) {
                    // don't append the /
                } else if (pathCtr == path.length - 1) {
                    // dont append a / to the end
                } else {
                    pathBuilder.append('/');
                }
                pathCtr++;
            }
            String sourcePath = pathBuilder.toString();
            log.info("source path added: {}", sourcePath.toString());
            sourcePaths.add(sourcePath);
        }
        try {
            collectionPanel.processDropOfFile(sourcePaths, dropTargetPath, collectionPanel.getiDropParentForm());
        } catch (IdropException ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }
    }

    /**
     * Something has been dropped on the center panel at the top that represents the parent collection of nodes in the centerl
     * panel scroll.
     * @param support
     * @param dropTargetPath
     * @param fileTree
     * @throws IdropRuntimeException
     */
    private void processDropOnParentCollectionPanel(TransferSupport support, String dropTargetPath, TreeSelectionModel fileTree) throws IdropRuntimeException {
        IdropParentCollectionPanel collectionPanel = (IdropParentCollectionPanel) support.getComponent();
        log.info("dropTargetPath: {}", dropTargetPath);
        List<String> sourcePaths = new ArrayList<String>();
        TreePath[] selectionPaths = fileTree.getSelectionPaths();
        Object[] path = null;
        StringBuilder pathBuilder = null;
        for (TreePath selectionPath : selectionPaths) {
            path = selectionPath.getPath();
            pathBuilder = new StringBuilder();
            int pathCtr = 0;
            for (Object pathElement : path) {
                pathBuilder.append(pathElement);
                if (pathCtr == 0) {
                    // don't append the /
                } else if (pathCtr == path.length - 1) {
                    // dont append a / to the end
                } else {
                    pathBuilder.append('/');
                }
                pathCtr++;
            }
            String sourcePath = pathBuilder.toString();
            log.info("source path added: {}", sourcePath.toString());
            sourcePaths.add(sourcePath);
        }
        try {
            collectionPanel.processDropOfFile(sourcePaths, dropTargetPath, collectionPanel.getiDropParentForm());
        } catch (IdropException ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }
    }

    /**
     * We support both copy and move actions.
     */
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    /**
     * Process a drop on the center panels coming from the desktop or OS explorer
     * @param support
     * @throws IdropRuntimeException
     */
    private void processDesktopDrop(TransferSupport support) throws IdropRuntimeException {

        Transferable transferable = support.getTransferable();
        log.debug("desktop drop event on collection");

        List<File> list;
        IdropAbstractCenterPanel folderPanel = (IdropAbstractCenterPanel) support.getComponent();

        String dropTargetPath;

        if (folderPanel instanceof IdropDefaultCenterPanel) {
            IdropDefaultCenterPanel idropCollectionPanel = (IdropDefaultCenterPanel) folderPanel;
            dropTargetPath = idropCollectionPanel.getCollectionViewHolder().getCollection().getPathOrName();
        } else if (folderPanel instanceof IdropParentCollectionPanel) {
            IdropParentCollectionPanel idropParentCollectionPanel = (IdropParentCollectionPanel) folderPanel;
            dropTargetPath = idropParentCollectionPanel.getCollectionViewHolder().getCollection().getPathOrName();
        } else {
            log.error("unknown panel type, cannot cast and determine target path");
            throw new IdropRuntimeException("unknown collection panel type");
        }

        log.info("dropTargetPath: {}", dropTargetPath);

        try {
            list = (List<File>) transferable.getTransferData(
                    DataFlavor.javaFileListFlavor);

            for (File transferFile : list) {
                try {

                    if (folderPanel instanceof IdropDefaultCenterPanel) {
                        IdropDefaultCenterPanel idropCollectionPanel = (IdropDefaultCenterPanel) folderPanel;
                        idropCollectionPanel.processDropOfFile(transferFile.getAbsolutePath(), dropTargetPath, folderPanel.getiDropParentForm());
                    } else if (folderPanel instanceof IdropParentCollectionPanel) {
                        IdropParentCollectionPanel idropParentCollectionPanel = (IdropParentCollectionPanel) folderPanel;
                        idropParentCollectionPanel.processDropOfFile(transferFile.getAbsolutePath(), dropTargetPath, folderPanel.getiDropParentForm());
                    } else {
                        log.error("unknown panel type, cannot cast and determine target path");
                        throw new IdropRuntimeException("unknown collection panel type");
                    }

                } catch (IdropException ex) {
                    Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                }
            }
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        } catch (IOException ex) {
            Logger.getLogger(CollectionTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }

        log.debug("drag from desktop:{}", list);
    }
}
