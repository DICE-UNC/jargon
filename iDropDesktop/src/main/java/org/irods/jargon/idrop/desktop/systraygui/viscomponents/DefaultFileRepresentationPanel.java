
/*
 * DefaultFileRepresentationPanel.java
 *
 * Created on Nov 12, 2010, 1:06:41 PM
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.slf4j.LoggerFactory;

/**
 * Panel used in various displays for a file or collection, eventually this can shift to a JTable, as the gui design has changed
 * @author mikeconway
 */
public class DefaultFileRepresentationPanel extends javax.swing.JPanel implements Transferable {

    private final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry;
    private final iDrop idrop;
    private final Color backgroundColor;
    private DataFlavor dragAndDropPanelDataFlavor = null;
    public static org.slf4j.Logger log = LoggerFactory.getLogger(DefaultFileRepresentationPanel.class);

    /**
     * <p>Returns (creating, if necessary) the DataFlavor representing RandomDragAndDropPanel</p>
     * @return
     */
    public DataFlavor getDragAndDropPanelDataFlavor() throws Exception {
        // Lazy load/create the flavor
        if (dragAndDropPanelDataFlavor == null) {
            dragAndDropPanelDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.irods.jargon.idrop.desktop.systraygui.viscomponents.DefaultFileRepresentationPanel");
        }

        return dragAndDropPanelDataFlavor;
    }

    /**
     * Create an instance of a data panel for display in iDroop
     * @param collectionAndDataObjectListingEntry <code>CollectionAndDataObjectListingEntry</code> with file/collection details
     * @param idrop <code>iDrop</code> gui reference.
     */
    public DefaultFileRepresentationPanel(final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry, final iDrop idrop, final Color backgroundColor) {
        super();
        try {
            getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            Logger.getLogger(DefaultFileRepresentationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

        this.collectionAndDataObjectListingEntry = collectionAndDataObjectListingEntry;
        this.idrop = idrop;
        this.backgroundColor = backgroundColor;


        if (collectionAndDataObjectListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
            pnlIcon.add(IconHelper.getFolderIcon());
            try {
                lblFileOrCollectionName.setText(collectionAndDataObjectListingEntry.getLastPathComponentForCollectionName());
            } catch (JargonException ex) {
                Logger.getLogger(DefaultFileRepresentationPanel.class.getName()).log(Level.SEVERE, null, ex);
                throw new IdropRuntimeException(ex);
            }
        } else {
            pnlIcon.add(IconHelper.getFileIcon());
            lblFileOrCollectionName.setText(collectionAndDataObjectListingEntry.getPathOrName());
        }

        this.setToolTipText(collectionAndDataObjectListingEntry.getParentPath());
        pnlIcon.setBackground(backgroundColor);
        pnlFileDetails.setBackground(backgroundColor);
        pnlShowInHierarchy.setBackground(backgroundColor);
        lblFileOrCollectionName.setBackground(backgroundColor);
        DragAndDropTransferHandler th = new DragAndDropTransferHandler(idrop);
        setTransferHandler(th);
        addMouseListener(new DraggableMouseListener(idrop, collectionAndDataObjectListingEntry));


        //setUpDrag();
    }

    private void setUpDrag() {
        this.setTransferHandler(new TransferHandler("defaultFileRepresentationPanel"));
        MouseListener ml = new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                JComponent jc = (JComponent) e.getSource();
                TransferHandler th = jc.getTransferHandler();
                th.exportAsDrag(jc, e, TransferHandler.COPY);
            }
        };
        this.addMouseListener(ml);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlIcon = new javax.swing.JPanel();
        pnlFileDetails = new javax.swing.JPanel();
        lblFileOrCollectionName = new javax.swing.JLabel();
        pnlShowInHierarchy = new javax.swing.JPanel();
        btnShowInHierarchy = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(2147483647, 80));
        setPreferredSize(new java.awt.Dimension(482, 80));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        pnlIcon.setBackground(new java.awt.Color(255, 255, 255));
        pnlIcon.setPreferredSize(new java.awt.Dimension(32, 32));
        pnlIcon.setLayout(new java.awt.GridLayout(0, 1));
        add(pnlIcon, java.awt.BorderLayout.WEST);

