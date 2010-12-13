/*
 * IdropSeriesPanel.java
 *
 * Created on May 27, 2010, 10:45:38 AM
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.idrop.desktop.systraygui.services.IRODSFileService;
import org.irods.jargon.idrop.desktop.systraygui.services.LocalTransferWorker;
import org.irods.jargon.idrop.desktop.systraygui.services.PolicyService;
import org.irods.jargon.idrop.desktop.systraygui.services.RuleExecutionWorker;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.desktop.systraygui.utils.TreeUtils;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.CollectionViewHolder;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.RoundedBorder;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.part.policy.domain.Policy;
import org.slf4j.LoggerFactory;

/**
 * Default center panel visual component.  The center panel is a scroll that shows files/folders
 * on iRODS, and the panel can handle various user actions (click, drop, menu)
 * @author mikeconway
 */
public class IdropDefaultCenterPanel extends IdropAbstractCenterPanel {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IdropDefaultCenterPanel.class);
    private final CollectionViewHolder collectionViewHolder;

    /**
     * Get the object that holds the actual iRODS collection data for the panel
     * @return
     */
    public CollectionViewHolder getCollectionViewHolder() {
        return collectionViewHolder;
    }

    public IdropDefaultCenterPanel(final iDrop iDropParentForm, final CollectionViewHolder collectionViewHolder, final Color backgroundColor) throws IdropException {

        super(iDropParentForm, backgroundColor);
        this.collectionViewHolder = collectionViewHolder;
        initComponents();
        this.setBorder(new RoundedBorder());
        buildPathLabel();
        this.setBackground(backgroundColor);
        pnlTools.setBackground(backgroundColor);
        pnlFileData.setBackground(backgroundColor);
        lblReplicationStatusImage.setIcon(new ImageIcon(IconHelper.getReplicationImage()));
        lblMetadataImage.setIcon(new ImageIcon(IconHelper.getMetadataImage()));
        setUpFolderIcon(backgroundColor);
        if (!iDropParentForm.getIdropConfig().isAdvancedView()) {
            pnlTools.setVisible(false);
        }
    }

    /**
     * build the label depicting the file path
     */
    protected void buildPathLabel() {
        String collectionAbsolutePath = collectionViewHolder.getCollection().getPathOrName();
        StringBuilder sb = new StringBuilder();
        
        sb.append(collectionAbsolutePath.substring(collectionAbsolutePath.lastIndexOf("/")));
        lblSeriesAbsolutePath.setText(sb.toString());
    }

    /**
     * display the metadata dialog
     * @param evt
     */
    protected void displayMetadataViewDialog(MouseEvent evt) {
        MetadataViewDialog metadataViewDialog = new MetadataViewDialog(this.getiDropParentForm(), this.getiDropParentForm().getIrodsAccount(), this.getSeriesAbsolutePath());
        metadataViewDialog.setLocation(evt.getX(), evt.getY());
        metadataViewDialog.setVisible(true);
    }

    /**
     * Set the appropriate icon for the file/collection
     * @param backgroundColor
     */
    protected void setUpFolderIcon(Color backgroundColor) {
        pnlCollectionIcon.setBackground(backgroundColor);
        pnlCollectionIcon.add(IconHelper.getFolderIcon());
    }

    /**
     * Get the absolute path of the file depicted by the panel
     * @return
     */
    private String getSeriesAbsolutePath() {
        return collectionViewHolder.getCollection().getPathOrName();
    }

    /**
     * Get the icon displayed in the panel for the file/folder
     * @return
     */
    protected JPanel getPnlCollectionIcon() {
        return pnlCollectionIcon;
    }

    /**
     * Obtain a reference to the label depicting the path of the file/folder
     * @return
     */
    protected JLabel getLblSeriesAbsolutePath() {
        return lblSeriesAbsolutePath;
    }

    /**
     * Handle the drop action.  A file has been dropped onto the panel
     * @param sourcePaths
     * @param absolutePath
     * @param idropParentForm
     * @throws IdropException
     */
    public void processDropOfFile(final String sourceAbsolutePath, final String absolutePath, final iDrop idropParentForm) throws IdropException {

        int ret = getiDropParentForm().showTransferConfirm(sourceAbsolutePath, absolutePath);

        if (ret == JOptionPane.NO_OPTION) {
            return;
        }
        log.debug("doing put from local to : {}", absolutePath);

        // check if this is a file or a collection.  If a file, then the file name needs to be appended to the iRODS file absolute path
        File sourceFile = new File(sourceAbsolutePath);

        String irodsFileAbsolutePath;
        if (sourceFile.isFile()) {
            log.info("this is a file, add the file name to the irods path");
            StringBuilder newPath = new StringBuilder();
            newPath.append(absolutePath);
            newPath.append('/');
            newPath.append(sourceFile.getName());
            irodsFileAbsolutePath = newPath.toString();
        } else {
            irodsFileAbsolutePath = absolutePath;
        }

        // if a policy is in force for a collection, get the policy to process required metadata

        if (this.getiDropParentForm().getIdropConfig().isPolicyAware()) {
            log.info("policy aware, see if there is a policy in force");
            IRODSFileSystem irodsFileSystem = null;
            try {
                irodsFileSystem = IRODSFileSystem.instance();
                PolicyService policyService = PolicyService.instance(irodsFileSystem.getIRODSAccessObjectFactory(),
                        this.getiDropParentForm().getIrodsAccount());
                Policy boundPolicy = policyService.getPolicyForCollection(absolutePath);
                if (boundPolicy == null) {
                    log.info("no policy bound");
                } else {
                    log.info("policy was bound:{}", boundPolicy);
                    obtainRequiredMetadata(boundPolicy, irodsFileSystem, irodsFileAbsolutePath);

                }
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
                throw new IdropException("error getting policy info for the collection", ex);
            } finally {
                try {
                    irodsFileSystem.close();
                } catch (JargonException ex) {
                    Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


        log.info("transfer to irods file with full path of: {}", irodsFileAbsolutePath);
        LocalTransferWorker localTransferWorker = new LocalTransferWorker(getiDropParentForm().getTransferManager(), sourceAbsolutePath, irodsFileAbsolutePath, getiDropParentForm().getIrodsAccount().getDefaultStorageResource(), getiDropParentForm().getIrodsAccount()); // TODO: right now just accept default resource

        log.info("executing localTransferWorker to process put operation");
        localTransferWorker.execute();
        log.debug("transfer was done");
    }

    /**
     * Handle the drop action.  A file has been dropped onto the panel
     * @param sourcePaths
     * @param absolutePath
     * @param idropParentForm
     * @throws IdropException
     */
    public void processDropOfFile(final List<String> sourcePaths, final String absolutePath, final iDrop idropParentForm) throws IdropException {
        IRODSFileService irodsFileService = new IRODSFileService(idropParentForm.getIrodsAccount());
        String sourceAbsolutePath = irodsFileService.getStringFromSourcePaths(sourcePaths);
        processDropOfFile(sourceAbsolutePath, absolutePath, idropParentForm);
    }

    /**
     * Action hook when the user clicks on the icon in the center panel.  For collection panels,
     * this currently is a drill-down into the file system.
     * @param evt
     */
    protected void processClickOnFolderIcon(java.awt.event.MouseEvent evt) {
        log.info("mouse was clicked for:{}", getSeriesAbsolutePath());
        final TreePath selectedPath;
        try {
            log.debug("building tree path");
            selectedPath = TreeUtils.buildTreePathForIrodsAbsolutePath(this.getiDropParentForm().getTreeStagingResource(), getSeriesAbsolutePath());
        } catch (IdropException ex) {
            Logger.getLogger(IdropDefaultCenterPanel.class.getName()).log(Level.SEVERE, null, ex);
            this.getiDropParentForm().showIdropException(ex);
            return;
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                log.debug("expanding path:{}:", selectedPath);

                // if iDrop is running with the login.preset option, the users home directory is the root of the heirarchy.


                getiDropParentForm().getTreeStagingResource().expandPath(selectedPath);
                getiDropParentForm().getTreeStagingResource().setSelectionPath(selectedPath);
            }
        });
    }

    protected void setUpReplicationDialog() {
        Point panelLocation = this.getLocation();
        ReplicationDialog replicationDialog = new ReplicationDialog(getiDropParentForm(), true,
                getCollectionViewHolder().getCollection().getParentPath());
        replicationDialog.setLocation(new Point((int) panelLocation.getX() + 30, (int) panelLocation.getY()));

        replicationDialog.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupCollection = new javax.swing.JPopupMenu();
        JMenuFixityCheck = new javax.swing.JMenuItem();
        jMenuReplication = new javax.swing.JMenuItem();
        pnlCollectionIcon = new javax.swing.JPanel();
        pnlFileData = new javax.swing.JPanel();
        lblSeriesAbsolutePath = new javax.swing.JLabel();
        pnlTools = new javax.swing.JPanel();
        lblMetadataImage = new javax.swing.JLabel();
        lblReplicationStatusImage = new javax.swing.JLabel();

        JMenuFixityCheck.setMnemonic('f');
        JMenuFixityCheck.setText("Fixity Check");
        JMenuFixityCheck.setToolTipText("Run a fixity check (checksum verification) on the collection");
        JMenuFixityCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JMenuFixityCheckActionPerformed(evt);
            }
        });
        jPopupCollection.add(JMenuFixityCheck);

        jMenuReplication.setMnemonic('r');
        jMenuReplication.setText("Replication");
        jMenuReplication.setToolTipText("Replicate this collection across multiple resources");
        jMenuReplication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuReplicationActionPerformed(evt);
            }
        });
        jPopupCollection.add(jMenuReplication);

        setMaximumSize(new java.awt.Dimension(9999, 70));
        setMinimumSize(new java.awt.Dimension(65, 10));
        setSize(new java.awt.Dimension(65, 30));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        pnlCollectionIcon.setMaximumSize(null);
        pnlCollectionIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlCollectionIconMouseClicked(evt);
            }
        });
        add(pnlCollectionIcon, java.awt.BorderLayout.WEST);

        pnlFileData.setMaximumSize(null);
        pnlFileData.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblSeriesAbsolutePath.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSeriesAbsolutePath.setText("jLabel1");
        lblSeriesAbsolutePath.setName("lblSeriesAbsolutePath"); // NOI18N
        pnlFileData.add(lblSeriesAbsolutePath);

        add(pnlFileData, java.awt.BorderLayout.CENTER);

        pnlTools.setMaximumSize(null);
        pnlTools.setMinimumSize(new java.awt.Dimension(40, 20));
        pnlTools.setPreferredSize(new java.awt.Dimension(40, 20));
        pnlTools.setSize(new java.awt.Dimension(40, 20));

        lblMetadataImage.setToolTipText("Display metadata panel");
        lblMetadataImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMetadataImageMouseClicked(evt);
            }
        });
        pnlTools.add(lblMetadataImage);

        lblReplicationStatusImage.setToolTipText("View replication status and replicate data");
        lblReplicationStatusImage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblReplicationStatusImageMouseClicked(evt);
            }
        });
        pnlTools.add(lblReplicationStatusImage);

        add(pnlTools, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // if(evt.isPopupTrigger()) {
        if (this.getiDropParentForm().getIdropConfig().isAdvancedView()) {
            jPopupCollection.show(evt.getComponent(), evt.getX(), evt.getY());
        }
        //}
    }//GEN-LAST:event_formMouseClicked

    private void JMenuFixityCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JMenuFixityCheckActionPerformed
        int n = JOptionPane.showConfirmDialog(
                this,
                "Apply policy for:" + getSeriesAbsolutePath(),
                "ApplyPolicy Confirmaiton",
                JOptionPane.YES_NO_OPTION);

        if (n != JOptionPane.YES_OPTION) {
            log.info("cancelled rule action");
            return;
        }

        RuleExecutionWorker ruleExecutionWorker;
        try {
            ruleExecutionWorker = new RuleExecutionWorker(getiDropParentForm(), this.getSeriesAbsolutePath(), getiDropParentForm().getIrodsAccount().getDefaultStorageResource(), getiDropParentForm().getIrodsAccount()); // TODO: right now just accept default resource
        } catch (IdropException ex) {
            Logger.getLogger(IdropDefaultCenterPanel.class.getName()).log(Level.SEVERE, null, ex);
            getiDropParentForm().showIdropException(ex);
            return;
        }

        log.info("executing localTransferWorker to process put operation");
        ruleExecutionWorker.execute();
        log.debug("transfer was done");

    }//GEN-LAST:event_JMenuFixityCheckActionPerformed

    private void jMenuReplicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuReplicationActionPerformed
        setUpReplicationDialog();
    }//GEN-LAST:event_jMenuReplicationActionPerformed

    /**
     * The folder icon was clicked in this panel.  Find the series associated with this panel and expand the
     * tree node associated with this node.
     * @param evt
     */
    private void pnlCollectionIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCollectionIconMouseClicked
        processClickOnFolderIcon(evt);
    }//GEN-LAST:event_pnlCollectionIconMouseClicked

    private void lblMetadataImageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMetadataImageMouseClicked
        displayMetadataViewDialog(evt);
    }//GEN-LAST:event_lblMetadataImageMouseClicked

    private void lblReplicationStatusImageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblReplicationStatusImageMouseClicked
        setUpReplicationDialog();
}//GEN-LAST:event_lblReplicationStatusImageMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem JMenuFixityCheck;
    private javax.swing.JMenuItem jMenuReplication;
    private javax.swing.JPopupMenu jPopupCollection;
    private javax.swing.JLabel lblMetadataImage;
    private javax.swing.JLabel lblReplicationStatusImage;
    private javax.swing.JLabel lblSeriesAbsolutePath;
    private javax.swing.JPanel pnlCollectionIcon;
    private javax.swing.JPanel pnlFileData;
    private javax.swing.JPanel pnlTools;
    // End of variables declaration//GEN-END:variables

    private void obtainRequiredMetadata(Policy boundPolicy, IRODSFileSystem irodsFileSystem, String irodsFileAbsolutePath) throws IdropException {
        Point panelLocation = this.getLocation();
        RequiredMetadataDialog requiredMetadataDialog = new RequiredMetadataDialog(this.getiDropParentForm(), irodsFileAbsolutePath, boundPolicy, true);
        requiredMetadataDialog.setLocation(new Point((int) panelLocation.getX() + 30, (int) panelLocation.getY()));
        requiredMetadataDialog.setVisible(true);

    }
}
