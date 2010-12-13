package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.desktop.systraygui.services.IRODSFileService;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * Transfer handler to handle import/export from the IRODSTree that handles the Swing JTree depicting
 * the iRODS file system
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSTreeTransferHandler extends TransferHandler {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IRODSTreeTransferHandler.class);
    public final iDrop idropGui;

    public IRODSTreeTransferHandler(final iDrop idropGui, final String string) {
        super(string);
        if (idropGui == null) {
            throw new IdropRuntimeException("null idrop gui");
        }
        this.idropGui = idropGui;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return super.canImport(support);
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        log.debug("canImport component:{}", comp);
        log.debug("transferFlavors:{}", transferFlavors);
        if (comp instanceof IRODSTree) {
            for (DataFlavor flavor : transferFlavors) {
                if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                    log.debug("found file list flavor, will import");
                    return true;
                }
            }
        }
        log.debug("cannot import");
        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        log.debug("creating a transferrable from the irods tree view");

        List<File> transferFiles = new ArrayList<File>();
        IRODSTree stagingViewTree = (IRODSTree) c;
        // get the selected node (one for now)

        TreePath[] selectionPaths = stagingViewTree.getSelectionModel().getSelectionPaths();
        log.info("transferrable path:{}", selectionPaths);

        IRODSFileService irodsFileService;
        try {
            irodsFileService = new IRODSFileService(idropGui.getIrodsAccount(), idropGui.getIrodsFileSystem());
        } catch (IdropException ex) {
            Logger.getLogger(IRODSTreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }

        IRODSNode listingEntryNode;
        String objectPath;
        for (TreePath selectionPath : selectionPaths) {
            listingEntryNode = (IRODSNode) selectionPath.getLastPathComponent();
            CollectionAndDataObjectListingEntry listingEntry = (CollectionAndDataObjectListingEntry) listingEntryNode.getUserObject();
            if (listingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
                objectPath = listingEntry.getPathOrName();
            } else {
                objectPath = listingEntry.getParentPath() + "/" + listingEntry.getPathOrName();
            }

            try {
                transferFiles.add((File) irodsFileService.getIRODSFileForPath(objectPath));
            } catch (IdropException ex) {
                Logger.getLogger(IRODSTreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
                throw new IdropRuntimeException(ex);
            }
        }

        return new IRODSTreeTransferable(transferFiles, stagingViewTree);
    }
}