        pnlFileDetails.setBackground(new java.awt.Color(255, 255, 255));
        pnlFileDetails.setLayout(new java.awt.GridLayout(0, 1));

        lblFileOrCollectionName.setText("file or collection name here");
        pnlFileDetails.add(lblFileOrCollectionName);

        add(pnlFileDetails, java.awt.BorderLayout.CENTER);

        pnlShowInHierarchy.setBackground(new java.awt.Color(255, 255, 255));
        pnlShowInHierarchy.setPreferredSize(new java.awt.Dimension(30, 30));

        btnShowInHierarchy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/irods/jargon/idrop/desktop/systraygui/images/show-in-hierarchy.png"))); // NOI18N
        btnShowInHierarchy.setToolTipText("Show this file or collection in the hierarchy view");

        org.jdesktop.layout.GroupLayout pnlShowInHierarchyLayout = new org.jdesktop.layout.GroupLayout(pnlShowInHierarchy);
        pnlShowInHierarchy.setLayout(pnlShowInHierarchyLayout);
        pnlShowInHierarchyLayout.setHorizontalGroup(
            pnlShowInHierarchyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 30, Short.MAX_VALUE)
            .add(pnlShowInHierarchyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlShowInHierarchyLayout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(btnShowInHierarchy)
                    .add(0, 0, Short.MAX_VALUE)))
        );
        pnlShowInHierarchyLayout.setVerticalGroup(
            pnlShowInHierarchyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 298, Short.MAX_VALUE)
            .add(pnlShowInHierarchyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlShowInHierarchyLayout.createSequentialGroup()
                    .add(0, 135, Short.MAX_VALUE)
                    .add(btnShowInHierarchy)
                    .add(0, 135, Short.MAX_VALUE)))
        );

        add(pnlShowInHierarchy, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * On a mouse click, set up the info panel
     * @param evt
     */
    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
       
    }//GEN-LAST:event_formMouseClicked

    /**
     * Get the file/collection data object that backs this panel
     * @return
     */
    public CollectionAndDataObjectListingEntry getCollectionAndDataObjectListingEntry() {
        return collectionAndDataObjectListingEntry;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnShowInHierarchy;
    private javax.swing.JLabel lblFileOrCollectionName;
    private javax.swing.JPanel pnlFileDetails;
    private javax.swing.JPanel pnlIcon;
    private javax.swing.JPanel pnlShowInHierarchy;
    // End of variables declaration//GEN-END:variables

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>If multiple DataFlavor's are supported, can choose what Object to return.</p>
     * <p>In this case, we only support one: the actual JPanel.</p>
     * <p>Note we could easily support more than one. For example, if supports text and drops to a JTextField, could return the label's text or any arbitrary text.</p>
     * @param flavor
     * @return
     */
    public Object getTransferData(DataFlavor flavor) {

        DataFlavor thisFlavor = null;

        try {
            thisFlavor = getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            System.err.println("Problem lazy loading: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return null;
        }

        // For now, assume wants this class... see loadDnD
        if (thisFlavor != null && flavor.equals(thisFlavor)) {
            return this;
        }

        return null;
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Returns supported DataFlavor. Again, we're only supporting this actual Object within the JVM.</p>
     * <p>For more information, see the JavaDoc for DataFlavor.</p>
     * @return
     */
    public DataFlavor[] getTransferDataFlavors() {

        DataFlavor[] flavors = {null};

        System.out.println("Step 4 of 7: Querying for acceptable DataFlavors to determine what is available. Our example only supports our custom RandomDragAndDropPanel DataFlavor.");

        try {
            flavors[0] = getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            System.err.println("Problem lazy loading: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return null;
        }

        return flavors;
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Determines whether this object supports the DataFlavor. In this case, only one is supported: for this object itself.</p>
     * @param flavor
     * @return True if DataFlavor is supported, otherwise false.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {

        System.out.println("Step 6 of 7: Verifying that DataFlavor is supported.  Our example only supports our custom RandomDragAndDropPanel DataFlavor.");

        DataFlavor[] flavors = {null};
        try {
            flavors[0] = getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            System.err.println("Problem lazy loading: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return false;
        }

        for (DataFlavor f : flavors) {
            if (f.equals(flavor)) {
                return true;
            }
        }

        return false;
    }
}

class DraggableMouseListener extends MouseAdapter {

    private final iDrop idrop;
    private final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry;

    public DraggableMouseListener(final iDrop idrop, final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
        super();
        this.idrop = idrop;
        this.collectionAndDataObjectListingEntry = collectionAndDataObjectListingEntry;
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        
    }

    @Override()
    public void mousePressed(MouseEvent e) {
           try {
            // TODO add your handling code here:
            idrop.initializeInfoPane(collectionAndDataObjectListingEntry);
        } catch (IdropException ex) {
            Logger.getLogger(DefaultFileRepresentationPanel.class.getName()).log(Level.SEVERE, null, ex);
            idrop.showIdropException(ex);
            throw new IdropRuntimeException(ex);
        }

        JComponent c = (JComponent) e.getSource();
        TransferHandler handler = c.getTransferHandler();
        handler.exportAsDrag(c, e, TransferHandler.COPY);
    }
} // DraggableMouseListener

class DragAndDropTransferHandler extends TransferHandler implements DragSourceMotionListener {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(DragAndDropTransferHandler.class);
    public DataFlavor treeDataFlavor;
    private final iDrop idropGui;

    public DragAndDropTransferHandler(final iDrop idropGui) {
        super();
        this.idropGui = idropGui;
        try {
            treeDataFlavor = new DataFlavor(javax.swing.tree.TreeSelectionModel.class, "application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel");
        } catch (Exception ex) {
            log.error("error building tree data flavor", ex);
            throw new IdropRuntimeException(ex);
        }
    }

    /**
     * <p>This creates the Transferable object. In our case, RandomDragAndDropPanel implements Transferable, so this requires only a type cast.</p>
     * @param c
     * @return
     */
    @Override()
    public Transferable createTransferable(JComponent c) {

        return (Transferable) c;
    }

    @Override
    public void dragMouseMoved(DragSourceDragEvent dsde) {
    }

    /**
     * <p>This is queried to see whether the component can be copied, moved, both or neither. We are only concerned with copying.</p>
     * @param c
     * @return
     */
    @Override()
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
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
        log.debug("drop event on a file representation panel");

        DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();

        for (DataFlavor dataFlavor : dataFlavors) {

            if (dataFlavor.getMimeType().equals("application/x-java-jvm-local-objectref; class=javax.swing.tree.TreeSelectionModel")) {
                log.info("processing from drop of java tree");
                try {
                    processDropFromTree(transferable, dataFlavors, support);
                } catch (IdropException ex) {
                    log.error("error processing DropFromTree", ex);
                    throw new IdropRuntimeException(ex);
                }
                return true;
            } else {
                log.info("this transfer is for a file list from desktop");
                try {
                    processDesktopDrop(transferable, dataFlavors, support);
                } catch (IdropException ex) {
                    log.error("error processing DropFromTree", ex);
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
            log.error("unsupported operation getting filetree", ex);
            throw new IdropRuntimeException("unsupported flavor in drop operation", ex);
        } catch (IOException ex) {
            log.error("IOException getting filetree", ex);
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

        // make sure it's a drop onto the file panel and not some other unexpected drop target.

        Object dropComponent = support.getComponent();

        if (!(dropComponent instanceof DefaultFileRepresentationPanel)) {
            log.error("drop event is not on a DefaultFileRepresentationPanel");
            throw new IdropException("drop event is not on a DefaultFileRepresentationPanel");
        }

        DefaultFileRepresentationPanel targetPanel = (DefaultFileRepresentationPanel) dropComponent;
        CollectionAndDataObjectListingEntry targetCollectionAndDataObjectListingEntry = targetPanel.getCollectionAndDataObjectListingEntry();
        log.info("drop onto:{}", targetCollectionAndDataObjectListingEntry);

        // if dropping onto a data object, act as if the drop is on the parent collection
        String derivedTargetAbsolutePath;
        if (targetCollectionAndDataObjectListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT) {
            log.info("using parent collection, as target is a data object");
            derivedTargetAbsolutePath = targetCollectionAndDataObjectListingEntry.getParentPath();
        } else {
            log.info("drop onto collection, just using collection name");
            derivedTargetAbsolutePath = targetCollectionAndDataObjectListingEntry.getPathOrName();
        }

        int ret;
        if (sourcePaths.size() == 1) {
            ret = idropGui.showTransferConfirm(sourcePaths.get(0), derivedTargetAbsolutePath);
        } else {
            ret = idropGui.showTransferConfirm("multiple files", derivedTargetAbsolutePath);
        }

        if (ret == JOptionPane.NO_OPTION) {
            return;
        }
        
        // FIXME: put into swing worker
        
        for (String sourcePath : sourcePaths) {
            try {
                log.info("enqueue a put operation");
                idropGui.getTransferManager().enqueueAPut(sourcePath, derivedTargetAbsolutePath, idropGui.getIrodsAccount().getDefaultStorageResource(), idropGui.getIrodsAccount());
            } catch (JargonException ex) {
                log.error("error enqueueing a put operation", ex);
                throw new IdropException("error enqueing put operation", ex);
            }

            log.info("transfer enqueued");
        }

    }

    /**
     * Process a drop on the center panels coming from the desktop or OS explorer
     * @param support
     * @throws IdropRuntimeException
     */
    private void processDesktopDrop(final Transferable transferable, final DataFlavor[] dataFlavors, final TransferSupport support) throws IdropException {

        // make sure it's a drop onto the file panel and not some other unexpected drop target.

        Object dropComponent = support.getComponent();

        if (!(dropComponent instanceof DefaultFileRepresentationPanel)) {
            log.error("drop event is not on a DefaultFileRepresentationPanel");
            throw new IdropException("drop event is not on a DefaultFileRepresentationPanel");
        }

        DefaultFileRepresentationPanel targetPanel = (DefaultFileRepresentationPanel) dropComponent;
        CollectionAndDataObjectListingEntry targetCollectionAndDataObjectListingEntry = targetPanel.getCollectionAndDataObjectListingEntry();
        log.info("drop onto:{}", targetCollectionAndDataObjectListingEntry);

        List<File> list;
        try {
            list = (List<File>) transferable.getTransferData(
                    DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException ex) {

            log.error("UnsupportedFlavor error in processDesktopDrop", ex);
            throw new IdropException(ex);
        } catch (IOException ex) {
            log.error("IOException error in processDesktopDrop", ex);
            throw new IdropException(ex);
        }

       String derivedTargetAbsolutePath;
            if (targetCollectionAndDataObjectListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT) {
                log.info("using parent collection, as target is a data object");
                derivedTargetAbsolutePath = targetCollectionAndDataObjectListingEntry.getParentPath();
            } else {
                log.info("drop onto collection, just using collection name");
                derivedTargetAbsolutePath = targetCollectionAndDataObjectListingEntry.getPathOrName();
            }


         int ret;
        if (list.size() == 1) {
            ret = idropGui.showTransferConfirm(list.get(0).getAbsolutePath(), derivedTargetAbsolutePath);
        } else {
            ret = idropGui.showTransferConfirm("multiple files", derivedTargetAbsolutePath);
        }

        if (ret == JOptionPane.NO_OPTION) {
            return;
        }

         //FIXME: put into swing worker thread

        for (File transferFile : list) {

            log.info("processing transfer file:{}", transferFile.getAbsolutePath());
            // if dropping onto a data object, act as if the drop is on the parent collection
        
            try {
                log.info("enqueue a put operation");
                idropGui.getTransferManager().enqueueAPut(transferFile.getAbsolutePath(), derivedTargetAbsolutePath, idropGui.getIrodsAccount().getDefaultStorageResource(), idropGui.getIrodsAccount());
            } catch (JargonException ex) {
                log.error("error enqueueing a put operation", ex);
                throw new IdropException("error enqueing put operation", ex);
            }

            log.info("transfer enqueued");
        }

        log.info("drop from desktop processed");

    }
}
