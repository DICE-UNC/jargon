/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginDialog.java
 *
 * Created on Jul 7, 2010, 4:41:29 PM
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.idrop.desktop.systraygui.utils.IdropConfig;
import org.irods.jargon.idrop.desktop.systraygui.utils.IdropPropertiesHelper;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mikeconway
 */
public class LoginDialog extends javax.swing.JDialog {

    private iDrop iDropParent = null;
    public static org.slf4j.Logger log = LoggerFactory.getLogger(LoginDialog.class);
    private IdropConfig idropConfig = null;

    /** Creates new form LoginDialog */
    public LoginDialog(iDrop iDropParent) {
        super(iDropParent, true);
        this.iDropParent = iDropParent;
        initComponents();
        try {
            // get the properties that determine the layout of the login dialog
            idropConfig = IdropConfig.instance();

            if (idropConfig.isLoginPreset()) {
                log.debug("login will use presets");
                lblHost.setVisible(false);
                txtHost.setVisible(false);
                lblPort.setVisible(false);
                txtPort.setVisible(false);
                lblZone.setVisible(false);
                txtZone.setVisible(false);
                lblResource.setVisible(false);
                txtResource.setVisible(false);
            }

        } catch (IdropException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
            iDropParent.showIdropException(ex);
            System.exit(1);
        }

        registerKeystrokeListener();

    }

