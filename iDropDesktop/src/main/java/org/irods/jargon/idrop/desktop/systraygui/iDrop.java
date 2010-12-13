
/*
 * IDrop.java
 *
 * Created on May 20, 2010, 2:59:48 PM
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.event.ItemEvent;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import org.irods.jargon.core.transfer.TransferStatus;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.idrop.desktop.systraygui.services.IconManager;
import org.irods.jargon.idrop.desktop.systraygui.services.QueueSchedulerTimerTask;
import org.irods.jargon.idrop.desktop.systraygui.utils.ColorHelper;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.desktop.systraygui.utils.IdropConfig;
import org.irods.jargon.idrop.desktop.systraygui.utils.LocalFileUtils;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.DefaultFileRepresentationPanel;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.IRODSFileSystemModel;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.IRODSNode;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.LocalFileNode;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.LocalFileSystemModel;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.LocalFileTree;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.IRODSTree;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.InfoPanelTransferHandler;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.IrodsTreeListenerForBuildingInfoPanel;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.TagCloudListModel;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.irods.jargon.transferengine.TransferManager;
import org.irods.jargon.transferengine.TransferManager.ErrorStatus;
import org.irods.jargon.transferengine.TransferManager.RunningStatus;
import org.irods.jargon.transferengine.TransferManagerCallbackListener;
import org.irods.jargon.usertagging.FreeTaggingService;
import org.irods.jargon.usertagging.FreeTaggingServiceImpl;
import org.irods.jargon.usertagging.UserTagCloudService;
import org.irods.jargon.usertagging.UserTagCloudServiceImpl;
import org.irods.jargon.usertagging.domain.IRODSTagGrouping;
import org.irods.jargon.usertagging.domain.TagCloudEntry;
import org.irods.jargon.usertagging.domain.TagQuerySearchResult;
import org.irods.jargon.usertagging.domain.UserTagCloudView;
import org.slf4j.LoggerFactory;

/**
 * Main system tray and GUI.  Create system tray menu, start timer process for queue.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class iDrop extends javax.swing.JFrame implements ActionListener, ItemListener, TransferManagerCallbackListener {

    private LocalFileSystemModel localFileModel = null;
    public static org.slf4j.Logger log = LoggerFactory.getLogger(iDrop.class);
    private boolean formShown = false;
    private LocalFileTree fileTree = null;
    private IRODSTree irodsTree = null;
    private TransferManager transferManager = null;
    private QueueManagerDialog queueManagerDialog = null;
    private Timer queueTimer = null;
    private QueueSchedulerTimerTask queueTimerTask = null;
    private IdropConfig idropConfig = null;
    private IconManager iconManager = null;
    private IRODSAccount irodsAccount = null;
    private CheckboxMenuItem pausedItem = null;
    private TrayIcon trayIcon = null;
    private IRODSFileSystem irodsFileSystem = null;
    private Object lastCachedInfoItem = null;
    public DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
    private UserTagCloudView userTagCloudView = null;
    private PreferencesDialog preferencesDialog = null;

    /**
     * Get the IRODSFileSystem that will be the source for all connections and references to access object and file factories.
     * NOTE: there is some legacy code that needs to be converted to use this reference.
     * @return
     */
    public IRODSFileSystem getIrodsFileSystem() {
        return irodsFileSystem;
    }

    /**
     * Get configuration information for iDrop
     * @return <code>IdropConfig</code> containing relevant configuration
     */
    public IdropConfig getIdropConfig() {
        return idropConfig;
    }

    /**
     * Callback from transferManager that indicates that an error has occurred.
     * @param es
     */
    @Override
    public void transferManagerErrorStatusUpdate(ErrorStatus es) {
        iconManager.setErrorStatus(es);
    }

    /**
     * Callback from transferManager that the status of running transfers has occurred
     * @param rs
     */
    @Override
    public void transferManagerRunningStatusUpdate(RunningStatus rs) {
        iconManager.setRunningStatus(rs);
    }

    /** Creates new form IDrop */
    public iDrop() {

        try {
            // load the properties
            this.idropConfig = IdropConfig.instance();
            idropConfig.setUpLogging();
        } catch (IdropException ex) {
            Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("unable to load iDrop configuration", ex);
        }

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }


        initComponents();
        this.pnlLocalTreeArea.setVisible(false);
        this.pnlIrodsInfo.setVisible(false);
        this.splitTargetCollections.setResizeWeight(0.8d);
        try {
            pnlIrodsInfo.setTransferHandler(new InfoPanelTransferHandler(this));
        } catch (IdropException ex) {
            Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException("error setting up infoPanelTransferHandler", ex);
        }

        if (!idropConfig.isAdvancedView()) {
            toolBarInfo.setVisible(false);
        }

        Toolkit t = Toolkit.getDefaultToolkit();
        int width = t.getScreenSize().width;
        int height = t.getScreenSize().height;

        int showX = width / 2;
        int showY = height / 2;

        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setLocation(showY, showY);
        loginDialog.setVisible(true);

        if (getIrodsAccount() == null) {
            log.warn("no account, exiting");
            System.exit(0);
        }

        /* the transfer manager is the central control for the data transfer queue, as well
         * as the maintainer of the status of the queue.  This app listens to the TransferManager to receive updates
         * about what the queue is doing.
         */

        iconManager = IconManager.instance(this);

        try {
            irodsFileSystem = IRODSFileSystem.instance();
            transferManager = TransferManager.instanceWithCallbackListenerAndUserLevelDatabase(this, idropConfig.isLogSuccessfulTransfers(), idropConfig.getTransferDatabaseName());

            // see if the queue has any pending transfers

            if (transferManager.getCurrentQueue().size() > 0) {
                int confirm = showTransferStartupConfirm();
                if (confirm == JOptionPane.NO_OPTION) {
                    transferManager.pause();
                }
            }

        } catch (JargonException ex) {
            Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        /* A timer task monitors the queue, and can be extended to process things like retrys and file synchronization */
        log.info("creating timer for queue manager");
        queueTimer = new Timer();
        try {
            queueTimerTask = QueueSchedulerTimerTask.instance(transferManager, this);
            queueTimer.scheduleAtFixedRate(queueTimerTask, 1000, QueueSchedulerTimerTask.EVERY_10_MINUTES);
        } catch (IdropException ex) {
            Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

    }

    /**
     * Display an error message dialog that indicates an exception has occcurred
     * @param idropException
     */
    public void showIdropException(Exception idropException) {
        JOptionPane.showMessageDialog(this, idropException.getMessage(), "iDROP Exception", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Utility method to display a dialog with a message.
     * @param messageFromOperation
     */
    public void showMessageFromOperation(final String messageFromOperation) {

        final iDrop thisIdropGui = this;
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(thisIdropGui, messageFromOperation, "iDROP Message", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    /**
     * Start up iDrop as a system tray application.  This is the main entry point for iDrop
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                //new IDrop().setVisible(true);
                final iDrop iDropGui = new iDrop();
                iDropGui.createAndShowSystemTray();
                iDropGui.processQueueStartup();
            }
        });
    }

    /**
     * Update the system tray icon based on the current status.
     * @param iconFile
     */
    public void updateIcon(final String iconFile) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                Image newIcon = createImage(iconFile, "icon");
                trayIcon.setImage(newIcon);
            }
        });
    }

    /**
     * Builds the system tray menu and installs the iDrop icon in the system tray.
     * The iDrop GUI is displayed when the iDrop menu item is selected from the system tray
     */
    protected void createAndShowSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();

        trayIcon =
                new TrayIcon(createImage("images/dialog-ok-2.png", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();

        // iconManager = IconManager.instance(this);

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem iDropItem = new MenuItem("iDrop");
        MenuItem preferencesItem = new MenuItem("Preferences");

        iDropItem.addActionListener(this);

        Menu displayMenu = new Menu("Display");
        MenuItem currentItem = new MenuItem("Current");
        MenuItem recentItem = new MenuItem("Recent");
        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem logoutItem = new MenuItem("Logout");

        pausedItem = new CheckboxMenuItem("Pause");

        MenuItem exitItem = new MenuItem("Exit");

        exitItem.addActionListener(this);
        recentItem.addActionListener(this);
        currentItem.addActionListener(this);
        errorItem.addActionListener(this);
        warningItem.addActionListener(this);
        preferencesItem.addActionListener(this);

        logoutItem.addActionListener(this);
        pausedItem.addItemListener(this);
        aboutItem.addActionListener(this);

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.add(iDropItem);
        popup.add(preferencesItem);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(currentItem);
        displayMenu.add(recentItem);
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        popup.addSeparator();
        popup.add(pausedItem);
        popup.addSeparator();
        popup.add(logoutItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. FIXME: move to static util */
    protected static Image createImage(String path, String description) {
        URL imageURL = iDrop.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    /**
     * Get the current iRODS login account.
     * @return <code>IRODSAccount</code> with the current iRODS connection information.
     */
    public IRODSAccount getIrodsAccount() {
        synchronized (this) {
            return irodsAccount;
        }
    }

    /**
     * Set the current connection information.
     * @return <code>IRODSAccount</code> with the current iRODS connection information.
     */
    public void setIrodsAccount(IRODSAccount irodsAccount) {
        synchronized (this) {
            this.irodsAccount = irodsAccount;
        }
    }

    /**
     * Handler for iDrop system tray menu options.
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("Exit")) {
            queueTimer.cancel();
            System.exit(0);
        } else if (e.getActionCommand().equals("Logout")) {
            this.setIrodsAccount(null);
            this.signalChangeInAccountSoCachedDataCanBeCleared();
            LoginDialog loginDialog = new LoginDialog(this);
            loginDialog.setVisible(true);

            if (getIrodsAccount() == null) {
                log.warn("no account, exiting");
                System.exit(0);
            } else {
                this.setVisible(false);
            }

        } else if (e.getActionCommand().equals("About")) {
            AboutDialog aboutDialog = new AboutDialog(this, true);
            aboutDialog.setLocation((int) (this.getLocation().getX() + this.getWidth() / 2), (int) (this.getLocation().getY() + this.getHeight() / 2));

            aboutDialog.setVisible(true);
        } else if (e.getActionCommand().equals("Preferences")) {
            showPreferencesDialog();
        } else if (e.getActionCommand().equals("Recent")) {

            log.info("showing recent items in queue");
            try {
                if (queueManagerDialog == null) {
                    queueManagerDialog = new QueueManagerDialog(this, transferManager, QueueManagerDialog.ViewType.RECENT);
                } else {
                    queueManagerDialog.refreshTableView(QueueManagerDialog.ViewType.RECENT);
                }
            } catch (IdropException ex) {
                Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                this.showIdropException(ex);
                return;
            }

            queueManagerDialog.setModal(false);
            queueManagerDialog.setVisible(true);
            queueManagerDialog.toFront();

        } else if (e.getActionCommand().equals("Current")) {

            log.info("showing current items in queue");
            try {
                if (queueManagerDialog == null) {
                    queueManagerDialog = new QueueManagerDialog(this, transferManager, QueueManagerDialog.ViewType.CURRENT);
                } else {
                    queueManagerDialog.refreshTableView(QueueManagerDialog.ViewType.CURRENT);
                }
            } catch (IdropException ex) {
                Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                this.showIdropException(ex);
                return;
            }
            queueManagerDialog.setModal(false);
            queueManagerDialog.setVisible(true);
            queueManagerDialog.toFront();

        } else if (e.getActionCommand().equals("Error")) {

            log.info("showing error items in queue");
            try {
                if (queueManagerDialog == null) {
                    queueManagerDialog = new QueueManagerDialog(this, transferManager, QueueManagerDialog.ViewType.ERROR);
                } else {
                    queueManagerDialog.refreshTableView(QueueManagerDialog.ViewType.ERROR);
                }
            } catch (IdropException ex) {
                Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                this.showIdropException(ex);
                return;
            }
            queueManagerDialog.setModal(false);
            queueManagerDialog.setVisible(true);
            queueManagerDialog.toFront();

        } else if (e.getActionCommand().equals("Warning")) {

            log.info("showing warning items in queue");
            try {
                if (queueManagerDialog == null) {
                    queueManagerDialog = new QueueManagerDialog(this, transferManager, QueueManagerDialog.ViewType.WARNING);
                } else {
                    queueManagerDialog.refreshTableView(QueueManagerDialog.ViewType.WARNING);
                }
            } catch (IdropException ex) {
                Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                this.showIdropException(ex);
                return;
            }
            queueManagerDialog.setModal(false);
            queueManagerDialog.setVisible(true);
            queueManagerDialog.toFront();

        } else {

            if (!this.formShown) {

                Toolkit t = Toolkit.getDefaultToolkit();
                int width = t.getScreenSize().width;
                int height = t.getScreenSize().height;

                int showX = (width / 2) - (this.getWidth() / 2);
                int showY = (height / 2) - (this.getHeight() / 2);
                this.setUpLocalFileSelectTree();
                this.buildTargetTree();
                this.formShown = true;
                this.setLocation(showX, showY);
                this.setVisible(true);

            } else {
                // refresh the tree when setting visible again, the account may have changed.

                buildTargetTree();
                this.setVisible(true);
            }

            this.toFront();
        }

    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    /**
     * A transfer confirm dialog
     * @param sourcePath <code>String</code> with the source path of the transfer
     * @param targetPath <code>String</code> with the target of the transfer
     * @return <code>int</code> with the dialog user response.
     */
    public int showTransferConfirm(final String sourcePath, final String targetPath) {

        StringBuilder sb = new StringBuilder();
        sb.append("Would you like to transfer from ");
        sb.append(sourcePath);
        sb.append(" to ");
        sb.append(targetPath);

        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                sb.toString(),
                "Transfer Confirmaiton",
                JOptionPane.YES_NO_OPTION);

        return n;
    }

    /**
     * A dialog to indicate that the queue should start processing
     */
    public int showTransferStartupConfirm() {


        //default icon, custom title
        int n = JOptionPane.showConfirmDialog(
                this,
                "There are transfers ready to process, should the transfer queue be started?  Click NO to pause the transfersf",
                "Begin Transfer Confirmation",
                JOptionPane.YES_NO_OPTION);

        return n;
    }

    /**
     * Returns the current iRODS remote tree view component.
     * @return <code>JTree</code> visual representation of the remote iRODS resource
     */
    public javax.swing.JTree getTreeStagingResource() {
        return irodsTree;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        if (e.getItem().equals("Pause")) {

            try {
                if (pausedItem.getState() == true) {
                    log.info("pausing....");
                    transferManager.pause();
                } else {
                    log.info("resuming queue");
                    transferManager.resume();
                }
            } catch (Exception ex) {
                Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * Get the transferManager that controls the transfer queue
     * @return <code>TransferManager</code> that controls all transfers via the transfer queue
     */
    public TransferManager getTransferManager() {
        return transferManager;
    }

    /**
     * Show or hide the iRODS info panel and manage the state of the show info menu and toggle so that they remain in synch
     */
    private void handleInfoPanelShowOrHide() {
        final iDrop idropGuiReference = this;
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                pnlIrodsInfo.setVisible(toggleIrodsDetails.isSelected());
                jCheckBoxMenuItemShowIrodsInfo.setSelected(toggleIrodsDetails.isSelected());
                // if info is being opened, initialize to the first selected item, or the root of the iRODS tree if none selected
                IRODSNode node;
                if (!(irodsTree.getLastSelectedPathComponent() instanceof IRODSNode)) {
                    log.info("last selected is not a Node, using root node");
                    node = (IRODSNode) irodsTree.getModel().getRoot();
                } else {
                    log.info("initializing with last selected node");
                    node = (IRODSNode) irodsTree.getLastSelectedPathComponent();
                }
                try {
                    IrodsTreeListenerForBuildingInfoPanel treeBuilder = new IrodsTreeListenerForBuildingInfoPanel(idropGuiReference);
                    treeBuilder.identifyNodeTypeAndInitializeInfoPanel(node);
                } catch (Exception ex) {
                    Logger.getLogger(IrodsTreeListenerForBuildingInfoPanel.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException("exception processing valueChanged() event for IRODSNode selection");
                }
                if (pnlIrodsInfo.isVisible()) {
                    splitTargetCollections.setDividerLocation(0.5d);
                }
            }
        });
    }

    /**
     * Set up a JTree that depicts the local file system
     */
    private void setUpLocalFileSelectTree() {


        /* build a list of the roots (e.g. drives on windows systems).  If there is only one, use it
         * as the basis for the file model, otherwise, display an additional panel listing
         * the other roots, and build the tree for the first drive
         * encountered.
         */

        if (fileTree != null) {
            log.info("file tree already initialized");
            return;
        }

        log.info("building tree to look at local file system");
        final iDrop gui = this;


        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                initializeLocalFileTreeModel(null);
                fileTree = new LocalFileTree(localFileModel, gui);
                fileTree.setDragEnabled(true);
                fileTree.setDropMode(javax.swing.DropMode.ON);
                fileTree.setTransferHandler(new TransferHandler("selectionModel"));
                fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
                listLocalDrives.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }

                        log.debug("new local file system model");
                        log.debug("selection event:{}", e);
                        Object selectedItem = listLocalDrives.getSelectedValue();
                        initializeLocalFileTreeModelWhenDriveIsSelected(selectedItem);


                    }
                });
                scrollLocalFileTree.setViewportView(fileTree);
                pnlLocalTreeArea.add(scrollLocalFileTree, java.awt.BorderLayout.CENTER);
            }
        });

    }

    private void initializeLocalFileTreeModelWhenDriveIsSelected(final Object selectedDrive) {
        if (selectedDrive == null) {
            log.debug("selected drive is null, use the first one");
            listLocalDrives.setSelectedIndex(0);

            localFileModel = new LocalFileSystemModel(new LocalFileNode(new File((String) listLocalDrives.getSelectedValue())));
            fileTree.setModel(localFileModel);
        } else {
            log.debug("selected drive is not null, create new root based on selection", selectedDrive);
            listLocalDrives.setSelectedValue(selectedDrive, true);
            localFileModel = new LocalFileSystemModel(new LocalFileNode(new File((String) selectedDrive)));
            fileTree.setModel(localFileModel);

        }

        scrollLocalDrives.setVisible(true);
    }

    private void initializeLocalFileTreeModel(final Object selectedDrive) {
        List<String> roots = LocalFileUtils.listFileRootsForSystem();

        if (roots.isEmpty()) {
            IdropException ie = new IdropException("unable to find any roots on the local file system");
            log.error("error building roots on local file system", ie);
            showIdropException(ie);
            return;
        } else if (roots.size() == 1) {
            scrollLocalDrives.setVisible(false);
            localFileModel = new LocalFileSystemModel(new LocalFileNode(new File(roots.get(0))));

        } else {
            DefaultListModel listModel = new DefaultListModel();
            for (String root : roots) {
                listModel.addElement(root);
            }

            listLocalDrives.setModel(listModel);

            scrollLocalDrives.setVisible(true);
        }
    }

    /**
     * build the JTree that will depict the iRODS resource
     */
    public void buildTargetTree() {
        log.info("building tree to look at staging resource");
        final iDrop gui = this;

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.debug("refreshing series panel");
                Enumeration<TreePath> currentPaths = null;
                TreePath rootPath = null;

                if (getTreeStagingResource() != null) {
                    rootPath = getTreeStagingResource().getPathForRow(0);
                    currentPaths = getTreeStagingResource().getExpandedDescendants(rootPath);
                    log.debug("selected tree node, paths are:{}", currentPaths);
                }

                CollectionAndDataObjectListingEntry root = new CollectionAndDataObjectListingEntry();

                if (idropConfig.isLoginPreset()) {
                    log.info("using policy preset home directory");
                    StringBuilder sb = new StringBuilder();
                    sb.append("/");
                    sb.append(getIrodsAccount().getZone());
                    sb.append("/");
                    sb.append("home");
                    root.setParentPath(sb.toString());
                    root.setPathOrName(getIrodsAccount().getHomeDirectory());
                } else {
                    log.info("using root path, no login preset");
                    root.setPathOrName("/");
                }


                //if (irodsTree == null) {
                log.info("building new iRODS tree");
                try {
                    irodsTree = new IRODSTree(gui);
                    IRODSNode rootNode = new IRODSNode(root, getIrodsAccount(), getIrodsFileSystem(), irodsTree);
                    irodsTree.setModel(new IRODSFileSystemModel(rootNode, getIrodsAccount()));
                    irodsTree.setRefreshingTree(true);
                    irodsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
                    IrodsTreeListenerForBuildingInfoPanel treeListener = new IrodsTreeListenerForBuildingInfoPanel(gui);
                    irodsTree.addTreeExpansionListener(treeListener);
                    irodsTree.addTreeSelectionListener(treeListener);
                    // preset to display root tree node
                    irodsTree.setSelectionRow(0);
                } catch (Exception ex) {
                    Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                }
                /* } else {
                log.info("refreshing iRODS tree");
                try {
                irodsTree.setRefreshingTree(true);
                IRODSNode rootNode = new IRODSNode(root, getIrodsAccount(), getIrodsFileSystem(), irodsTree);
                irodsTree.setModel(new IRODSFileSystemModel(rootNode, getIrodsAccount()));
                } catch (IdropException ex) {
                Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                throw new IdropRuntimeException(ex);
                }
                }

                 * */

                scrollIrodsTree.setViewportView(getTreeStagingResource());

                TreePath currentPath;

                if (currentPaths != null) {
                    while (currentPaths.hasMoreElements()) {
                        currentPath = (TreePath) currentPaths.nextElement();
                        log.debug("expanding tree path:{}", currentPath);
                        irodsTree.expandPath(currentPath);
                    }
                }
                irodsTree.setRefreshingTree(false);
                irodsFileSystem.closeAndEatExceptions(irodsAccount);

                /*
                irodsTree.validate();
                irodsTree.repaint();
                //irodsTree.repaint();
                //irodsTree.repaint();
                pnlTargetTree.validate();
                pnlTargetTree.repaint();
                scrollIrodsTree.validate();
                scrollIrodsTree.repaint();
                pnlIrodsArea.validate();
                pnlIrodsArea.repaint();
                pnlTabHierarchicalView.validate();
                pnlTabHierarchicalView.repaint();
                gui.validate();
                gui.repaint();
                 * */

            }
        });
    }

    /**
     * This is the callback method that will be called by the transfer engine to give
     * real-time status.  Currently these go in the bit bucket but will be used
     * for progress bars and other things
     * @param <code>TransferStatus</code> with the status of the file transfer as reported in real-time
     * by the transfer engine process.
     */
    @Override
    public void transferStatusCallback(TransferStatus ts) {
        log.info("transfer status callback to iDROP:{}", ts);
    }

    private void processQueueStartup() {
        if (transferManager.getRunningStatus() == TransferManager.RunningStatus.PAUSED) {
            pausedItem.setState(true);
        }
    }

    public void initializeInfoPane(final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) throws IdropException {
        if (!toggleIrodsDetails.isSelected()) {
            log.info("info display not selected, don't bother");
            return;
        }

        if (collectionAndDataObjectListingEntry == null) {
            throw new IdropException("null collectionAndDataObjectListingEntry");
        }

        final iDrop idropGui = this;

        // need to get the collection or data object info from iRODS

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                try {
                    if (collectionAndDataObjectListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
                        log.info("looking up collection to build info panel");
                        CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(getIrodsAccount());
                        Collection collection = collectionAO.findByAbsolutePath(collectionAndDataObjectListingEntry.getPathOrName());
                        initializeInfoPanel(collection);
                    } else {
                        log.info("looking up data object to build info panel");
                        DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(getIrodsAccount());
                        DataObject dataObject = dataObjectAO.findByAbsolutePath(collectionAndDataObjectListingEntry.getParentPath() + "/"
                                + collectionAndDataObjectListingEntry.getPathOrName());
                        initializeInfoPanel(dataObject);
                    }

                } catch (Exception e) {
                    log.error("exception building info panel from collection and data object listing entry:{}", collectionAndDataObjectListingEntry, e);
                    throw new IdropRuntimeException(e);
                } finally {
                    irodsFileSystem.closeAndEatExceptions(getIrodsAccount());
                    idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                }
            }
        });
    }

    /**
     * Initialize the info panel with data from iRODS.  In this case, the data is an iRODS data object (file)
     * @param dataObject <code>DataObject</code> iRODS domain object for a file.
     * @throws IdropException
     */
    public void initializeInfoPanel(final DataObject dataObject) throws IdropException {

        if (!toggleIrodsDetails.isSelected()) {
            log.info("info display not selected, don't bother");
            return;
        }

        if (dataObject == null) {
            throw new IdropException("Null dataObject");
        }

        this.lastCachedInfoItem = dataObject;
        final iDrop idropGui = this;

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                lblFileOrCollectionName.setText(dataObject.getDataName());
                txtParentPath.setText(dataObject.getCollectionName());
                txtComment.setText(dataObject.getComments());

                log.debug("getting available tags for data object");

                try {
                    FreeTaggingService freeTaggingService = FreeTaggingServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), getIrodsAccount());
                    IRODSTagGrouping irodsTagGrouping = freeTaggingService.getTagsForDataObjectInFreeTagForm(dataObject.getCollectionName() + "/" + dataObject.getDataName());
                    txtTags.setText(irodsTagGrouping.getSpaceDelimitedTagsForDomain());
                    pnlInfoIcon.removeAll();
                    pnlInfoIcon.add(IconHelper.getFileIcon());
                    pnlInfoIcon.validate();
                    lblInfoCreatedAtValue.setText(df.format(dataObject.getCreatedAt()));
                    lblInfoUpdatedAtValue.setText(df.format(dataObject.getUpdatedAt()));
                    lblInfoLengthValue.setText(String.valueOf(dataObject.getDataSize()));
                    lblInfoLengthValue.setVisible(true);
                    lblInfoLength.setVisible(true);
                } catch (JargonException ex) {
                    Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                } finally {
                    irodsFileSystem.closeAndEatExceptions(getIrodsAccount());
                    idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

    }

    /**
     * Initialize the info panel with data from iRODS.  In this case, the data is an iRODS collection (directory).
     * @param collection
     * @throws IdropException
     */
    public void initializeInfoPanel(final Collection collection) throws IdropException {
        if (collection == null) {
            throw new IdropException("Null collection");
        }

        log.info("initialize info panel with collection:{}", collection);

        if (!toggleIrodsDetails.isSelected()) {
            log.info("info display not selected, don't bother");
            return;
        }

        this.lastCachedInfoItem = collection;
        final iDrop idropGui = this;

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                lblFileOrCollectionName.setText(collection.getCollectionLastPathComponent());
                txtParentPath.setText(collection.getCollectionParentName());
                txtComment.setText(collection.getComments());

                log.debug("getting available tags for data object");

                try {
                    FreeTaggingService freeTaggingService = FreeTaggingServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), getIrodsAccount());
                    IRODSTagGrouping irodsTagGrouping = freeTaggingService.getTagsForCollectionInFreeTagForm(collection.getCollectionName());
                    txtTags.setText(irodsTagGrouping.getSpaceDelimitedTagsForDomain());
                    pnlInfoIcon.removeAll();
                    pnlInfoIcon.add(IconHelper.getFolderIcon());
                    pnlInfoIcon.validate();
                    lblInfoCreatedAtValue.setText(df.format(collection.getCreatedAt()));
                    lblInfoUpdatedAtValue.setText(df.format(collection.getModifiedAt()));
                    lblInfoLengthValue.setVisible(false);
                    lblInfoLength.setVisible(false);
                } catch (JargonException ex) {
                    Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                } finally {
                    irodsFileSystem.closeAndEatExceptions(getIrodsAccount());
                    idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    /**
     * Get the JTree component that represents the iRODS file system in the iDrop gui.
     * @return <code>IRODSTree</code> that is the JTree component for the iRODS file system view.
     */
    public IRODSTree getIrodsTree() {
        return irodsTree;
    }

    public JToggleButton getToggleIrodsDetails() {
        return toggleIrodsDetails;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     *
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        seriesDisplayPanel = new javax.swing.JPanel();
        labelSeriesName = new javax.swing.JLabel();
        labelSeriesPath = new javax.swing.JLabel();
        pnlCollectionsInTargetCollection = new javax.swing.JPanel();
        iDropToolbar = new javax.swing.JPanel();
        pnlToolbarSizer = new javax.swing.JPanel();
        pnlTopToolbarSearchArea = new javax.swing.JPanel();
        pnlSearchSizer = new javax.swing.JPanel();
        lblMainSearch = new javax.swing.JLabel();
        txtMainSearch = new javax.swing.JTextField();
        pnlLocalToggleSizer = new javax.swing.JPanel();
        toggleLocalFiles = new javax.swing.JToggleButton();
        pnlIrodsDetailsToggleSizer = new javax.swing.JPanel();
        toggleIrodsDetails = new javax.swing.JToggleButton();
        pnlIdropMain = new javax.swing.JPanel();
        jSplitPanelLocalRemote = new javax.swing.JSplitPane();
        pnlLocalTreeArea = new javax.swing.JPanel();
        pnlLocalRoots = new javax.swing.JPanel();
        scrollLocalDrives = new javax.swing.JScrollPane();
        listLocalDrives = new javax.swing.JList();
        pnlRefreshButton = new javax.swing.JPanel();
        btnRefreshLocalDrives = new javax.swing.JButton();
        pnlDrivesFiller = new javax.swing.JPanel();
        scrollLocalFileTree = new javax.swing.JScrollPane();
        pnlIrodsArea = new javax.swing.JPanel();
        splitTargetCollections = new javax.swing.JSplitPane();
        tabIrodsViews = new javax.swing.JTabbedPane();
        pnlTabHierarchicalView = new javax.swing.JPanel();
        pnlIrodsTreeToolbar = new javax.swing.JPanel();
        btnRefreshTargetTree = new javax.swing.JButton();
        pnlIrodsTreeMaster = new javax.swing.JPanel();
        scrollIrodsTree = new javax.swing.JScrollPane();
        pnlTargetTree = new javax.swing.JPanel();
        pnlTabTagView = new javax.swing.JPanel();
        pnlTagSearch = new javax.swing.JPanel();
        btnRefreshTagCloud = new javax.swing.JButton();
        pnlTagViewMaster = new javax.swing.JPanel();
        splitTagsAndTagResults = new javax.swing.JSplitPane();
        pnlTagListing = new javax.swing.JPanel();
        scrollPaneTagCloudList = new javax.swing.JScrollPane();
        listTagCloudList = new javax.swing.JList();
        scrollTagResults = new javax.swing.JScrollPane();
        pnlTagResultsInner = new javax.swing.JPanel();
        pnlIrodsInfo = new javax.swing.JPanel();
        scrollIrodsInfo = new javax.swing.JScrollPane();
        pnlIrodsInfoInner = new javax.swing.JPanel();
        pnlFileIconSizer = new javax.swing.JPanel();
        pnlInfoIcon = new javax.swing.JPanel();
        pnlFileNameAndIcon = new javax.swing.JPanel();
        lblFileOrCollectionName = new javax.swing.JLabel();
        pnlInfoCollectionParent = new javax.swing.JPanel();
        lblFileParent = new javax.swing.JLabel();
        pnlScrollParentPathSizer = new javax.swing.JPanel();
        scrollParentPath = new javax.swing.JScrollPane();
        txtParentPath = new javax.swing.JTextArea();
        pnlInfoComment = new javax.swing.JPanel();
        lblComment = new javax.swing.JLabel();
        pnlInfoCommentScrollSizer = new javax.swing.JPanel();
        scrollComment = new javax.swing.JScrollPane();
        txtComment = new javax.swing.JTextArea();
        pnlInfoTags = new javax.swing.JPanel();
        lblTags = new javax.swing.JLabel();
        pnlInfoTagsSizer = new javax.swing.JPanel();
        txtTags = new javax.swing.JTextField();
        pnlInfoButton = new javax.swing.JPanel();
        pnlInfoButtonSizer = new javax.swing.JPanel();
        btnUpdateInfo = new javax.swing.JButton();
        pnlInfoDetails = new javax.swing.JPanel();
        lblInfoCreatedAt = new javax.swing.JLabel();
        lblInfoCreatedAtValue = new javax.swing.JLabel();
        lblInfoUpdatedAt = new javax.swing.JLabel();
        lblInfoUpdatedAtValue = new javax.swing.JLabel();
        lblInfoLength = new javax.swing.JLabel();
        lblInfoLengthValue = new javax.swing.JLabel();
        pnlToolbarInfo = new javax.swing.JPanel();
        toolBarInfo = new javax.swing.JToolBar();
        btnViewMetadata = new javax.swing.JButton();
        btnReplication = new javax.swing.JButton();
        separator1 = new javax.swing.JToolBar.Separator();
        btnMoveToTrash = new javax.swing.JButton();
        separator2 = new javax.swing.JToolBar.Separator();
        pnlIdropBottom = new javax.swing.JPanel();
        lblIdropMessage = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuView = new javax.swing.JMenu();
        jCheckBoxMenuItemShowSourceTree = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowIrodsInfo = new javax.swing.JCheckBoxMenuItem();
        jMenuItemPreferences = new javax.swing.JMenuItem();

        seriesDisplayPanel.setLayout(new java.awt.BorderLayout());

        labelSeriesName.setFont(new java.awt.Font("Lucida Grande", 0, 18));
        labelSeriesName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSeriesName.setText("Correpondence about something good");
        labelSeriesName.setName("labelSeriesName"); // NOI18N
        seriesDisplayPanel.add(labelSeriesName, java.awt.BorderLayout.NORTH);

        labelSeriesPath.setText("jLabel2");
        labelSeriesPath.setName("labelCollectionAbsolutePath"); // NOI18N
        seriesDisplayPanel.add(labelSeriesPath, java.awt.BorderLayout.SOUTH);

        pnlCollectionsInTargetCollection.setLayout(new javax.swing.BoxLayout(pnlCollectionsInTargetCollection, javax.swing.BoxLayout.Y_AXIS));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("iDrop - iRODS Cloud Browser");
        setMinimumSize(new java.awt.Dimension(600, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        iDropToolbar.setMinimumSize(new java.awt.Dimension(800, 400));
        iDropToolbar.setPreferredSize(new java.awt.Dimension(1077, 40));
        iDropToolbar.setLayout(new java.awt.BorderLayout());

        pnlToolbarSizer.setLayout(new java.awt.BorderLayout());

        pnlTopToolbarSearchArea.setMinimumSize(new java.awt.Dimension(45, 50));
        pnlTopToolbarSearchArea.setLayout(new java.awt.BorderLayout());

        pnlSearchSizer.setMinimumSize(new java.awt.Dimension(74, 30));
        pnlSearchSizer.setPreferredSize(new java.awt.Dimension(254, 50));
        pnlSearchSizer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        lblMainSearch.setText("Search:");
        lblMainSearch.setMaximumSize(null);
        lblMainSearch.setMinimumSize(null);
        lblMainSearch.setPreferredSize(new java.awt.Dimension(45, 40));
        pnlSearchSizer.add(lblMainSearch);

        txtMainSearch.setColumns(20);
        txtMainSearch.setToolTipText("Search for files or tags");
        txtMainSearch.setMinimumSize(null);
        txtMainSearch.setPreferredSize(new java.awt.Dimension(100, 30));
        txtMainSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMainSearchKeyPressed(evt);
            }
        });
        pnlSearchSizer.add(txtMainSearch);

        pnlTopToolbarSearchArea.add(pnlSearchSizer, java.awt.BorderLayout.SOUTH);

        pnlToolbarSizer.add(pnlTopToolbarSearchArea, java.awt.BorderLayout.CENTER);

        pnlLocalToggleSizer.setLayout(new java.awt.BorderLayout());

        toggleLocalFiles.setText("<<< Local Files");
        toggleLocalFiles.setToolTipText("Browse the local file system.");
        toggleLocalFiles.setMaximumSize(new java.awt.Dimension(144, 10));
        toggleLocalFiles.setMinimumSize(new java.awt.Dimension(144, 10));
        toggleLocalFiles.setPreferredSize(new java.awt.Dimension(144, 30));
        toggleLocalFiles.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                toggleLocalFilesStateChanged(evt);
            }
        });
        toggleLocalFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleLocalFilesActionPerformed(evt);
            }
        });
        pnlLocalToggleSizer.add(toggleLocalFiles, java.awt.BorderLayout.NORTH);
        toggleLocalFiles.getAccessibleContext().setAccessibleName("<<< Local Files ");

        pnlToolbarSizer.add(pnlLocalToggleSizer, java.awt.BorderLayout.WEST);

        pnlIrodsDetailsToggleSizer.setLayout(new java.awt.BorderLayout());

        toggleIrodsDetails.setToolTipText("Browse the local file system.");
        toggleIrodsDetails.setLabel("iRODS Info >>>>");
        toggleIrodsDetails.setMaximumSize(new java.awt.Dimension(144, 10));
        toggleIrodsDetails.setMinimumSize(new java.awt.Dimension(144, 10));
        toggleIrodsDetails.setPreferredSize(new java.awt.Dimension(144, 30));
        toggleIrodsDetails.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                toggleIrodsDetailsStateChanged(evt);
            }
        });
        toggleIrodsDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleIrodsDetailsActionPerformed(evt);
            }
        });
        pnlIrodsDetailsToggleSizer.add(toggleIrodsDetails, java.awt.BorderLayout.NORTH);
        toggleIrodsDetails.getAccessibleContext().setAccessibleName("");

        pnlToolbarSizer.add(pnlIrodsDetailsToggleSizer, java.awt.BorderLayout.EAST);

        iDropToolbar.add(pnlToolbarSizer, java.awt.BorderLayout.NORTH);

        getContentPane().add(iDropToolbar, java.awt.BorderLayout.NORTH);

        pnlIdropMain.setPreferredSize(new java.awt.Dimension(500, 300));
        pnlIdropMain.setLayout(new javax.swing.BoxLayout(pnlIdropMain, javax.swing.BoxLayout.PAGE_AXIS));

        jSplitPanelLocalRemote.setBorder(null);
        jSplitPanelLocalRemote.setDividerLocation(250);
        jSplitPanelLocalRemote.setDividerSize(30);
        jSplitPanelLocalRemote.setMaximumSize(null);
        jSplitPanelLocalRemote.setPreferredSize(new java.awt.Dimension(0, 0));

        pnlLocalTreeArea.setBackground(new java.awt.Color(153, 255, 102));
        pnlLocalTreeArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlLocalTreeArea.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlLocalTreeArea.setOpaque(false);
        pnlLocalTreeArea.setPreferredSize(new java.awt.Dimension(0, 0));
        pnlLocalTreeArea.setLayout(new java.awt.BorderLayout());

        pnlLocalRoots.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlLocalRoots.setLayout(new java.awt.BorderLayout());

        scrollLocalDrives.setMaximumSize(null);
        scrollLocalDrives.setMinimumSize(new java.awt.Dimension(0, 0));
        scrollLocalDrives.setPreferredSize(new java.awt.Dimension(300, 100));

        listLocalDrives.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listLocalDrives.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listLocalDrives.setMaximumSize(null);
        listLocalDrives.setPreferredSize(new java.awt.Dimension(150, 200));
        listLocalDrives.setVisibleRowCount(4);
        scrollLocalDrives.setViewportView(listLocalDrives);

        pnlLocalRoots.add(scrollLocalDrives, java.awt.BorderLayout.CENTER);

        pnlRefreshButton.setMaximumSize(new java.awt.Dimension(1000, 30));
        pnlRefreshButton.setMinimumSize(new java.awt.Dimension(0, 0));
        pnlRefreshButton.setPreferredSize(new java.awt.Dimension(101, 30));

        btnRefreshLocalDrives.setLabel("Refresh");
        btnRefreshLocalDrives.setMaximumSize(new java.awt.Dimension(200, 50));
        btnRefreshLocalDrives.setMinimumSize(new java.awt.Dimension(0, 0));
        btnRefreshLocalDrives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshLocalDrivesActionPerformed(evt);
            }
        });
        pnlRefreshButton.add(btnRefreshLocalDrives);

        pnlLocalRoots.add(pnlRefreshButton, java.awt.BorderLayout.NORTH);
        pnlLocalRoots.add(pnlDrivesFiller, java.awt.BorderLayout.SOUTH);

        pnlLocalTreeArea.add(pnlLocalRoots, java.awt.BorderLayout.NORTH);

        scrollLocalFileTree.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        scrollLocalFileTree.setBorder(null);
        scrollLocalFileTree.setToolTipText("scroll panel tooltip");
        scrollLocalFileTree.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollLocalFileTree.setMaximumSize(null);
        scrollLocalFileTree.setMinimumSize(new java.awt.Dimension(0, 0));
        scrollLocalFileTree.setPreferredSize(new java.awt.Dimension(500, 500));
        pnlLocalTreeArea.add(scrollLocalFileTree, java.awt.BorderLayout.CENTER);

        jSplitPanelLocalRemote.setLeftComponent(pnlLocalTreeArea);

        pnlIrodsArea.setPreferredSize(new java.awt.Dimension(600, 304));
        pnlIrodsArea.setLayout(new java.awt.BorderLayout());

        splitTargetCollections.setDividerLocation(400);
        splitTargetCollections.setMinimumSize(new java.awt.Dimension(0, 0));

        tabIrodsViews.setPreferredSize(new java.awt.Dimension(350, 300));
        tabIrodsViews.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabIrodsViewsStateChanged(evt);
            }
        });

        pnlTabHierarchicalView.setLayout(new java.awt.BorderLayout());

        btnRefreshTargetTree.setMnemonic('r');
        btnRefreshTargetTree.setText("Refresh");
        btnRefreshTargetTree.setToolTipText("Refresh the view of the iRODS server");
        btnRefreshTargetTree.setFocusable(false);
        btnRefreshTargetTree.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefreshTargetTree.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefreshTargetTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTargetTreeActionPerformed(evt);
            }
        });
        pnlIrodsTreeToolbar.add(btnRefreshTargetTree);

        pnlTabHierarchicalView.add(pnlIrodsTreeToolbar, java.awt.BorderLayout.NORTH);

        pnlIrodsTreeMaster.setLayout(new java.awt.BorderLayout());

        scrollIrodsTree.setMinimumSize(null);
        scrollIrodsTree.setPreferredSize(null);

        pnlTargetTree.setLayout(new java.awt.BorderLayout());
        scrollIrodsTree.setViewportView(pnlTargetTree);

        pnlIrodsTreeMaster.add(scrollIrodsTree, java.awt.BorderLayout.CENTER);

        pnlTabHierarchicalView.add(pnlIrodsTreeMaster, java.awt.BorderLayout.CENTER);

        tabIrodsViews.addTab("iRODS Tree View", pnlTabHierarchicalView);

        pnlTabTagView.setLayout(new java.awt.BorderLayout());

        btnRefreshTagCloud.setMnemonic('C');
        btnRefreshTagCloud.setText("Refresh Tag Cloud");
        btnRefreshTagCloud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTagCloudActionPerformed(evt);
            }
        });
        pnlTagSearch.add(btnRefreshTagCloud);

        pnlTabTagView.add(pnlTagSearch, java.awt.BorderLayout.NORTH);

        pnlTagViewMaster.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlTagViewMasterComponentShown(evt);
            }
        });
        pnlTagViewMaster.setLayout(new java.awt.GridLayout(1, 0));

        splitTagsAndTagResults.setDividerLocation(100);

        pnlTagListing.setLayout(new java.awt.GridLayout(1, 0));

        listTagCloudList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listTagCloudList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listTagCloudListValueChanged(evt);
            }
        });
        scrollPaneTagCloudList.setViewportView(listTagCloudList);

        pnlTagListing.add(scrollPaneTagCloudList);

        splitTagsAndTagResults.setLeftComponent(pnlTagListing);

        scrollTagResults.setMinimumSize(null);
        scrollTagResults.setPreferredSize(new java.awt.Dimension(0, 0));

        pnlTagResultsInner.setLayout(new javax.swing.BoxLayout(pnlTagResultsInner, javax.swing.BoxLayout.PAGE_AXIS));
        scrollTagResults.setViewportView(pnlTagResultsInner);

        splitTagsAndTagResults.setRightComponent(scrollTagResults);

        pnlTagViewMaster.add(splitTagsAndTagResults);

        pnlTabTagView.add(pnlTagViewMaster, java.awt.BorderLayout.CENTER);

        tabIrodsViews.addTab("Tag View", pnlTabTagView);

        splitTargetCollections.setLeftComponent(tabIrodsViews);

        pnlIrodsInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        pnlIrodsInfo.setLayout(new java.awt.GridLayout(1, 0));

        scrollIrodsInfo.setPreferredSize(new java.awt.Dimension(400, 300));

        pnlIrodsInfoInner.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlIrodsInfoInner.setToolTipText("Information on selected iRODS file or collection");
        pnlIrodsInfoInner.setMinimumSize(new java.awt.Dimension(250, 200));
        pnlIrodsInfoInner.setPreferredSize(new java.awt.Dimension(300, 300));
        pnlIrodsInfoInner.setLayout(new java.awt.GridLayout(0, 1, 10, 10));

        pnlFileIconSizer.setMinimumSize(new java.awt.Dimension(50, 50));
        pnlFileIconSizer.setPreferredSize(new java.awt.Dimension(50, 50));
        pnlFileIconSizer.setLayout(new java.awt.BorderLayout());

        pnlInfoIcon.setMaximumSize(new java.awt.Dimension(50, 50));
        pnlInfoIcon.setLayout(new java.awt.GridLayout(1, 0));
        pnlFileIconSizer.add(pnlInfoIcon, java.awt.BorderLayout.WEST);

        pnlIrodsInfoInner.add(pnlFileIconSizer);

        pnlFileNameAndIcon.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        lblFileOrCollectionName.setText("jLabel1");
        pnlFileNameAndIcon.add(lblFileOrCollectionName);

        pnlIrodsInfoInner.add(pnlFileNameAndIcon);

        pnlInfoCollectionParent.setMinimumSize(new java.awt.Dimension(65, 39));
        pnlInfoCollectionParent.setLayout(new java.awt.BorderLayout());

        lblFileParent.setText("Parent path of file:");
        pnlInfoCollectionParent.add(lblFileParent, java.awt.BorderLayout.NORTH);
        lblFileParent.getAccessibleContext().setAccessibleDescription("The path of the parent of the file or collection");

        pnlScrollParentPathSizer.setLayout(new java.awt.BorderLayout());

        scrollParentPath.setMinimumSize(null);

        txtParentPath.setColumns(32);
        txtParentPath.setEditable(false);
        txtParentPath.setMaximumSize(null);
        txtParentPath.setMinimumSize(null);
        scrollParentPath.setViewportView(txtParentPath);

        pnlScrollParentPathSizer.add(scrollParentPath, java.awt.BorderLayout.WEST);

        pnlInfoCollectionParent.add(pnlScrollParentPathSizer, java.awt.BorderLayout.CENTER);

        pnlIrodsInfoInner.add(pnlInfoCollectionParent);

        pnlInfoComment.setLayout(new java.awt.BorderLayout());

        lblComment.setText("Comment:");
        lblComment.setToolTipText("");
        pnlInfoComment.add(lblComment, java.awt.BorderLayout.NORTH);
        lblComment.getAccessibleContext().setAccessibleDescription("lable for comment area");

        pnlInfoCommentScrollSizer.setPreferredSize(new java.awt.Dimension(388, 84));
        pnlInfoCommentScrollSizer.setLayout(new java.awt.BorderLayout());

        scrollComment.setMinimumSize(null);
        scrollComment.setPreferredSize(new java.awt.Dimension(388, 84));

        txtComment.setColumns(32);
        txtComment.setMaximumSize(null);
        txtComment.setMinimumSize(null);
        scrollComment.setViewportView(txtComment);

        pnlInfoCommentScrollSizer.add(scrollComment, java.awt.BorderLayout.WEST);

        pnlInfoComment.add(pnlInfoCommentScrollSizer, java.awt.BorderLayout.CENTER);

        pnlIrodsInfoInner.add(pnlInfoComment);

        pnlInfoTags.setLayout(new java.awt.BorderLayout());

        lblTags.setText("Tags:");
        lblTags.setToolTipText("");
        pnlInfoTags.add(lblTags, java.awt.BorderLayout.NORTH);
        lblTags.getAccessibleContext().setAccessibleName("Tags");
        lblTags.getAccessibleContext().setAccessibleDescription("Label for free tagging area");

        pnlInfoTagsSizer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        txtTags.setColumns(30);
        txtTags.setToolTipText("Name of file or collection.  This field allows editing to rename");
        txtTags.setMinimumSize(null);
        txtTags.setPreferredSize(null);
        txtTags.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTagsFocusLost(evt);
            }
        });
        txtTags.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtTagsKeyPressed(evt);
            }
        });
        pnlInfoTagsSizer.add(txtTags);

        pnlInfoTags.add(pnlInfoTagsSizer, java.awt.BorderLayout.CENTER);

        pnlIrodsInfoInner.add(pnlInfoTags);

        pnlInfoButton.setLayout(new java.awt.BorderLayout());

        pnlInfoButtonSizer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnUpdateInfo.setMnemonic('I');
        btnUpdateInfo.setText("Update Info");
        btnUpdateInfo.setToolTipText("Update information on the info panel such as tags and comment");
        btnUpdateInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateInfoActionPerformed(evt);
            }
        });
        pnlInfoButtonSizer.add(btnUpdateInfo);

        pnlInfoButton.add(pnlInfoButtonSizer, java.awt.BorderLayout.SOUTH);

        pnlIrodsInfoInner.add(pnlInfoButton);

        pnlInfoDetails.setLayout(new java.awt.GridBagLayout());

        lblInfoCreatedAt.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblInfoCreatedAt.setText("Created:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlInfoDetails.add(lblInfoCreatedAt, gridBagConstraints);

        lblInfoCreatedAtValue.setText("XXXXXX");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlInfoDetails.add(lblInfoCreatedAtValue, gridBagConstraints);

        lblInfoUpdatedAt.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblInfoUpdatedAt.setText("Updated:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlInfoDetails.add(lblInfoUpdatedAt, gridBagConstraints);

        lblInfoUpdatedAtValue.setText("XXXXXX");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlInfoDetails.add(lblInfoUpdatedAtValue, gridBagConstraints);

        lblInfoLength.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblInfoLength.setText("Length:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlInfoDetails.add(lblInfoLength, gridBagConstraints);

        lblInfoLengthValue.setText("XXXXXX");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlInfoDetails.add(lblInfoLengthValue, gridBagConstraints);

        pnlIrodsInfoInner.add(pnlInfoDetails);

        pnlToolbarInfo.setLayout(new java.awt.BorderLayout());

        toolBarInfo.setRollover(true);

        btnViewMetadata.setText("Metadata");
        btnViewMetadata.setFocusable(false);
        btnViewMetadata.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnViewMetadata.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnViewMetadata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewMetadataActionPerformed(evt);
            }
        });
        toolBarInfo.add(btnViewMetadata);

        btnReplication.setText("Replication");
        btnReplication.setFocusable(false);
        btnReplication.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReplication.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReplication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReplicationActionPerformed(evt);
            }
        });
        toolBarInfo.add(btnReplication);
        toolBarInfo.add(separator1);

        btnMoveToTrash.setText("Move to Trash");
        btnMoveToTrash.setFocusable(false);
        btnMoveToTrash.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMoveToTrash.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarInfo.add(btnMoveToTrash);

        separator2.setMinimumSize(new java.awt.Dimension(50, 1));
        toolBarInfo.add(separator2);

        pnlToolbarInfo.add(toolBarInfo, java.awt.BorderLayout.NORTH);

        pnlIrodsInfoInner.add(pnlToolbarInfo);

        scrollIrodsInfo.setViewportView(pnlIrodsInfoInner);
        pnlIrodsInfoInner.getAccessibleContext().setAccessibleName("info panel");

        pnlIrodsInfo.add(scrollIrodsInfo);

        splitTargetCollections.setRightComponent(pnlIrodsInfo);

        pnlIrodsArea.add(splitTargetCollections, java.awt.BorderLayout.CENTER);

        jSplitPanelLocalRemote.setRightComponent(pnlIrodsArea);

        pnlIdropMain.add(jSplitPanelLocalRemote);

        getContentPane().add(pnlIdropMain, java.awt.BorderLayout.CENTER);

        pnlIdropBottom.setToolTipText("Display area for status and messages");
        pnlIdropBottom.setLayout(new java.awt.BorderLayout());
        pnlIdropBottom.add(lblIdropMessage, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlIdropBottom, java.awt.BorderLayout.SOUTH);

        jMenuFile.setMnemonic('f');
        jMenuFile.setText("File");

        jMenuItemExit.setMnemonic('x');
        jMenuItemExit.setText("Exit");
        jMenuItemExit.setToolTipText("Close the iDROP console window");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar1.add(jMenuFile);

        jMenuEdit.setMnemonic('E');
        jMenuEdit.setText("Edit");
        jMenuBar1.add(jMenuEdit);

        jMenuView.setMnemonic('V');
        jMenuView.setText("View");

        jCheckBoxMenuItemShowSourceTree.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK));
        jCheckBoxMenuItemShowSourceTree.setMnemonic('L');
        jCheckBoxMenuItemShowSourceTree.setText("Show Local");
        jCheckBoxMenuItemShowSourceTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowSourceTreeActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowSourceTree);

        jCheckBoxMenuItemShowIrodsInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK));
        jCheckBoxMenuItemShowIrodsInfo.setMnemonic('I');
        jCheckBoxMenuItemShowIrodsInfo.setText("Show iRODS Info");
        jCheckBoxMenuItemShowIrodsInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowIrodsInfoActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowIrodsInfo);

        jMenuItemPreferences.setMnemonic('p');
        jMenuItemPreferences.setText("Preferences");
        jMenuItemPreferences.setToolTipText("Show the preferences panel");
        jMenuItemPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPreferencesActionPerformed(evt);
            }
        });
        jMenuView.add(jMenuItemPreferences);

        jMenuBar1.add(jMenuView);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        this.setVisible(false);
        this.formShown = false;
    }//GEN-LAST:event_formWindowClosed

    private void btnRefreshTargetTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTargetTreeActionPerformed
        buildTargetTree();
    }//GEN-LAST:event_btnRefreshTargetTreeActionPerformed

    private void toggleLocalFilesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_toggleLocalFilesStateChanged
    }//GEN-LAST:event_toggleLocalFilesStateChanged

    private void toggleLocalFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleLocalFilesActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                pnlLocalTreeArea.setVisible(toggleLocalFiles.isSelected());
                jCheckBoxMenuItemShowSourceTree.setSelected(toggleLocalFiles.isSelected());
                if (pnlLocalTreeArea.isVisible()) {
                    jSplitPanelLocalRemote.setDividerLocation(0.3d);
                }
            }
        });
    }//GEN-LAST:event_toggleLocalFilesActionPerformed

    /**
     * Display/hide a panel that depicts the local file system.
     * @param evt
     */
    private void jCheckBoxMenuItemShowSourceTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowSourceTreeActionPerformed
        toggleLocalFiles.setSelected(jCheckBoxMenuItemShowSourceTree.isSelected());
        toggleLocalFilesActionPerformed(evt);
    }//GEN-LAST:event_jCheckBoxMenuItemShowSourceTreeActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    /**
     * Handle the press of the refresh local drives button, refresh the local file tree.
     * @param evt
     */
    private void btnRefreshLocalDrivesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshLocalDrivesActionPerformed
        // keep track of currently selected paths
        log.debug("refreshing local files tree");

        if (fileTree == null) {
            log.warn("null file tree - ignored when refreshing");
            return;
        }

        final TreePath rootPath = fileTree.getPathForRow(0);
        final Enumeration<TreePath> currentPaths = fileTree.getExpandedDescendants(rootPath);
        log.debug("expanded local tree node, paths are:{}", currentPaths);

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    // keep track of the currently selected drive
                    Object selectedDrive = listLocalDrives.getSelectedValue();
                    initializeLocalFileTreeModel(selectedDrive);
                    fileTree.setModel(localFileModel);

                    // re-expand the tree paths that are currently expanded
                    final Enumeration<TreePath> pathsToExpand = currentPaths;
                    fileTree.expandTreeNodesBasedOnListOfPreviouslyExpandedNodes(pathsToExpand);
                } catch (IdropException ex) {
                    Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException("exception expanding tree nodes", ex);
                }

            }
        });




    }//GEN-LAST:event_btnRefreshLocalDrivesActionPerformed

    private void toggleIrodsDetailsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_toggleIrodsDetailsStateChanged
        // unused right now
    }//GEN-LAST:event_toggleIrodsDetailsStateChanged

    /**
     * Show or hide the irods details panel
     * @param evt
     */
    private void toggleIrodsDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleIrodsDetailsActionPerformed
        jCheckBoxMenuItemShowIrodsInfo.setSelected(toggleIrodsDetails.isSelected());
        handleInfoPanelShowOrHide();
    }//GEN-LAST:event_toggleIrodsDetailsActionPerformed

    private void jCheckBoxMenuItemShowIrodsInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowIrodsInfoActionPerformed
        toggleIrodsDetails.setSelected(jCheckBoxMenuItemShowIrodsInfo.isSelected());
        handleInfoPanelShowOrHide();

    }//GEN-LAST:event_jCheckBoxMenuItemShowIrodsInfoActionPerformed

    /**
     * Focus lost on tags, update tags in the info box.  Updates are done in the tagging service by taking a delta between
     * the current and desired tag set.
     * @param evt
     */
    private void txtTagsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTagsFocusLost
    }//GEN-LAST:event_txtTagsFocusLost

    /**
     * Method to clear any cached values when an account changes.  Some data is cached and lazily loaded
     */
    public void signalChangeInAccountSoCachedDataCanBeCleared() {
        log.info("clearing any cached data associated with the account");
        userTagCloudView = null;
        irodsTree = null;
        lastCachedInfoItem = null;
    }

    /**
     * Tag view master panel has been shown, lazily load the user tag cloud if not loaded
     * @param evt
     */
    private void pnlTagViewMasterComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlTagViewMasterComponentShown
        // dont think right event TODO: loose this event
    }//GEN-LAST:event_pnlTagViewMasterComponentShown

    private void tabIrodsViewsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabIrodsViewsStateChanged

        if (tabIrodsViews.isShowing()) {
            if (userTagCloudView == null) {
                refreshTagCloud();
            }
        }

    }//GEN-LAST:event_tabIrodsViewsStateChanged

    /**
     * rebuild the tag cloud list 
     */
    private void refreshTagCloud() {
        log.info("lazily loading user tag cloud for:{}", getIrodsAccount());
        try {
            // FIXME: depict data objects/collections?  munge together?  wha...
            UserTagCloudService userTagCloudService = UserTagCloudServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), getIrodsAccount());
            userTagCloudView = userTagCloudService.getTagCloud();
        } catch (JargonException ex) {
            Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            irodsFileSystem.closeAndEatExceptions(getIrodsAccount());

        }
        try {
            TagCloudListModel tagCloudListModel = new TagCloudListModel(userTagCloudView);
            listTagCloudList.setModel(tagCloudListModel);
        } catch (IdropException ex) {
            Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }
    }

    private void txtMainSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMainSearchKeyPressed

        // enter key triggers search
        if (evt.getKeyCode() != java.awt.event.KeyEvent.VK_ENTER) {
            return;
        }

        log.info("do a search");

        if (pnlTabHierarchicalView.isShowing()) {
            log.info("search of file hierarchy");
        } else {
            log.info("search by tags");
            searchTagsAndBuildResultEntries(txtMainSearch.getText());
        }
    }//GEN-LAST:event_txtMainSearchKeyPressed

    /**
     * Process a selection in the tag cloud list
     * @param evt
     */
    private void listTagCloudListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTagCloudListValueChanged
        //value changed on list selection indicates that a tag search should be done
        if (evt.getValueIsAdjusting()) {
            return;
        }

        Object selectedModelAsObject = listTagCloudList.getSelectedValue();

        if (selectedModelAsObject == null) {
            return;
        }

        TagCloudListModel tagCloudListModel = (TagCloudListModel) listTagCloudList.getModel();



        TagCloudEntry entry = tagCloudListModel.getTagCloudEntry(listTagCloudList.getMinSelectionIndex());

        log.info("search for tag cloud entry based on list selection:{}", entry);

        searchTagsAndBuildResultEntries(entry.getIrodsTagValue().getTagData());

    }//GEN-LAST:event_listTagCloudListValueChanged

    private void btnRefreshTagCloudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTagCloudActionPerformed
        log.info("refreshing the tag cloud on user action");
        refreshTagCloud();
    }//GEN-LAST:event_btnRefreshTagCloudActionPerformed

    private void txtTagsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTagsKeyPressed

        // FIXME: cull this

        // enter key triggers search
        if (evt.getKeyCode() != java.awt.event.KeyEvent.VK_ENTER) {
            return;
        }

    }//GEN-LAST:event_txtTagsKeyPressed

    /**
     * The view menu of iDrop indicates that the user wants to show the preferences dialog
     * @param evt
     */
    private void jMenuItemPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPreferencesActionPerformed
        showPreferencesDialog();
    }//GEN-LAST:event_jMenuItemPreferencesActionPerformed

    /**
     * Display the data replication dialog for the collection or data object depicted in the info panel
     * @param evt
     */
    private void btnReplicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReplicationActionPerformed
        if (lastCachedInfoItem == null) {
            return;
        }

        ReplicationDialog replicationDialog;
        if (lastCachedInfoItem instanceof DataObject) {
            DataObject cachedDataObject = (DataObject) lastCachedInfoItem;
            replicationDialog = new ReplicationDialog(this, true, cachedDataObject.getCollectionName(), cachedDataObject.getDataName());
        } else if (lastCachedInfoItem instanceof Collection) {
            Collection collection = (Collection) lastCachedInfoItem;
            replicationDialog = new ReplicationDialog(this, true, collection.getCollectionName());
        } else {
            showIdropException(new IdropException("Unknown type of object displayed in info area, cannot create the replication dialog"));
            throw new IdropRuntimeException("unknown type of object displayed in info area");
        }

        replicationDialog.setLocation((int) (this.getLocation().getX() + replicationDialog.getWidth() / 2), (int) (this.getLocation().getY() + replicationDialog.getHeight() / 2));
        replicationDialog.setVisible(true);
    }//GEN-LAST:event_btnReplicationActionPerformed

    /**
     * Display the metadata edit /view dialog for the item displayed in the info panel
     * @param evt
     */
    private void btnViewMetadataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewMetadataActionPerformed
        if (lastCachedInfoItem == null) {
            return;
        }

        MetadataViewDialog metadataViewDialog;
        if (lastCachedInfoItem instanceof DataObject) {
            DataObject cachedDataObject = (DataObject) lastCachedInfoItem;
            metadataViewDialog = new MetadataViewDialog(this, getIrodsAccount(), cachedDataObject.getCollectionName(), cachedDataObject.getDataName());
        } else if (lastCachedInfoItem instanceof Collection) {
            Collection collection = (Collection) lastCachedInfoItem;
            metadataViewDialog = new MetadataViewDialog(this, getIrodsAccount(), collection.getCollectionName());
        } else {
            showIdropException(new IdropException("Unknown type of object displayed in info area, cannot create the replication dialog"));
            throw new IdropRuntimeException("unknown type of object displayed in info area");
        }

        metadataViewDialog.setLocation((int) (this.getLocation().getX() + metadataViewDialog.getWidth() / 2), (int) (this.getLocation().getY() + metadataViewDialog.getHeight() / 2));
        metadataViewDialog.setVisible(true);
    }//GEN-LAST:event_btnViewMetadataActionPerformed

    private void btnUpdateInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateInfoActionPerformed
        // if I have cached an item, see if it is a file or collection

        if (this.lastCachedInfoItem == null) {
            log.warn("unknown data item, tags will not be processed");
            return;
        }

        // initialize a variable with the last item visible to the runnable
        final Object lastCachedItemToProcessTagsFor = this.lastCachedInfoItem;
        final iDrop idropGui = this;

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                FreeTaggingService freeTaggingService;

                try {
                    freeTaggingService = FreeTaggingServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), getIrodsAccount());

                    if (lastCachedInfoItem instanceof Collection) {
                        log.info("processing tags for collection");
                        Collection collection = (Collection) lastCachedItemToProcessTagsFor;
                        IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.COLLECTION, collection.getCollectionName(), txtTags.getText(), getIrodsAccount().getUserName());
                        log.debug("new tag set is:{}", txtTags.getText());
                        freeTaggingService.updateTags(irodsTagGrouping);
                    } else if (lastCachedInfoItem instanceof DataObject) {
                        log.info("processing tags for data object");
                        DataObject dataObject = (DataObject) lastCachedItemToProcessTagsFor;
                        IRODSTagGrouping irodsTagGrouping = new IRODSTagGrouping(MetadataDomain.DATA, dataObject.getCollectionName() + "/" + dataObject.getDataName(), txtTags.getText(), getIrodsAccount().getUserName());
                        log.debug("new tag set is:{}", txtTags.getText());
                        freeTaggingService.updateTags(irodsTagGrouping);
                    } else {
                        log.error("unknown item type cached as being displayed in info area");
                        throw new IdropRuntimeException("unknown item type cached");
                    }

                    idropGui.showMessageFromOperation("update of info successful");

                } catch (JargonException ex) {
                    Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException(ex);
                } finally {
                    try {
                        irodsFileSystem.close(getIrodsAccount());
                    } catch (JargonException ex) {
                        Logger.getLogger(iDrop.class.getName()).log(Level.SEVERE, null, ex);
                        // logged and ignored
                    }
                }
            }
        });
    }//GEN-LAST:event_btnUpdateInfoActionPerformed

    /**
     * Common code to show the preferences dialog
     */
    private void showPreferencesDialog() {
        if (preferencesDialog == null) {
            preferencesDialog = new PreferencesDialog(this, true);
            preferencesDialog.setLocation((int) (preferencesDialog.getLocation().getX() + preferencesDialog.getWidth() / 2), (int) (preferencesDialog.getLocation().getY() + preferencesDialog.getHeight() / 2));
            preferencesDialog.setVisible(true);
        } else {
            preferencesDialog.setVisible(true);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMoveToTrash;
    private javax.swing.JButton btnRefreshLocalDrives;
    private javax.swing.JButton btnRefreshTagCloud;
    private javax.swing.JButton btnRefreshTargetTree;
    private javax.swing.JButton btnReplication;
    private javax.swing.JButton btnUpdateInfo;
    private javax.swing.JButton btnViewMetadata;
    private javax.swing.JPanel iDropToolbar;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowIrodsInfo;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowSourceTree;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemPreferences;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JSplitPane jSplitPanelLocalRemote;
    private javax.swing.JLabel labelSeriesName;
    private javax.swing.JLabel labelSeriesPath;
    private javax.swing.JLabel lblComment;
    private javax.swing.JLabel lblFileOrCollectionName;
    private javax.swing.JLabel lblFileParent;
    private javax.swing.JLabel lblIdropMessage;
    private javax.swing.JLabel lblInfoCreatedAt;
    private javax.swing.JLabel lblInfoCreatedAtValue;
    private javax.swing.JLabel lblInfoLength;
    private javax.swing.JLabel lblInfoLengthValue;
    private javax.swing.JLabel lblInfoUpdatedAt;
    private javax.swing.JLabel lblInfoUpdatedAtValue;
    private javax.swing.JLabel lblMainSearch;
    private javax.swing.JLabel lblTags;
    private javax.swing.JList listLocalDrives;
    private javax.swing.JList listTagCloudList;
    private javax.swing.JPanel pnlCollectionsInTargetCollection;
    private javax.swing.JPanel pnlDrivesFiller;
    private javax.swing.JPanel pnlFileIconSizer;
    private javax.swing.JPanel pnlFileNameAndIcon;
    private javax.swing.JPanel pnlIdropBottom;
    private javax.swing.JPanel pnlIdropMain;
    private javax.swing.JPanel pnlInfoButton;
    private javax.swing.JPanel pnlInfoButtonSizer;
    private javax.swing.JPanel pnlInfoCollectionParent;
    private javax.swing.JPanel pnlInfoComment;
    private javax.swing.JPanel pnlInfoCommentScrollSizer;
    private javax.swing.JPanel pnlInfoDetails;
    private javax.swing.JPanel pnlInfoIcon;
    private javax.swing.JPanel pnlInfoTags;
    private javax.swing.JPanel pnlInfoTagsSizer;
    private javax.swing.JPanel pnlIrodsArea;
    private javax.swing.JPanel pnlIrodsDetailsToggleSizer;
    private javax.swing.JPanel pnlIrodsInfo;
    private javax.swing.JPanel pnlIrodsInfoInner;
    private javax.swing.JPanel pnlIrodsTreeMaster;
    private javax.swing.JPanel pnlIrodsTreeToolbar;
    private javax.swing.JPanel pnlLocalRoots;
    private javax.swing.JPanel pnlLocalToggleSizer;
    private javax.swing.JPanel pnlLocalTreeArea;
    private javax.swing.JPanel pnlRefreshButton;
    private javax.swing.JPanel pnlScrollParentPathSizer;
    private javax.swing.JPanel pnlSearchSizer;
    private javax.swing.JPanel pnlTabHierarchicalView;
    private javax.swing.JPanel pnlTabTagView;
    private javax.swing.JPanel pnlTagListing;
    private javax.swing.JPanel pnlTagResultsInner;
    private javax.swing.JPanel pnlTagSearch;
    private javax.swing.JPanel pnlTagViewMaster;
    private javax.swing.JPanel pnlTargetTree;
    private javax.swing.JPanel pnlToolbarInfo;
    private javax.swing.JPanel pnlToolbarSizer;
    private javax.swing.JPanel pnlTopToolbarSearchArea;
    private javax.swing.JScrollPane scrollComment;
    private javax.swing.JScrollPane scrollIrodsInfo;
    private javax.swing.JScrollPane scrollIrodsTree;
    private javax.swing.JScrollPane scrollLocalDrives;
    private javax.swing.JScrollPane scrollLocalFileTree;
    private javax.swing.JScrollPane scrollPaneTagCloudList;
    private javax.swing.JScrollPane scrollParentPath;
    private javax.swing.JScrollPane scrollTagResults;
    private javax.swing.JToolBar.Separator separator1;
    private javax.swing.JToolBar.Separator separator2;
    private javax.swing.JPanel seriesDisplayPanel;
    private javax.swing.JSplitPane splitTagsAndTagResults;
    private javax.swing.JSplitPane splitTargetCollections;
    private javax.swing.JTabbedPane tabIrodsViews;
    private javax.swing.JToggleButton toggleIrodsDetails;
    private javax.swing.JToggleButton toggleLocalFiles;
    private javax.swing.JToolBar toolBarInfo;
    private javax.swing.JTextArea txtComment;
    private javax.swing.JTextField txtMainSearch;
    private javax.swing.JTextArea txtParentPath;
    private javax.swing.JTextField txtTags;
    // End of variables declaration//GEN-END:variables

    /**
     * Process a tag search and build the tag result panels based on the given search text
     * @param searchText
     */
    private void searchTagsAndBuildResultEntries(final String searchText) {
        if (searchText.isEmpty()) {
            this.showMessageFromOperation("please enter a tag to search on");
            return;
        }

        final String searchTerms = searchText.trim();
        final iDrop idropGui = this;

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                try {
                    idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    FreeTaggingService freeTaggingService = FreeTaggingServiceImpl.instance(irodsFileSystem.getIRODSAccessObjectFactory(), getIrodsAccount());
                    TagQuerySearchResult result = freeTaggingService.searchUsingFreeTagString(searchTerms);
                    pnlTagResultsInner.removeAll();
                    pnlTagResultsInner.validate();
                    scrollTagResults.validate();

                    ColorHelper colorHelper = new ColorHelper();

                    log.info("doing tag query on {}, build panels based on result");
                    //FIXME: this is a bit of a shim for duplicates, i need to look at findWhere() in jargon core to determine whether a distinct option is needed, for now, filter

                    String lastEntry = "";
                    String thisEntry = "";

                    for (CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry : result.getQueryResultEntries()) {
                        if (collectionAndDataObjectListingEntry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.COLLECTION) {
                            thisEntry = collectionAndDataObjectListingEntry.getPathOrName();
                        } else {
                            thisEntry = collectionAndDataObjectListingEntry.getParentPath() + "/" + collectionAndDataObjectListingEntry.getPathOrName();
                        }

                        if (thisEntry.equals(lastEntry)) {
                            continue;
                        } else {
                            lastEntry = thisEntry;
                        }

                        DefaultFileRepresentationPanel fileRepresentationPanel =
                                new DefaultFileRepresentationPanel(collectionAndDataObjectListingEntry, idropGui, colorHelper.getNextColor());

                        pnlTagResultsInner.add(fileRepresentationPanel);
                    }

                    pnlTagResultsInner.validate();
                    scrollTagResults.validate();
                    pnlTagResultsInner.repaint();

                } catch (Exception e) {
                    idropGui.showIdropException(e);
                    return;
                } finally {
                    irodsFileSystem.closeAndEatExceptions(getIrodsAccount());
                    idropGui.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    public Object getLastCachedInfoItem() {
        return lastCachedInfoItem;
    }
}
