package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.TransferHandler;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 *Handle drop events for the info panel.  Local and iRODS files/collections can be dropped on the info panel to signal transfers.
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public class InfoPanelTransferHandler extends TransferHandler {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(InfoPanelTransferHandler.class);
    public DataFlavor treeDataFlavor;
    private final iDrop idropGui;

    public InfoPanelTransferHandler(final iDrop idropGui) throws IdropException {
        super();

        if (idropGui == null) {
            throw new IdropException("null idropGui");
        }

        this.idropGui = idropGui;

        try {
            treeDataFlavor = new DataFlavor(javax.swing.tree.TreeSelectionModel.class, "application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel");
        } catch (Exception ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }
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
                try {
                    processDropFromTree(transferable, dataFlavors, support);
                } catch (IdropException ex) {
                    Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                }
                return true;
            } else {
                log.info("this transfer is for a file list from desktop");
                try {
                    processDesktopDrop(transferable, dataFlavors, support);
                } catch (IdropException ex) {
                    Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                }
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
    private void processDropFromTree(final Transferable transferable, final DataFlavor[] dataFlavors, final TransferSupport support) throws IdropException {
        log.debug("processDropFromTree()");

        TreeSelectionModel fileTree;
        try {
            fileTree = (TreeSelectionModel) transferable.getTransferData(dataFlavors[0]);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor in drop operation", ex);
        } catch (IOException ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unsupported flavor in drop operation", ex);
        }

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

        Object lastCachedItem = idropGui.getLastCachedInfoItem();
        if (lastCachedItem == null) {
            log.warn("drop on an empty info item, ignored");
        } else if (lastCachedItem instanceof Collection) {
            Collection droppedCollection = (Collection) lastCachedItem;
            log.info("drop onto collection:{}", droppedCollection);

            int ret;
            if (sourcePaths.size() == 1) {
                ret = idropGui.showTransferConfirm(sourcePaths.get(0), droppedCollection.getCollectionName());
            } else {
                ret = idropGui.showTransferConfirm("multiple files", droppedCollection.getCollectionName());
            }

            if (ret == JOptionPane.NO_OPTION) {
                return;
            }

            for (String sourcePath : sourcePaths) {
                processDropOnCollection(droppedCollection, sourcePath);
            }

        } else if (lastCachedItem instanceof DataObject) {

            DataObject droppedDataObject = (DataObject) lastCachedItem;
            log.info("drop onto data object:{}", droppedDataObject);

            int ret;
            if (sourcePaths.size() == 1) {
                ret = idropGui.showTransferConfirm(sourcePaths.get(0), droppedDataObject.getCollectionName());
            } else {
                ret = idropGui.showTransferConfirm("multiple files", droppedDataObject.getCollectionName());
            }

            if (ret == JOptionPane.NO_OPTION) {
                return;
            }

            for (String sourcePath : sourcePaths) {
                processDropOnDataObject(droppedDataObject, sourcePath);
            }

        } else {
            throw new IdropRuntimeException("invalid object type was cached for the info Panel");
        }

    }

    private void processDropOnDataObject(final DataObject dataObject, final String sourcePath) throws IdropException {

        File sourceFile = new File(sourcePath);

        if (!sourceFile.exists()) {
            log.error("dropped file does not exist:{}", sourcePath);
            throw new IdropException("dropped file does not exist");
        }

        log.info("drop of a local file  onto an iRODS data object, will process as a drop on parent collection:{}", dataObject.getCollectionName());

        try {
            log.info("enqueue a put operation");
            idropGui.getTransferManager().enqueueAPut(sourcePath, dataObject.getCollectionName(), idropGui.getIrodsAccount().getDefaultStorageResource(), idropGui.getIrodsAccount());
        } catch (JargonException ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("error enqueing put operation", ex);
        }

    }

    private void processDropOnCollection(final Collection collection, final String sourcePath) throws IdropException {

        File sourceFile = new File(sourcePath);

        if (!sourceFile.exists()) {
            log.error("dropped file does not exist:{}", sourcePath);
            throw new IdropException("dropped file does not exist");
        }

        log.info("drop of a local file  onto an iRODS collection, will process as a drop on parent collection:{}", collection.getCollectionName());

        try {
            log.info("enqueue a put operation");
            idropGui.getTransferManager().enqueueAPut(sourcePath, collection.getCollectionName(), idropGui.getIrodsAccount().getDefaultStorageResource(), idropGui.getIrodsAccount());
        } catch (JargonException ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("error enqueing put operation", ex);
        }

    }

    /**
     * Process a drop on the center panels coming from the desktop or OS explorer
     * @param support
     * @throws IdropRuntimeException
     */
    private void processDesktopDrop(final Transferable transferable, final DataFlavor[] dataFlavors, final TransferSupport support) throws IdropException {


        log.debug("desktop drop event on collection");

        Object lastCachedItem = idropGui.getLastCachedInfoItem();


        List<File> list;
        try {
            list = (List<File>) transferable.getTransferData(
                    DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException(ex);
        } catch (IOException ex) {
            Logger.getLogger(InfoPanelTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException(ex);
        }

        for (File transferFile : list) {

            log.info("processing transfer file:{}", transferFile.getAbsolutePath());
            if (lastCachedItem == null) {
                log.warn("drop on an empty info item, ignored");
            } else if (lastCachedItem instanceof Collection) {
                Collection droppedCollection = (Collection) lastCachedItem;
                log.info("drop onto collection:{}", droppedCollection);

                int ret;
                if (list.size() == 1) {
                    ret = idropGui.showTransferConfirm(transferFile.getAbsolutePath(), droppedCollection.getCollectionName());
                } else {
                    ret = idropGui.showTransferConfirm("multiple files", droppedCollection.getCollectionName());
                }

                if (ret == JOptionPane.NO_OPTION) {
                    return;
                }
                processDropOnCollection(droppedCollection, transferFile.getAbsolutePath());
            } else if (lastCachedItem instanceof DataObject) {

                DataObject droppedDataObject = (DataObject) lastCachedItem;
                log.info("drop onto data object:{}", droppedDataObject);

                int ret;
                if (list.size() == 1) {
                    ret = idropGui.showTransferConfirm(transferFile.getAbsolutePath(), droppedDataObject.getCollectionName());
                } else {
                    ret = idropGui.showTransferConfirm("multiple files", droppedDataObject.getCollectionName());
                }

                if (ret == JOptionPane.NO_OPTION) {
                    return;
                }

                processDropOnDataObject(droppedDataObject, transferFile.getAbsolutePath());
            } else {
                throw new IdropRuntimeException("invalid object type was cached for the info Panel");
            }
        }

        log.info("drop from desktop processed");

    }
}