    /**
     * Action to take when login is initiated
     * @return
     * @throws NumberFormatException
     */
    private boolean processLogin() throws NumberFormatException {
        //validate various inputs based on whether a full login, or a uid only login is indicated
        if (!idropConfig.isLoginPreset()) {
            txtHost.setBackground(Color.white);
            txtPort.setBackground(Color.white);
            txtZone.setBackground(Color.white);
            txtResource.setBackground(Color.white);
            if (txtHost.getText().length() == 0) {
                txtHost.setBackground(Color.red);
            }
            if (txtPort.getText().length() == 0) {
                txtPort.setBackground(Color.red);
            } else {
                try {
                    Integer.parseInt(txtPort.getText());
                } catch (Exception e) {
                    txtPort.setBackground(Color.red);
                }
            }
            if (txtZone.getText().length() == 0) {
                txtZone.setBackground(Color.red);
            }
            if (txtResource.getText().length() == 0) {
                txtResource.setBackground(Color.red);
            }
        }
        txtUserName.setBackground(Color.white);
        password.setBackground(Color.white);
        if (txtUserName.getText().length() == 0) {
            txtUserName.setBackground(Color.red);
        }
        if (password.getPassword().length == 0) {
            password.setBackground(Color.red);
        }
        StringBuilder sb = new StringBuilder();
        final IRODSAccount irodsAccount;
        try {
            // validated, now try to log in
            if (idropConfig.isLoginPreset()) {
                log.debug("creating account with presets");
                String presetHost = idropConfig.getIdropProperties().getProperty(IdropPropertiesHelper.LOGIN_PRESET_HOST);
                log.info("presetHost:{}", presetHost);
                int presetPort = Integer.parseInt(idropConfig.getIdropProperties().getProperty(IdropPropertiesHelper.LOGIN_PRESET_PORT));
                log.info("presetPort:{}", presetPort);
                String presetZone = idropConfig.getIdropProperties().getProperty(IdropPropertiesHelper.LOGIN_PRESET_ZONE);
                log.info("presetZone:{}", presetZone);
                String presetResource = idropConfig.getIdropProperties().getProperty(IdropPropertiesHelper.LOGIN_PRESET_RESOURCE);
                log.info("presetResource:{}", presetResource);
                sb.append('/');
                sb.append(presetZone);
                sb.append("/home/");
                sb.append(txtUserName.getText());
                irodsAccount = IRODSAccount.instance(presetHost, presetPort, txtUserName.getText(), new String(password.getPassword()), sb.toString(), presetZone, presetResource);
            } else {
                sb.append('/');
                sb.append(txtZone.getText());
                sb.append("/home/");
                sb.append(txtUserName.getText());
                irodsAccount = IRODSAccount.instance(txtHost.getText(), Integer.parseInt(txtPort.getText()), txtUserName.getText(), new String(password.getPassword()), sb.toString(), txtZone.getText(), txtResource.getText());
            }
        } catch (JargonException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
            iDropParent.showIdropException(ex);
            return true;
        }
        IRODSFileSystem irodsFileSystem = null;
        try {
            irodsFileSystem = IRODSFileSystem.instance();
            final UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(irodsAccount);
            final User loggedInUser = userAO.findByName(txtUserName.getText());
            iDropParent.setIrodsAccount(irodsAccount);
            this.dispose();
        } catch (JargonException ex) {
            if (ex.getMessage().indexOf("Connection refused") > -1) {
                Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                iDropParent.showMessageFromOperation("Cannot connect to the server, is it down?");
                return true;
            } else if (ex.getMessage().indexOf("Connection reset") > -1) {
                Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                iDropParent.showMessageFromOperation("Cannot connect to the server, is it down?");
                return true;
            } else if (ex.getMessage().indexOf("io exception opening socket") > -1) {
                Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                iDropParent.showMessageFromOperation("Cannot connect to the server, is it down?");
                return true;
            } else {
                Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                iDropParent.showMessageFromOperation("login error - unable to log in, or invalid user id");
                return true;
            }
        } finally {
            if (irodsFileSystem != null) {
                try {
                    irodsFileSystem.close();
                } catch (JargonException ex) {
                    Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    /**
     * Register a listener for the enter event, so login can occur.
     */
    private void registerKeystrokeListener() {

        KeyStroke enter = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
        Action enterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processLogin();
            }
        };
        btnOK.registerKeyboardAction(enterAction, enter,
                             JComponent.WHEN_IN_FOCUSED_WINDOW);

        }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlLoginInfo = new javax.swing.JPanel();
        lblHost = new javax.swing.JLabel();
        txtHost = new javax.swing.JTextField();
        lblPort = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        lblZone = new javax.swing.JLabel();
        txtZone = new javax.swing.JTextField();
        lblResource = new javax.swing.JLabel();
        txtResource = new javax.swing.JTextField();
        lblUserName = new javax.swing.JLabel();
        txtUserName = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        pnlToolbar = new javax.swing.JPanel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblLogin = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlLoginInfo.setLayout(new java.awt.GridBagLayout());

        lblHost.setText("Host:");
        pnlLoginInfo.add(lblHost, new java.awt.GridBagConstraints());

        txtHost.setColumns(30);
        pnlLoginInfo.add(txtHost, new java.awt.GridBagConstraints());

        lblPort.setText("Port:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        pnlLoginInfo.add(lblPort, gridBagConstraints);

        txtPort.setColumns(8);
        txtPort.setText("1247");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlLoginInfo.add(txtPort, gridBagConstraints);

        lblZone.setText("Zone:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        pnlLoginInfo.add(lblZone, gridBagConstraints);

        txtZone.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        pnlLoginInfo.add(txtZone, gridBagConstraints);

        lblResource.setText("Resource:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        pnlLoginInfo.add(lblResource, gridBagConstraints);

        txtResource.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        pnlLoginInfo.add(txtResource, gridBagConstraints);

        lblUserName.setText("User Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        pnlLoginInfo.add(lblUserName, gridBagConstraints);

        txtUserName.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        pnlLoginInfo.add(txtUserName, gridBagConstraints);

        lblPassword.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        pnlLoginInfo.add(lblPassword, gridBagConstraints);

        password.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlLoginInfo.add(password, gridBagConstraints);

        getContentPane().add(pnlLoginInfo, java.awt.BorderLayout.CENTER);

        pnlToolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 5));

        btnOK.setMnemonic('o');
        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        pnlToolbar.add(btnOK);

        btnCancel.setMnemonic('c');
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlToolbar.add(btnCancel);

        getContentPane().add(pnlToolbar, java.awt.BorderLayout.SOUTH);

        lblLogin.setText("Please log in to your iDrop data grid");
        getContentPane().add(lblLogin, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
      processLogin();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel lblHost;
    private javax.swing.JLabel lblLogin;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblResource;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JLabel lblZone;
    private javax.swing.JPasswordField password;
    private javax.swing.JPanel pnlLoginInfo;
    private javax.swing.JPanel pnlToolbar;
    private javax.swing.JTextField txtHost;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtResource;
    private javax.swing.JTextField txtUserName;
    private javax.swing.JTextField txtZone;
    // End of variables declaration//GEN-END:variables
}
