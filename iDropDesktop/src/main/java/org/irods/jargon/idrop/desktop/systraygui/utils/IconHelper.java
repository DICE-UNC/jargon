package org.irods.jargon.idrop.desktop.systraygui.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;

/**
 * Helper class to manage icons
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IconHelper {

    private static Image virusErrorIcon = null;
    private static Image virusSuccessIcon = null;
    private static Image virusUnknownIcon = null;
    private static Image checksumValidIcon = null;
    private static Image checksumErrorIcon = null;
    private static Image folderIcon = null;
    private static Image folderOpenIcon = null;
    private static Image fileIcon = null;
    private static Image metadataIcon = null;
    private static Image policyIcon = null;
    private static Image replicationIcon = null;

    public static JLabel getFolderIcon() {

        if (folderIcon == null) {
            folderIcon = createImage("images/folder.png", "folder");
        }

        return new JLabel(new ImageIcon(folderIcon));
    }

    public static JLabel getFolderOpenIcon() {

        if (folderOpenIcon == null) {
            folderOpenIcon = createImage("images/folder-open.png", "folder open");
        }

        return new JLabel(new ImageIcon(folderOpenIcon));
    }

    public static JLabel getFileIcon() {

        if (fileIcon == null) {
            fileIcon = createImage("images/file.png", "file");
        }
        return new JLabel(new ImageIcon(fileIcon));
    }

    public static JLabel getVirusErrorIcon() {

        if (virusErrorIcon == null) {
            virusErrorIcon = createImage("images/virus-detected.png", "virus scan fail");
        }

        JLabel virusErrorLabel = new JLabel(new ImageIcon(virusErrorIcon));
        virusErrorLabel.setToolTipText("virus scan failure");

        return virusErrorLabel;
    }

    public static JLabel getVirusSuccessIcon() {
        if (virusSuccessIcon == null) {
            virusSuccessIcon = createImage("images/security-high-2.png", "virus scan success");
        }
        JLabel virusLabel = new JLabel(new ImageIcon(virusSuccessIcon));
        virusLabel.setToolTipText("virus scan success");

        return virusLabel;
    }

    public static JLabel getVirusUnknownIcon() {

        if (virusUnknownIcon == null) {
            virusUnknownIcon = createImage("images/dialog-question.png", "virus scan unknown");
        }
        JLabel virusLabel = new JLabel(new ImageIcon(virusUnknownIcon));
        virusLabel.setToolTipText("virus scan status unknown");

        return virusLabel;
    }

    public static JLabel getFixityErrorIcon() {

        if (checksumErrorIcon == null) {
            checksumErrorIcon = createImage("images/checksum-error.png", "fixity check fail");
        }
        JLabel virusLabel = new JLabel(new ImageIcon(checksumErrorIcon));
        virusLabel.setToolTipText("fixity check error - missing checksum");

        return virusLabel;
    }

    public static JLabel getFixityOkIcon() {

        if (checksumValidIcon == null) {
            checksumValidIcon = createImage("images/checksum-valid.png", "fixity check ok");
        }
        JLabel virusLabel = new JLabel(new ImageIcon(checksumValidIcon));
        virusLabel.setToolTipText("fixity check success");

        return virusLabel;
    }

    public static JLabel getPolicyIcon(String policyDescription) {

        if (policyDescription == null) {
            policyDescription = "";
        }
        if (policyIcon == null) {
            policyIcon = createImage("images/policy.png", "policy");
        }
        JLabel policyLabel = new JLabel(new ImageIcon(policyIcon));
        policyLabel.setToolTipText("This collection has a bound policy - " + policyDescription);

        return policyLabel;
    }

    public static Image getMetadataImage() {

        if (metadataIcon == null) {
            metadataIcon = createImage("images/metadata.png", "metadata icon");
        }
        return metadataIcon;
    }

    public static Image getReplicationImage() {

        if (replicationIcon == null) {
            replicationIcon = createImage("images/replication-status.png", "replicationicon");
        }
        return replicationIcon;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static Image createImage(String path, String description) {
        URL imageURL = iDrop.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
